package com.erzhena.spaceexplorer_api.dto;

import java.util.List;

public record CursorResponse<T>(
        List<T> content,
        String nextCursor
) {
}