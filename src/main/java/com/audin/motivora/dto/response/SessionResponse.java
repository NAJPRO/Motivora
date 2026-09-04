package com.audin.motivora.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * One active session, as shown in "my connected devices".
 */
public record SessionResponse(
        Integer id,
        String deviceId,
        String deviceName,
        String platform,
        boolean current,
        Instant expireAt,
        LocalDateTime createdAt) {
}
