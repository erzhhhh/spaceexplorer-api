package com.erzhena.spaceexplorer_api.dto;

import com.erzhena.spaceexplorer_api.entity.Article;

import java.time.Instant;

// Для отдачи entity. Он создан для отдачи наружу. Это и есть твой публичный контракт: то,
// что видит Android-приложение. Отдавать его — его прямая работа.
// DTO, граница с твоими клиентами
public record ArticleResponse(
        Long id,
        String title,
        String url,
        String imageUrl,
        String newsSite,
        String summary,
        Instant publishedAt
) {

    public static ArticleResponse from(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getUrl(),
                article.getImageUrl(),
                article.getNewsSite(),
                article.getSummary(),
                article.getPublishedAt()
        );
    }
}
