package com.erzhena.spaceexplorer_api;

import java.time.Instant;

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
