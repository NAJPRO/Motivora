package com.audin.motivora.dto.response;

public record ThemeResponse(
    Integer id,
    String slug,
    String name,
    String description,
    String color,
    String imageUrl,
    boolean isActive
) {

}
