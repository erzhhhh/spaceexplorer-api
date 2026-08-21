package com.erzhena.spaceexplorer_api;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

// внутренний, но по другой причине. Это чужой формат, формат SNAPI.
// Если отдать его наружу, твой API окажется привязан к чужому — SNAPI
// поменяет структуру, и твои клиенты сломаются. Поэтому он живёт внутри
// и превращается в Article, а потом в ArticleResponse.
// DTO, граница с чужим API
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SnapiArticle(
        Long id,
        String title,
        String url,
        String imageUrl,
        String newsSite,
        String summary,
        Instant publishedAt,
        Instant updatedAt
) {
}
