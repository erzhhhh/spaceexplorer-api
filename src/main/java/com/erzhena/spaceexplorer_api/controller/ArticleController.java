package com.erzhena.spaceexplorer_api.controller;

import com.erzhena.spaceexplorer_api.dto.ArticleResponse;
import com.erzhena.spaceexplorer_api.dto.SliceResponse;
import com.erzhena.spaceexplorer_api.service.ArticleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@Tag(name = "Articles (offset pagination)")
public class ArticleController {

    private final ArticleService service;

    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @GetMapping // get - не меняет состояние сервера. Тело не нужно
    public SliceResponse<ArticleResponse> getByOffset(
            @ParameterObject
            @PageableDefault(sort = {"publishedAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return service.getByOffset(pageable);
    }
}