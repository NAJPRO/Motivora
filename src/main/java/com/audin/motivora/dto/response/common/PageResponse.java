package com.audin.motivora.dto.response.common;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Stable pagination envelope.
 *
 * Serializing Spring's {@code Page} leaks its internals ({@code pageable}, {@code sort},
 * {@code numberOfElements}...), whose shape changes between Spring versions — a problem for
 * mobile clients that stay installed across releases. {@code hasNext} is what an infinite
 * scroll actually needs.
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious());
    }
}
