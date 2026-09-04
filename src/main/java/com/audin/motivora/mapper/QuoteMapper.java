package com.audin.motivora.mapper;

import java.util.List;
import java.util.Set;

import com.audin.motivora.dto.request.QuoteRequest;
import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.entity.Quote;

public interface QuoteMapper {

    /** Public shape: no submitter identity, {@code isFavorite} defaults to false. */
    QuoteResponse toResponse(Quote quote);

    QuoteResponse toResponse(Quote quote, boolean favorite);

    List<QuoteResponse> toResponse(List<Quote> quotes);

    /** Public shape, flagging which quotes the caller has favourited. */
    List<QuoteResponse> toResponse(List<Quote> quotes, Set<Integer> favoritedQuoteIds);

    /** Admin shape: includes {@code createdByUser}. */
    QuoteResponse toAdminResponse(Quote quote);

    Quote toEntity(QuoteRequest dto);

    Quote toEntityUpdate(Quote quote, QuoteRequest dto);
}
