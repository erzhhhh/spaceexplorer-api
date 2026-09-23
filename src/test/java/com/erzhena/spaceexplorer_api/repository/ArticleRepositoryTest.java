package com.erzhena.spaceexplorer_api.repository;

import com.erzhena.spaceexplorer_api.TestcontainersConfiguration;
import com.erzhena.spaceexplorer_api.entity.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository repository;

    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    void findLatestReturnsNewestArticlesFirst() {
        repository.saveAll(List.of(
                article(1L, Instant.parse("2026-09-01T10:00:00Z")),
                article(3L, Instant.parse("2026-09-03T10:00:00Z")),
                article(2L, Instant.parse("2026-09-02T10:00:00Z"))
        ));

        List<Article> result = repository.findLatest(2);

        assertThat(result)
                .extracting(Article::getId)
                .containsExactly(3L, 2L);
    }

    @Test
    void findLatestOrdersByIdWhenDatesAreEqual() {
        Instant sameDate = Instant.parse("2026-09-01T10:00:00Z");
        repository.saveAll(List.of(
                article(1L, sameDate),
                article(3L, sameDate),
                article(2L, sameDate)
        ));

        List<Article> result = repository.findLatest(3);

        assertThat(result)
                .extracting(Article::getId)
                .containsExactly(3L, 2L, 1L);
    }

    @Test
    void findLatestReturnsAllWhenLimitExceedsCount() {
        repository.saveAll(List.of(
                article(1L, Instant.parse("2026-09-01T10:00:00Z")),
                article(2L, Instant.parse("2026-09-02T10:00:00Z"))
        ));

        List<Article> result = repository.findLatest(10);

        assertThat(result)
                .extracting(Article::getId)
                .containsExactly(2L, 1L);
    }

    @Test
    void findOlderThanReturnsArticlesBeforeCursor() {
        Instant cursorDate = Instant.parse("2026-09-03T10:00:00Z");
        repository.saveAll(List.of(
                article(1L, Instant.parse("2026-09-01T10:00:00Z")),
                article(4L, Instant.parse("2026-09-04T10:00:00Z")),
                article(3L, cursorDate),
                article(2L, Instant.parse("2026-09-02T10:00:00Z"))
        ));

        List<Article> result = repository.findOlderThan(cursorDate, 3L, 10);

        assertThat(result)
                .extracting(Article::getId)
                .containsExactly(2L, 1L);
    }

    @Test
    void findOlderThanUsesIdWhenDatesAreEqual() {
        Instant sameDate = Instant.parse("2026-09-02T10:00:00Z");
        repository.saveAll(List.of(
                article(1L, Instant.parse("2026-09-01T10:00:00Z")),
                article(3L, sameDate),
                article(2L, sameDate),
                article(4L, sameDate)
        ));

        // Cursor points to article 3
        List<Article> result = repository.findOlderThan(sameDate, 3L, 10);

        assertThat(result)
                .extracting(Article::getId)
                .containsExactly(2L, 1L);
    }

    @Test
    void findOlderThanRespectsLimit() {
        Instant cursorDate = Instant.parse("2026-09-04T10:00:00Z");
        repository.saveAll(List.of(
                article(1L, Instant.parse("2026-09-01T10:00:00Z")),
                article(3L, Instant.parse("2026-09-03T10:00:00Z")),
                article(2L, Instant.parse("2026-09-02T10:00:00Z"))
        ));

        List<Article> result = repository.findOlderThan(cursorDate, 4L, 2);

        assertThat(result)
                .extracting(Article::getId)
                .containsExactly(3L, 2L);
    }

    private Article article(Long id, Instant publishedAt) {
        Article article = new Article();
        article.setId(id);
        article.setTitle("Title " + id);
        article.setUrl("https://example.com/" + id);
        article.setNewsSite("NASA");
        article.setPublishedAt(publishedAt);
        article.setUpdatedAt(publishedAt);
        return article;
    }
}