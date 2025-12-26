package com.audin.motivora.dto.response;

public record FavoriteResponse(
    Integer id,
    Integer userId,
    QuoteResponse quote
) {

}
