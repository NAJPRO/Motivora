package com.audin.motivora.dto.response;

public record QuoteResponse(
    Integer id,
    String slug,
    String content,
    AuthorResponse author,
    UserDTOResponse createdByUser,
    ThemeResponse theme,
    String status
) {

}
