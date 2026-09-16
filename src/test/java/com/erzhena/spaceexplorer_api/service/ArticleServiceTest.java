package com.erzhena.spaceexplorer_api.service;

import com.erzhena.spaceexplorer_api.client.SnapiClient;
import com.erzhena.spaceexplorer_api.client.dto.SnapiArticle;
import com.erzhena.spaceexplorer_api.entity.Article;
import com.erzhena.spaceexplorer_api.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    private static final Instant OLD_DATE = Instant.parse("2026-09-01T10:00:00Z");
    private static final Instant NEW_DATE = Instant.parse("2026-09-02T10:00:00Z");

    @Mock
    ArticleRepository repository;

    @Mock
    SnapiClient snapiClient;

    @Captor
    ArgumentCaptor<List<Article>> captor;

    @InjectMocks
    ArticleService service;

    @Test
    void importSavesNewArticle() {
        SnapiArticle article = snapiArticle(1L, OLD_DATE);
        when(snapiClient.fetchArticles(10)).thenReturn(List.of(article));

        // The article doesn't exist in the DB
        when(repository.findAllById(List.of(1L))).thenReturn(List.of());

        int saved = service.importFromSnapi(10);

        assertThat(saved).isEqualTo(1);

        verify(repository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .extracting(Article::getId)
                .containsExactly(1L);
    }

    @Test
    void importSkipsUnchangedArticle() {
        SnapiArticle article = snapiArticle(1L, OLD_DATE);
        when(snapiClient.fetchArticles(10)).thenReturn(List.of(article));

        // The article exists in the DB
        Article existing = existingArticle(1L, OLD_DATE);
        when(repository.findAllById(List.of(1L))).thenReturn(List.of(existing));

        int saved = service.importFromSnapi(10);

        assertThat(saved).isZero();
        verify(repository).saveAll(List.of());
    }

    @Test
    void importSavesUpdatedArticle() {
        SnapiArticle article = snapiArticle(1L, NEW_DATE);
        when(snapiClient.fetchArticles(10)).thenReturn(List.of(article));

        // The article exists in the DB with an older updatedAt
        Article existing = existingArticle(1L, OLD_DATE);
        when(repository.findAllById(List.of(1L))).thenReturn(List.of(existing));

        int saved = service.importFromSnapi(10);

        assertThat(saved).isEqualTo(1);

        verify(repository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .extracting(Article::getId)
                .containsExactly(1L);
    }

    @Test
    void importSkipsArticleWhenDbVersionIsNewer() {
        SnapiArticle article = snapiArticle(1L, OLD_DATE);
        when(snapiClient.fetchArticles(10)).thenReturn(List.of(article));

        // The article exists in the DB with a fresher updatedAt
        Article existing = existingArticle(1L, NEW_DATE);
        when(repository.findAllById(List.of(1L))).thenReturn(List.of(existing));

        int saved = service.importFromSnapi(10);

        assertThat(saved).isZero();
        verify(repository).saveAll(List.of());
    }

    @Test
    void importSavesOnlyNewAndUpdatedArticles() {
        // SNAPI returns 3 articles
        when(snapiClient.fetchArticles(10)).thenReturn(List.of(
                snapiArticle(1L, NEW_DATE),   // new
                snapiArticle(2L, OLD_DATE),   // unchanged
                snapiArticle(3L, NEW_DATE)    // changed
        ));

        // DB has articles 2 and 3, both with the old date
        when(repository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(
                existingArticle(2L, OLD_DATE),
                existingArticle(3L, OLD_DATE)
        ));

        int saved = service.importFromSnapi(10);

        assertThat(saved).isEqualTo(2);

        // Check which articles were saved
        verify(repository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .extracting(Article::getId)
                .containsExactly(1L, 3L);
    }

    @Test
    void importThrowsWhenSnapiFails() {
        // SNAPI throws an exception
        when(snapiClient.fetchArticles(10))
                .thenThrow(new RuntimeException("SNAPI is unavailable"));

        // import() method should rethrow it
        assertThatThrownBy(() -> service.importFromSnapi(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("SNAPI is unavailable");

        // Nothing saved into the DB
        verify(repository, never()).saveAll(any());
    }

    private Article existingArticle(Long id, Instant updatedAt) {
        Article article = new Article();
        article.setId(id);
        article.setUpdatedAt(updatedAt);
        return article;
    }

    private SnapiArticle snapiArticle(Long id, Instant updatedAt) {
        return new SnapiArticle(
                id,
                "Title " + id,
                "https://example.com/" + id,
                "https://example.com/" + id + ".jpg",
                "NASA",
                "Summary",
                Instant.parse("2026-09-01T09:00:00Z"),
                updatedAt
        );
    }
}