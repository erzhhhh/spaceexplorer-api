package com.erzhena.spaceexplorer_api.service;

import com.erzhena.spaceexplorer_api.client.SnapiClient;
import com.erzhena.spaceexplorer_api.client.dto.SnapiArticle;
import com.erzhena.spaceexplorer_api.dto.ArticleCursor;
import com.erzhena.spaceexplorer_api.dto.ArticleResponse;
import com.erzhena.spaceexplorer_api.dto.CursorResponse;
import com.erzhena.spaceexplorer_api.entity.Article;
import com.erzhena.spaceexplorer_api.exception.InvalidCursorException;
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
        Article existing = article(1L, OLD_DATE);
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
        Article existing = article(1L, OLD_DATE);
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
        Article existing = article(1L, NEW_DATE);
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
                article(2L, OLD_DATE),
                article(3L, OLD_DATE)
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

        // importFromSnapi() should rethrow it
        assertThatThrownBy(() -> service.importFromSnapi(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("SNAPI is unavailable");

        // Nothing saved into the DB
        verify(repository, never()).saveAll(any());
    }

    @Test
    void getByCursorReturnsFirstPageWithoutNextCursor() {
        // No cursor: the service asks for size + 1 latest articles: there is no next page
        when(repository.findLatest(11)).thenReturn(List.of(
                article(2L, NEW_DATE),
                article(1L, OLD_DATE)
        ));

        CursorResponse<ArticleResponse> response = service.getByCursor(null, 10);

        assertThat(response.content()).hasSize(2);
        assertThat(response.nextCursor()).isNull();
        verify(repository, never()).findOlderThan(any(), any(), anyInt());
    }

    @Test
    void getByCursorTrimsExtraArticleAndReturnsNextCursor() {
        // The repository returns size + 1 articles: there is a next page
        when(repository.findLatest(3)).thenReturn(List.of(
                article(3L, NEW_DATE, NEW_DATE),
                article(2L, NEW_DATE, NEW_DATE),
                article(1L, OLD_DATE, OLD_DATE)
        ));

        CursorResponse<ArticleResponse> response = service.getByCursor(null, 2);

        assertThat(response.content())
                .extracting(ArticleResponse::id)
                .containsExactly(3L, 2L);
        assertThat(response.nextCursor()).isNotNull();
        verify(repository, never()).findOlderThan(any(), any(), anyInt());
    }

    @Test
    void getByCursorLoadsArticlesOlderThanCursor() {
        String cursor = new ArticleCursor(NEW_DATE, 2L).encode();

        // Articles older than the date from the cursor
        when(repository.findOlderThan(NEW_DATE, 2L, 11)).thenReturn(List.of(
                article(1L, OLD_DATE)
        ));

        CursorResponse<ArticleResponse> response = service.getByCursor(cursor, 10);

        assertThat(response.content())
                .extracting(ArticleResponse::id)
                .containsExactly(1L);
        assertThat(response.nextCursor()).isNull();
        verify(repository, never()).findLatest(anyInt());
    }

    @Test
    void getByCursorBuildsNextCursorFromLastReturnedArticle() {
        when(repository.findLatest(3)).thenReturn(List.of(
                article(3L, NEW_DATE, NEW_DATE),
                article(2L, NEW_DATE, NEW_DATE),  // last on this page
                article(1L, OLD_DATE, OLD_DATE)   // extra
        ));

        CursorResponse<ArticleResponse> response = service.getByCursor(null, 2);

        // The cursor points to article 2, not to the extra article 1
        ArticleCursor next = ArticleCursor.decode(response.nextCursor());
        assertThat(next.id()).isEqualTo(2L);
        assertThat(next.publishedAt()).isEqualTo(NEW_DATE);
    }

    @Test
    void getByCursorReturnsNoNextCursorWhenExactlySizeArticles() {
        when(repository.findLatest(3)).thenReturn(List.of(
                article(2L, NEW_DATE),
                article(1L, OLD_DATE)
        ));

        CursorResponse<ArticleResponse> response = service.getByCursor(null, 2);

        assertThat(response.content()).hasSize(2);
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void getByCursorThrowsOnInvalidCursor() {
        assertThatThrownBy(() -> service.getByCursor("not-a-cursor", 10))
                .isInstanceOf(InvalidCursorException.class);
    }

    private Article article(Long id, Instant updatedAt) {
        return article(id, updatedAt, null);
    }

    private Article article(Long id, Instant updatedAt, Instant publishedAt) {
        Article article = new Article();
        article.setId(id);
        article.setUpdatedAt(updatedAt);
        article.setPublishedAt(publishedAt);
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