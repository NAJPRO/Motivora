package com.audin.motivora.dto.response;

import java.time.LocalDateTime;

/**
 * A quote as returned to clients.
 *
 * {@code createdByUser} is only populated on admin endpoints: it carries the submitter's
 * email, which must never leave through the public catalogue.
 */
public record QuoteResponse(
        Integer id,
        String slug,
        String content,
        AuthorResponse author,
        ThemeResponse theme,
        String status,
        boolean isFavorite,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        UserDTOResponse createdByUser) {
}
