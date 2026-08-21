package com.erzhena.spaceexplorer_api.client.dto;

import java.util.List;

public record SnapiArticlesResponse(
        int count,
        String next,
        String previous,
        List<SnapiArticle> results
) {
}
