package com.audin.motivora.service;

import java.util.List;

import com.audin.motivora.dto.request.ThemeRequest;
import com.audin.motivora.dto.response.ThemeResponse;

public interface ThemeService {

    ThemeResponse save(ThemeRequest request);
    ThemeResponse update(String idOrSlug, ThemeRequest request);
    ThemeResponse create(String idOrSlug);
    void disable(String idOrSlug);
    void enable(String idOrSlug);

    /** Admin listing: every theme, active or not. */
    List<ThemeResponse> getAll();

    /** Public catalogue: active themes only. */
    List<ThemeResponse> getAllActive();

    /** Public detail: an active theme, or 404. */
    ThemeResponse getActive(String idOrSlug);
}
