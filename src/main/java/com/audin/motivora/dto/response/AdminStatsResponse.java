package com.audin.motivora.dto.response;

public record AdminStatsResponse(
    long totalQuotes,
    long totalAuthors,
    long totalThemes,
    long totalUsers,
    long pendingQuotes
) {

}
