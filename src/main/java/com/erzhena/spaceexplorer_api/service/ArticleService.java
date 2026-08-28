package com.erzhena.spaceexplorer_api.service;

import com.erzhena.spaceexplorer_api.client.SnapiClient;
import com.erzhena.spaceexplorer_api.client.dto.SnapiArticle;
import com.erzhena.spaceexplorer_api.dto.ArticleCursor;
import com.erzhena.spaceexplorer_api.dto.ArticleResponse;
import com.erzhena.spaceexplorer_api.dto.CursorResponse;
import com.erzhena.spaceexplorer_api.dto.SliceResponse;
import com.erzhena.spaceexplorer_api.entity.Article;
import com.erzhena.spaceexplorer_api.repository.ArticleRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service // @Inject constructor --- это бин
public class ArticleService {

    private final ArticleRepository repository;
    private final SnapiClient snapiClient;

    public ArticleService(ArticleRepository repository, SnapiClient snapiClient) {
        this.repository = repository;
        this.snapiClient = snapiClient;
    }

    public SliceResponse<ArticleResponse> getByOffset(Pageable pageable) {
        Slice<ArticleResponse> slice = repository.findAllBy(pageable)
                .map(ArticleResponse::from);

        return SliceResponse.from(slice);
    }

    public CursorResponse<ArticleResponse> getByCursor(String cursor, int size) {
        int limit = size + 1;

        List<Article> articles;
        if (cursor == null) {
            articles = repository.findLatest(limit);
        } else {
            ArticleCursor articleCursor = ArticleCursor.decode(cursor);
            articles = repository.findOlderThan(articleCursor.publishedAt(), articleCursor.id(), limit);
        }

        boolean hasNext = articles.size() > size;
        if (hasNext) {
            articles = articles.subList(0, size);
        }

        String nextCursor = null;
        if (hasNext) {
            Article last = articles.getLast();
            nextCursor = new ArticleCursor(last.getPublishedAt(), last.getId()).encode();
        }

        List<ArticleResponse> content = articles.stream().map(ArticleResponse::from).toList();

        return new CursorResponse<>(content, nextCursor);
    }

    // Аннотация над методом означает: всё, что происходит внутри, —
    // одна транзакция. Метод завершился нормально → изменения фиксируются.
    // Вылетело исключение → всё откатывается.
    @Transactional
    public int importFromSnapi(int limit) {
        List<SnapiArticle> fetched = snapiClient.fetchArticles(limit);

        List<Long> ids = fetched.stream()
                .map(SnapiArticle::id)
                .toList();

        Map<Long, Instant> known = repository.findAllById(ids).stream()
                .collect(Collectors.toMap(Article::getId, Article::getUpdatedAt));

        List<Article> toSave = fetched.stream()
                .filter(dto -> isNewOrChanged(dto, known.get(dto.id())))
                .map(this::toEntity)
                .toList();

        repository.saveAll(toSave);
        return toSave.size();
    }

    private boolean isNewOrChanged(SnapiArticle dto, Instant knownUpdatedAt) {
        return knownUpdatedAt == null || dto.updatedAt().isAfter(knownUpdatedAt);
    }

    private Article toEntity(SnapiArticle dto) {
        Article article = new Article();
        article.setId(dto.id());
        article.setTitle(dto.title());
        article.setUrl(dto.url());
        article.setImageUrl(dto.imageUrl());
        article.setNewsSite(dto.newsSite());
        article.setSummary(dto.summary());
        article.setPublishedAt(dto.publishedAt());
        article.setUpdatedAt(dto.updatedAt());
        return article;
    }
}