package com.erzhena.spaceexplorer_api.controller;

import com.erzhena.spaceexplorer_api.dto.ArticleResponse;
import com.erzhena.spaceexplorer_api.dto.CursorResponse;
import com.erzhena.spaceexplorer_api.exception.InvalidCursorException;
import com.erzhena.spaceexplorer_api.exception.SnapiUnavailableException;
import com.erzhena.spaceexplorer_api.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@WebMvcTest(ArticleV2Controller.class)
class ArticleV2ControllerTest {

    @Autowired
    MockMvcTester mvc; // «ненастоящий браузер»: отправляет запросы в контроллер без сервера и сети и даёт проверить ответ.

    @MockitoBean
    ArticleService service;

    @Test
    void importArticlesReturnsNumberOfSavedArticles() {
        // The service reports 5 saved articles
        when(service.importFromSnapi(20)).thenReturn(5);

        // Send POST /api/v2/articles/import
        assertThat(mvc.post().uri("/api/v2/articles/import"))
                .hasStatusOk()
                .bodyText().isEqualTo("5");

        // The controller called the service with limit 20
        verify(service).importFromSnapi(20);
    }

    @Test
    void getByCursorUsesDefaultCursorAndSize() {
        when(service.getByCursor(any(), anyInt()))
                .thenReturn(new CursorResponse<>(List.of(), null));

        assertThat(mvc.get().uri("/api/v2/articles"))
                .hasStatusOk();

        verify(service).getByCursor(null, 20);
    }

    @Test
    void getByCursorUsesCursorAndSize() {
        when(service.getByCursor("abc", 5))
                .thenReturn(new CursorResponse<>(List.of(), null));

        assertThat(mvc.get().uri("/api/v2/articles")
                .param("cursor", "abc")
                .param("size", "5"))
                .hasStatusOk();

        verify(service).getByCursor("abc", 5);
    }

    @Test
    void getByCursorReturnsArticlesAsJson() {
        ArticleResponse article = new ArticleResponse(
                1L,
                "Starship launch",
                "https://example.com/1",
                "https://example.com/1.jpg",
                "NASA",
                "Summary",
                Instant.parse("2026-09-01T10:00:00Z")
        );
        CursorResponse<ArticleResponse> response = new CursorResponse<>(List.of(article), "next-page-cursor");

        when(service.getByCursor(any(), anyInt()))
                .thenReturn(response);

        assertThat(mvc.get().uri("/api/v2/articles")
                .param("cursor", "abc")
                .param("size", "5"))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        {
                          "content": [
                            {
                              "id": 1,
                              "title": "Starship launch",
                              "url": "https://example.com/1",
                              "imageUrl": "https://example.com/1.jpg",
                              "newsSite": "NASA",
                              "summary": "Summary",
                              "publishedAt": "2026-09-01T10:00:00Z"
                            }
                          ],
                          "nextCursor": "next-page-cursor"
                        }
                        """);
    }

    @Test
    void getByCursorReturns400WhenCursorIsInvalid() {
        when(service.getByCursor(any(), anyInt()))
                .thenThrow(new InvalidCursorException("bad cursor"));

        assertThat(mvc.get().uri("/api/v2/articles").param("cursor", "garbage"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.title").isEqualTo("Invalid cursor");
    }

    @Test
    void getByCursorReturns400WhenSizeIsTooSmall() {
        assertThat(mvc.get().uri("/api/v2/articles")
                .param("cursor", "abc")
                .param("size", "0"))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(service, never()).getByCursor(any(), anyInt());
    }

    @Test
    void getByCursorReturns400WhenSizeIsTooBig() {
        assertThat(mvc.get().uri("/api/v2/articles")
                .param("cursor", "abc")
                .param("size", "101"))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(service, never()).getByCursor(any(), anyInt());
    }

    @Test
    void importReturns503WhenSnapiIsUnavailable() {
        when(service.importFromSnapi(20))
                .thenThrow(new SnapiUnavailableException("SNAPI down", new RuntimeException()));

        assertThat(mvc.post().uri("/api/v2/articles/import"))
                .hasStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .bodyJson()
                .extractingPath("$.title").isEqualTo("News source unavailable");
    }
}