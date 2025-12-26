package com.audin.motivora.mapper;

import java.util.List;

import com.audin.motivora.dto.request.QuoteRequest;
import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.entity.Quote;

public interface QuoteMapper {
    QuoteResponse toResponse(Quote quote);
    List<QuoteResponse> toResponse(List<Quote> quotes);
    Quote toEntity(QuoteRequest dto);
    Quote toEntityUpdate(Quote quote, QuoteRequest dto);

}
