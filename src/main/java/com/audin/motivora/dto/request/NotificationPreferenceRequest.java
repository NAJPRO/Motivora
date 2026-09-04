package com.audin.motivora.dto.request;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationPreferenceRequest {

    private Boolean dailyQuoteEnabled;

    /** Local hour of day, 0-23, interpreted in {@code timezone}. */
    @Min(value = 0, message = "Hour must be between 0 and 23")
    @Max(value = 23, message = "Hour must be between 0 and 23")
    private Integer dailyQuoteHour;

    /** IANA zone id, e.g. {@code Africa/Porto-Novo}. */
    private String timezone;

    /** Theme ids the user wants their daily quote drawn from. Empty means any theme. */
    private List<Integer> followedThemeIds;
}
