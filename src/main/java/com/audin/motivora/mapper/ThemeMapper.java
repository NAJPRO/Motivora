package com.audin.motivora.mapper;

import java.util.List;

import com.audin.motivora.dto.request.ThemeRequest;
import com.audin.motivora.dto.response.ThemeResponse;
import com.audin.motivora.entity.Theme;

public interface ThemeMapper {
    Theme toEntity(ThemeRequest dto);
    Theme toEntityUpdate(Theme theme ,ThemeRequest dto);

    ThemeResponse toResponse(Theme theme);
    List<ThemeResponse> toResponse(List<Theme> themes);
}
