package com.audin.motivora.mapper.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.audin.motivora.dto.request.ThemeRequest;
import com.audin.motivora.dto.response.ThemeResponse;
import com.audin.motivora.entity.Theme;
import com.audin.motivora.mapper.QuoteMapper;
import com.audin.motivora.mapper.ThemeMapper;

@Component
public class ThemeMapperImpl implements ThemeMapper {

    @Override
    public Theme toEntity(ThemeRequest dto) {
        Theme theme = new Theme();
        theme.setName(dto.getName());
        theme.setDescription(dto.getDescription());
        theme.setColor(dto.getColor());
        theme.setImageUrl(dto.getImageUrl());
        return theme;
    }

    @Override
    public ThemeResponse toResponse(Theme theme) {
        QuoteMapper quoteMapper = new QuoteMapperImpl();
        ThemeResponse response = new ThemeResponse(
            theme.getId(),
            theme.getName(),
            theme.getDescription(),
            theme.getColor(),
            theme.getImageUrl(),
            quoteMapper.toResponse(theme.getQuotes())
        );
        return response;
    }

    @Override
    public List<ThemeResponse> toResponse(List<Theme> themes) {
        ArrayList<ThemeResponse> responses = new ArrayList<>();
        for( Theme theme : themes ) {
            responses.add(this.toResponse(theme));
        }
        return responses;
    }

    @Override
    public Theme toEntityUpdate(Theme theme, ThemeRequest dto) {
        if(!theme.getName().equals(dto.getName()))
            theme.setName(dto.getName());
        if(!theme.getDescription().equals(dto.getDescription()))
            theme.setDescription(dto.getDescription());
        if(!theme.getColor().equals(dto.getColor()))
            theme.setColor(dto.getColor());
        return theme;
    }

}
