package com.audin.motivora.service;

import org.springframework.data.domain.Page;

import com.audin.motivora.dto.request.QuoteRequest;
import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.entity.Author;
import com.audin.motivora.entity.Theme;

public interface QuoteService {

    QuoteResponse getQuote(String idOrSlug);

    QuoteResponse getRandomQuote();

    /** Same quote for everyone for a given day: what the mobile home screen shows. */
    QuoteResponse getQuoteOfTheDay();

    Page<QuoteResponse> getAllQuotes(int page, int size);

    Page<QuoteResponse> getAllQuotesForAdmin(String status, int page, int size);

    Page<QuoteResponse> searchByKeyword(String keyword, int page, int size);

    Page<QuoteResponse> getQuotesByAuthor(Integer authorId, int page, int size);

    Page<QuoteResponse> getQuotesByTheme(String themeIdOrSlug, int page, int size);

    // Admin methods
    QuoteResponse save(QuoteRequest dto);
    QuoteResponse update(String idOrSlug, QuoteRequest dto);
    void disable(String idOrSlug);
    void enable(String idOrSlug);
    void disableByAuthor(Author author);
    void enableByAuthor(Author author);
    void disableByTheme(Theme theme);
    void enableByTheme(Theme theme);
}
