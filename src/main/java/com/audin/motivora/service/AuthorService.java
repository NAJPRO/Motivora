package com.audin.motivora.service;

import java.util.List;

import com.audin.motivora.dto.request.AuthorRequest;
import com.audin.motivora.dto.response.AuthorResponse;

public interface AuthorService {
    AuthorResponse save(AuthorRequest dto);
    AuthorResponse create(String idOrSlug);
    List<AuthorResponse> getAll();
    AuthorResponse update(String idOrSlug, AuthorRequest dto);
    void disable(String idOrSlug);
    void enable(String idOrSlug);

}
