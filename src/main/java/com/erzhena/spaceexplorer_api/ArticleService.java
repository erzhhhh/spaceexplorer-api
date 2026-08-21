package com.erzhena.spaceexplorer_api;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // @Inject constructor --- это бин
public class ArticleService {

    private final ArticleRepository repository;
    private final SnapiClient snapiClient;

    public ArticleService(ArticleRepository repository, SnapiClient snapiClient) {
        this.repository = repository;
        this.snapiClient = snapiClient;
    }

    public List<ArticleResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(ArticleResponse::from)
                .toList();
    }

    // Аннотация над методом означает: всё, что происходит внутри, —
    // одна транзакция. Метод завершился нормально → изменения фиксируются.
    // Вылетело исключение → всё откатывается.
    @Transactional
    public int importFromSnapi(int limit) {
        List<SnapiArticle> fetched = snapiClient.fetchArticles(limit);

        List<Article> articles = fetched.stream()
                .map(this::toEntity)
                .toList();

        repository.saveAll(articles);
        return articles.size();
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