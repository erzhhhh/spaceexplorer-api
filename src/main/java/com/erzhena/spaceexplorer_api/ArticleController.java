package com.erzhena.spaceexplorer_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // @Inject constructor --- это бин
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService service;

    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @GetMapping // get - не меняет состояние сервера. Тело не нужно
    public List<ArticleResponse> getAll() {
        return service.getAll();
    }

    @PostMapping("/import") // post - меняет состояние сервера. Может быть без тела
    public int importArticles() {
        return service.importFromSnapi(20);
    }
}