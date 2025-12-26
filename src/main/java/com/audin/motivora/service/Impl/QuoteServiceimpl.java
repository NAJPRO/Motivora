package com.audin.motivora.service.Impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import com.audin.motivora.dto.request.QuoteRequest;
import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.entity.Author;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.Theme;
import com.audin.motivora.enums.QuoteStatus;
import com.audin.motivora.mapper.QuoteMapper;
import com.audin.motivora.repository.QuoteRepository;
import com.audin.motivora.service.QuoteService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuoteServiceimpl implements QuoteService {
    private final QuoteMapper quoteMapper;
    private final QuoteRepository quoteRepository;

    @Override
    public void disable(String idOrSlug) {
        Quote quote = this.findByIdOrSlug(idOrSlug);
        if (!quote.getStatus().equals(QuoteStatus.PUBLISHED)) {
            throw new IllegalStateException("Quote is already disabled");
        }
        quote.setStatus(QuoteStatus.DISABLE);
        quoteRepository.save(quote);
    }

    @Override
    public void enable(String idOrSlug) {
        Quote quote = this.findByIdOrSlug(idOrSlug);
        if (quote.getStatus().equals(QuoteStatus.PUBLISHED)) {
            throw new IllegalStateException("Quote is already enabled");
        }
        quote.setStatus(QuoteStatus.PUBLISHED);
        quoteRepository.save(quote);
    }

    @Override
    public Page<QuoteResponse> getAllQuotes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return quoteRepository
                .findAllPublishedQuotes(QuoteStatus.PUBLISHED, pageable)
                .map(quoteMapper::toResponse);
    }

    @Override
    public QuoteResponse getQuote(String idOrSlug) {
        return quoteMapper.toResponse(this.findByIdOrSlug(idOrSlug));
    }

    @Override
    public Page<QuoteResponse> getQuotesByAuthor(Integer authorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return quoteRepository
                .findAllQuotesByAuthor(authorId, QuoteStatus.PUBLISHED, pageable)
                .map(quoteMapper::toResponse);
    }

    @Override
    public QuoteResponse getRandomQuote() {
        Quote quote = quoteRepository
                .findAllByStatus(QuoteStatus.PUBLISHED)
                .stream()
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("No quotes available"));

        return quoteMapper.toResponse(quote);
    }

    @Override
    public QuoteResponse save(QuoteRequest dto) {
        Quote quote = quoteMapper.toEntity(dto);
        quote = quoteRepository.save(quote);
        return quoteMapper.toResponse(quote);
    }

    @Override
    public Page<QuoteResponse> searchByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return quoteRepository
                .searchByKeyword(keyword, QuoteStatus.PUBLISHED, pageable)
                .map(quoteMapper::toResponse);
    }

    @Override
    public QuoteResponse update(String idOrSlug, QuoteRequest dto) {
        Quote quote = this.findByIdOrSlug(idOrSlug);
        quote = quoteMapper.toEntityUpdate(quote, dto);
        quote = quoteRepository.save(quote);
        return quoteMapper.toResponse(quote);
    }

    @Override
    public void disableByAuthor(Author author) {
        this.quoteRepository.changeStatusByAuthor(author.getId(), QuoteStatus.DISABLE);
    }

    @Override
    public void enableByAuthor(Author author) {
        this.quoteRepository.changeStatusByAuthor(author.getId(), QuoteStatus.PUBLISHED);
    }

    @Override
    public void disableByTheme(Theme theme) {
        this.quoteRepository.changeStatusByTheme(theme.getId(), QuoteStatus.DISABLE);
    }

    @Override
    public void enableByTheme(Theme theme) {
        this.quoteRepository.changeStatusByTheme(theme.getId(), QuoteStatus.PUBLISHED);
    }

    private Quote findByIdOrSlug(String idOrSlug) {
        if (idOrSlug.matches("\\d+")) {
            return quoteRepository.findById(Integer.parseInt(idOrSlug))
                    .orElseThrow(() -> new EntityNotFoundException("Quote not found"));
        } else {
            return quoteRepository.findBySlug(idOrSlug)
                    .orElseThrow(() -> new EntityNotFoundException("Quote not found"));
        }
    }

}
