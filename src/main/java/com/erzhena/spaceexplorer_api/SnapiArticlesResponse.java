package com.erzhena.spaceexplorer_api;

import java.util.List;

public record SnapiArticlesResponse(
        int count,
        String next,
        String previous,
        List<SnapiArticle> results
) {
}
