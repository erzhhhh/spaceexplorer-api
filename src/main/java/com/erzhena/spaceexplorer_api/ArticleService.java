package com.erzhena.spaceexplorer_api;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository repository;

    public ArticleService(ArticleRepository repository) {
        this.repository = repository;
    }

    public List<ArticleResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(ArticleResponse::from)
                .toList();
    }
}