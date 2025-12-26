package com.audin.motivora.mapper.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.audin.motivora.dto.request.QuoteRequest;
import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.mapper.AuthorMapper;
import com.audin.motivora.mapper.QuoteMapper;
import com.audin.motivora.mapper.ThemeMapper;

@Component
public class QuoteMapperImpl implements QuoteMapper {

    @Override
    public Quote toEntity(QuoteRequest dto) {
        Quote quote = new Quote();
        quote.setContent(dto.getContent());
        return quote;
    }

    @Override
    public QuoteResponse toResponse(Quote quote) {
        AuthorMapper authorMapper = new AuthorMapperImpl();
        ThemeMapper themeMapper = new ThemeMapperImpl();
        QuoteResponse response = new QuoteResponse(
            quote.getId(),
            quote.getSlug(),
            quote.getContent(),
            authorMapper.toResponse(quote.getAuthor()),
            null,
            themeMapper.toResponse(quote.getTheme()),
            quote.getStatus().name()
        );
        return response;
    }

    @Override
    public List<QuoteResponse> toResponse(List<Quote> quotes) {
        ArrayList<QuoteResponse> responses = new ArrayList<>();
        for (Quote quote : quotes) {
            responses.add(this.toResponse(quote));
        }
        return responses;
    }

    @Override
    public Quote toEntityUpdate(Quote quote, QuoteRequest dto) {
        if (!quote.getContent().equals(dto.getContent())) {
            quote.setContent(dto.getContent());
        }
        return quote;
    }

}
