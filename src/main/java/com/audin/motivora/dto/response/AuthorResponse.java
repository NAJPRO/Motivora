package com.audin.motivora.dto.response;

import java.util.List;

public record AuthorResponse(
    Integer id,
    String name,
    String slug,
    String bio,
    String avatarUrl
) {

}
