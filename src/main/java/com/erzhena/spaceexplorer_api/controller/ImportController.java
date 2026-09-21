package com.erzhena.spaceexplorer_api.controller;

import com.erzhena.spaceexplorer_api.service.ArticleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class ImportController {

    private final ArticleService service;

    public ImportController(ArticleService service) {
        this.service = service;
    }

    @PostMapping("/import")
    public int importArticles() {
        return service.importFromSnapi(20);
    }
}
