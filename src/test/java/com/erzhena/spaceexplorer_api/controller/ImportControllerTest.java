package com.erzhena.spaceexplorer_api.controller;

import com.erzhena.spaceexplorer_api.exception.SnapiUnavailableException;
import com.erzhena.spaceexplorer_api.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(ImportController.class)
class ImportControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    ArticleService service;

    @Test
    void importArticlesReturnsNumberOfSavedArticles() {
        // The service reports 5 saved articles
        when(service.importFromSnapi(20)).thenReturn(5);

        // Send POST /api/articles/import
        assertThat(mvc.post().uri("/api/admin/import"))
                .hasStatusOk()
                .bodyText().isEqualTo("5");

        // The controller called the service with limit 20
        verify(service).importFromSnapi(20);
    }

    @Test
    void importArticlesReturns503WhenSnapiIsUnavailable() {
        when(service.importFromSnapi(20)).thenThrow(
                new SnapiUnavailableException("SNAPI down", new RuntimeException())
        );

        assertThat(mvc.post().uri("/api/admin/import"))
                .hasStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .bodyJson()
                .extractingPath("$.title").isEqualTo("News source unavailable");
    }
}