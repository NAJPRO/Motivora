package com.audin.motivora.dto.response;

import java.time.LocalDateTime;

public record DeviceTokenResponse(
        Integer id,
        String platform,
        String deviceName,
        boolean enabled,
        LocalDateTime createdAt) {
}
