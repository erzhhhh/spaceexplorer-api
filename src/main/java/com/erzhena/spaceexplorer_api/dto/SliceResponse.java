package com.erzhena.spaceexplorer_api.dto;

import org.springframework.data.domain.Slice;

import java.util.List;

public record SliceResponse<T>(
        List<T> content,
        int page,
        int size,
        boolean last
) {
    public static <E> SliceResponse<E> from(Slice<E> slice) {
        return new SliceResponse<>(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                slice.isLast()
        );
    }
}