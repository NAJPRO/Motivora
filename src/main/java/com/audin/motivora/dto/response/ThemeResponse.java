package com.audin.motivora.dto.response;

import java.util.List;

public record ThemeResponse(
    Integer id,
    String name,
    String description,
    String color,
    String imageUrl,
    List<QuoteResponse> quotes
) {

}
