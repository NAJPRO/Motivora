package com.audin.motivora.mapper.Impl;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.audin.motivora.dto.request.QuoteRequest;
import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.enums.QuoteStatus;
import com.audin.motivora.mapper.AuthMapper;
import com.audin.motivora.mapper.AuthorMapper;
import com.audin.motivora.mapper.QuoteMapper;
import com.audin.motivora.mapper.ThemeMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuoteMapperImpl implements QuoteMapper {

    private final AuthorMapper authorMapper;
    private final ThemeMapper themeMapper;
    private final AuthMapper authMapper;

    @Override
    public Quote toEntity(QuoteRequest dto) {
        Quote quote = new Quote();
        quote.setContent(dto.getContent());
        return quote;
    }

    @Override
    public QuoteResponse toResponse(Quote quote) {
        return this.toResponse(quote, false);
    }

    @Override
    public QuoteResponse toResponse(Quote quote, boolean favorite) {
        return this.map(quote, favorite, false);
    }

    @Override
    public List<QuoteResponse> toResponse(List<Quote> quotes) {
        return quotes.stream().map(this::toResponse).toList();
    }

    @Override
    public List<QuoteResponse> toResponse(List<Quote> quotes, Set<Integer> favoritedQuoteIds) {
        return quotes.stream()
                .map(quote -> this.toResponse(quote, favoritedQuoteIds.contains(quote.getId())))
                .toList();
    }

    @Override
    public QuoteResponse toAdminResponse(Quote quote) {
        return this.map(quote, false, true);
    }

    @Override
    public Quote toEntityUpdate(Quote quote, QuoteRequest dto) {
        if (dto.getContent() != null && !dto.getContent().equals(quote.getContent())) {
            quote.setContent(dto.getContent());
        }
        return quote;
    }

    private QuoteResponse map(Quote quote, boolean favorite, boolean includeSubmitter) {
        if (quote == null) {
            return null;
        }
        return new QuoteResponse(
                quote.getId(),
                quote.getSlug(),
                quote.getContent(),
                quote.getAuthor() != null ? this.authorMapper.toResponse(quote.getAuthor()) : null,
                quote.getTheme() != null ? this.themeMapper.toResponse(quote.getTheme()) : null,
                this.statusToken(quote.getStatus()),
                favorite,
                quote.getPublishedAt(),
                quote.getCreatedAt(),
                includeSubmitter && quote.getCreatedByUser() != null
                        ? this.authMapper.toDto(quote.getCreatedByUser())
                        : null);
    }

    /**
     * Normalizes the internal {@link QuoteStatus} to the vocabulary expected by the
     * admin front (see ADMIN_BACKEND_CONTRACT §4): PUBLISHED / PENDING / DISABLED / ...
     */
    private String statusToken(QuoteStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PUBLISHED -> "PUBLISHED";
            case REVIEW_PENDING -> "PENDING";
            case DISABLE -> "DISABLED";
            case ARCHIVED -> "ARCHIVED";
            case DRAFT -> "DRAFT";
            case REJECTED -> "REJECTED";
            case DELETED -> "DELETED";
        };
    }
}
