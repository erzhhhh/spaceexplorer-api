package com.erzhena.spaceexplorer_api.controller;

import com.erzhena.spaceexplorer_api.dto.ArticleResponse;
import com.erzhena.spaceexplorer_api.dto.SliceResponse;
import com.erzhena.spaceexplorer_api.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    ArticleService service;

    @Test
    void getByOffsetReturnsArticlesAsJson() {
        ArticleResponse article = new ArticleResponse(
                1L,
                "Starship launch",
                "https://example.com/1",
                "https://example.com/1.jpg",
                "NASA",
                "Summary",
                Instant.parse("2026-09-01T10:00:00Z")
        );
        SliceResponse<ArticleResponse> response = new SliceResponse<>(List.of(article), 0, 10, true);

        when(service.getByOffset(any())).thenReturn(response);

        assertThat(mvc.get().uri("/api/articles"))
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
                          "page": 0,
                          "size": 10,
                          "last": true
                        }
                        """);
    }

    @Test
    void getByOffsetUsesDefaultPagingAndSorting() {
        when(service.getByOffset(any()))
                .thenReturn(new SliceResponse<>(List.of(), 0, 10, true));

        assertThat(mvc.get().uri("/api/articles"))
                .hasStatusOk();

        // Catch the Pageable the controller passed to the service
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).getByOffset(captor.capture());

        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort())
                .isEqualTo(Sort.by(Sort.Direction.DESC, "publishedAt", "id"));
    }

    @Test
    void getByOffsetUsesPagingFromRequest() {
        when(service.getByOffset(any()))
                .thenReturn(new SliceResponse<>(List.of(), 2, 5, true));

        assertThat(mvc.get().uri("/api/articles")
                .param("page", "2")
                .param("size", "5"))
                .hasStatusOk();

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).getByOffset(captor.capture());

        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort())
                .isEqualTo(Sort.by(Sort.Direction.DESC, "publishedAt", "id"));
    }
}