package com.erzhena.spaceexplorer_api.client;

import com.erzhena.spaceexplorer_api.client.dto.SnapiArticle;
import com.erzhena.spaceexplorer_api.config.SnapiConfig;
import com.erzhena.spaceexplorer_api.exception.SnapiUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(SnapiClient.class)
@Import(SnapiConfig.class)
class SnapiClientTest {

    @Autowired
    SnapiClient client;

    @Autowired
    MockRestServiceServer server; // it is created by @RestClientTest

    @Test
    void fetchArticlesParsesResponse() {
        // The fake server answers with SNAPI-style JSON
        server.expect(requestTo(containsString("/articles/")))
                .andRespond(
                        withSuccess(
                                """
                                        {
                                          "count": 1,
                                          "results": [
                                            {
                                              "id": 42,
                                              "title": "Starship launch",
                                              "url": "https://example.com/42",
                                              "image_url": "https://example.com/42.jpg",
                                              "news_site": "NASA",
                                              "summary": "Summary",
                                              "published_at": "2026-09-01T10:00:00Z",
                                              "updated_at": "2026-09-01T11:00:00Z"
                                            }
                                          ]
                                        }
                                        """,
                                MediaType.APPLICATION_JSON
                        )
                );

        List<SnapiArticle> articles = client.fetchArticles(10);

        assertThat(articles).hasSize(1);
        SnapiArticle article = articles.getFirst();
        assertThat(article.id()).isEqualTo(42L);
        assertThat(article.title()).isEqualTo("Starship launch");
        assertThat(article.newsSite()).isEqualTo("NASA");
        assertThat(article.publishedAt()).isEqualTo(Instant.parse("2026-09-01T10:00:00Z"));
    }

    @Test
    void fetchArticlesReturnsEmptyListWhenNoResults() {
        server.expect(requestTo(containsString("/articles/")))
                .andRespond(
                        withSuccess(
                                """
                                        {
                                          "count": 0,
                                          "results": []
                                        }
                                        """,
                                MediaType.APPLICATION_JSON
                        )
                );

        List<SnapiArticle> articles = client.fetchArticles(10);

        assertThat(articles).isEmpty();
    }

    @Test
    void fetchArticlesReturnsEmptyListWhenBodyIsEmpty() {
        // 200 with no body
        server.expect(requestTo(containsString("/articles/")))
                .andRespond(withSuccess());

        List<SnapiArticle> articles = client.fetchArticles(10);

        assertThat(articles).isEmpty();
    }

    @Test
    void fetchArticlesThrowsWhenSnapiFails500() {
        server.expect(requestTo(containsString("/articles/")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchArticles(10))
                .isInstanceOf(SnapiUnavailableException.class)
                .hasMessage("Failed to fetch articles from SNAPI")
                .cause().isInstanceOf(RestClientException.class);
    }

    @Test
    void fetchArticlesThrowsWhenSnapiFails404() {
        server.expect(requestTo(containsString("/articles/")))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.fetchArticles(10))
                .isInstanceOf(SnapiUnavailableException.class)
                .hasMessage("Failed to fetch articles from SNAPI")
                .cause().isInstanceOf(RestClientException.class);
    }

    @Test
    void fetchArticlesThrowsWhenSnapiFailsIncorrectJSON() {
        server.expect(requestTo(containsString("/articles/")))
                .andRespond(withSuccess("not a json", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchArticles(10))
                .isInstanceOf(SnapiUnavailableException.class)
                .hasMessage("Failed to fetch articles from SNAPI")
                .cause().isInstanceOf(RestClientException.class);
    }

    @Test
    void fetchArticlesSendsLimitToSnapi() {
        server.expect(requestTo("https://api.spaceflightnewsapi.net/v4/articles/?limit=10"))
                .andRespond(withSuccess("""
                        {"count": 0, "results": []}
                        """, MediaType.APPLICATION_JSON));

        client.fetchArticles(10);

        // Confirms that the expected request actually took place
        server.verify();
    }
}