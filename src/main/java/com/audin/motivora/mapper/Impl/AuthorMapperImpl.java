package com.audin.motivora.mapper.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.audin.motivora.dto.request.AuthorRequest;
import com.audin.motivora.dto.response.AuthorResponse;
import com.audin.motivora.entity.Author;
import com.audin.motivora.mapper.AuthorMapper;

@Component
public class AuthorMapperImpl implements AuthorMapper {

    @Override
    public AuthorResponse toResponse(Author author) {
        AuthorResponse response = new AuthorResponse(
            author.getId(),
            author.getName(),
            author.getSlug(),
            author.getBio(),
            author.getAvatarUrl()
        );
        return response;
    }

    @Override
    public List<AuthorResponse> toResponse(List<Author> authors) {
        ArrayList<AuthorResponse> responses = new ArrayList<>();
        for (Author author : authors) {
            responses.add(this.toResponse(author));
        }
        return responses;
    }

    @Override
    public Author toEntity(AuthorRequest dto) {
        Author author = new Author();
        author.setName(dto.getName());
        author.setBio(dto.getBio());
        author.setAvatarUrl(dto.getAvatarUrl());
        return author;
    }

    @Override
    public Author updateEntity(Author author, AuthorRequest dto) {
        author.setName(dto.getName());
        author.setBio(dto.getBio());
        author.setAvatarUrl(dto.getAvatarUrl());

        return author;
    }

    
}