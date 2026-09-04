package com.audin.motivora.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        Integer id,
        String type,
        String title,
        String message,
        Integer quoteId,
        String quoteSlug,
        boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt) {
}
