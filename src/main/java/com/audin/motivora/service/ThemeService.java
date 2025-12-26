package com.audin.motivora.service;

import java.util.List;

import com.audin.motivora.dto.request.ThemeRequest;
import com.audin.motivora.dto.response.ThemeResponse;

public interface ThemeService {
    void save(ThemeRequest request);
    ThemeResponse update(String idOrSlug, ThemeRequest request);
    ThemeResponse create(String idOrSlug);
    void disable(String idOrSlug);
    void enable(String idOrSlug);

    List<ThemeResponse> getAll();

}
