package com.erzhena.spaceexplorer_api.service;

import com.erzhena.spaceexplorer_api.TestcontainersConfiguration;
import com.erzhena.spaceexplorer_api.client.SnapiClient;
import com.erzhena.spaceexplorer_api.client.dto.SnapiArticle;
import com.erzhena.spaceexplorer_api.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ArticleServiceImportTransactionTest {

    @Autowired
    ArticleService service; // real service, with a real transaction

    @Autowired
    ArticleRepository repository; // real repository, accesses the database

    @MockitoBean
    SnapiClient snapiClient; // stub instead of the real client

    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    void importSavesAllArticlesWhenNothingFails() {
        when(snapiClient.fetchArticles(10)).thenReturn(List.of(
                snapiArticle(1L, "Starship launch"),
                snapiArticle(2L, "Mars rover update")
        ));

        int saved = service.importFromSnapi(10);

        assertThat(saved).isEqualTo(2);
        assertThat(repository.count()).isEqualTo(2); // repository.count() == SELECT COUNT(*) FROM article;
    }

    @Test
    void importRollsBackWhenSavingFails() {
        // The second article has no title, which violates NOT NULL
        when(snapiClient.fetchArticles(10)).thenReturn(List.of(
                snapiArticle(1L, "Starship launch"),
                snapiArticle(2L, null)
        ));

        assertThatThrownBy(() -> service.importFromSnapi(10))
                .isInstanceOf(RuntimeException.class);

        // The valid article is gone too: the transaction rolled back
        assertThat(repository.count()).isZero();
    }

    @Test
    void secondImportOfSameArticlesSavesNothing() {
        when(snapiClient.fetchArticles(10)).thenReturn(List.of(
                snapiArticle(1L, "Starship launch"),
                snapiArticle(2L, "Mars rover update")
        ));

        service.importFromSnapi(10);
        int savedSecondTime = service.importFromSnapi(10);

        assertThat(savedSecondTime).isZero();
        assertThat(repository.count()).isEqualTo(2);
    }

    private SnapiArticle snapiArticle(Long id, String title) {
        return new SnapiArticle(
                id,
                title,
                "https://example.com/" + id,
                "https://example.com/" + id + ".jpg",
                "NASA",
                "Summary",
                Instant.parse("2026-09-01T10:00:00Z"),
                Instant.parse("2026-09-01T11:00:00Z")
        );
    }
}