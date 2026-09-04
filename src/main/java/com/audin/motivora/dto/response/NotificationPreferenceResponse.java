package com.audin.motivora.dto.response;

import java.util.List;

public record NotificationPreferenceResponse(
        boolean dailyQuoteEnabled,
        int dailyQuoteHour,
        String timezone,
        List<ThemeResponse> followedThemes) {
}
