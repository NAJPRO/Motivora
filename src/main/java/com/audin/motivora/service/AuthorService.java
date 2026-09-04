package com.audin.motivora.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.audin.motivora.dto.request.AuthorRequest;
import com.audin.motivora.dto.response.AuthorResponse;

public interface AuthorService {

    AuthorResponse save(AuthorRequest dto);
    AuthorResponse create(String idOrSlug);
    List<AuthorResponse> getAll();
    Page<AuthorResponse> getAll(int page, int size);
    AuthorResponse update(String idOrSlug, AuthorRequest dto);
    void disable(String idOrSlug);
    void enable(String idOrSlug);

    /** Public catalogue: active authors only. */
    Page<AuthorResponse> getAllActive(int page, int size);

    /** Public detail: an active author, or 404. */
    AuthorResponse getActive(String idOrSlug);
}
