package com.erzhena.spaceexplorer_api;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

// да, только внутренний. Привязан к схеме таблицы, наружу уходить не должен
// entity, объект под управлением Hibernate, живёт только в слое работы с базой. Не DTO
@Entity
@Getter
@Setter
public class Article {

    @Id
    private Long id;

    private String title;

    private String url;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "news_site")
    private String newsSite;

    private String summary;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
