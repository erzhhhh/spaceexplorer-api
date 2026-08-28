package com.erzhena.spaceexplorer_api.controller;

import com.erzhena.spaceexplorer_api.dto.ArticleResponse;
import com.erzhena.spaceexplorer_api.dto.CursorResponse;
import com.erzhena.spaceexplorer_api.service.ArticleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/articles")
public class ArticleV2Controller {

    private final ArticleService service;

    public ArticleV2Controller(ArticleService service) {
        this.service = service;
    }

    @GetMapping
    public CursorResponse<ArticleResponse> getByCursor(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.getByCursor(cursor, size);
    }

    @PostMapping("/import")
    public int importArticles() {
        return service.importFromSnapi(20);
    }
}