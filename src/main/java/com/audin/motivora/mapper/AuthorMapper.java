package com.audin.motivora.mapper;

import java.util.List;

import com.audin.motivora.dto.request.AuthorRequest;
import com.audin.motivora.dto.response.AuthorResponse;
import com.audin.motivora.entity.Author;

public interface AuthorMapper {
    AuthorResponse toResponse(Author author);
    List<AuthorResponse> toResponse(List<Author> authors);
    Author toEntity(AuthorRequest dto);
    Author updateEntity(Author author, AuthorRequest dto);
}
 