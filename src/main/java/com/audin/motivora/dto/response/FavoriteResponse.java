package com.audin.motivora.dto.response;

import java.time.LocalDateTime;

public record FavoriteResponse(
    Integer id,
    Integer userId,
    QuoteResponse quote,
    LocalDateTime createdAt
) {

}
