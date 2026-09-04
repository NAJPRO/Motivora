package com.audin.motivora.service.Impl;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.dto.request.QuoteRequest;
import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.entity.Author;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.Theme;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.QuoteStatus;
import com.audin.motivora.mapper.QuoteMapper;
import com.audin.motivora.repository.AuthorRepository;
import com.audin.motivora.repository.FavoriteRepository;
import com.audin.motivora.repository.QuoteRepository;
import com.audin.motivora.repository.ThemeRepository;
import com.audin.motivora.service.QuoteService;
import com.audin.motivora.utils.AuthUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuoteServiceImpl implements QuoteService {

    private final QuoteMapper quoteMapper;
    private final QuoteRepository quoteRepository;
    private final AuthorRepository authorRepository;
    private final ThemeRepository themeRepository;
    private final FavoriteRepository favoriteRepository;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public void disable(String idOrSlug) {
        Quote quote = this.findByIdOrSlug(idOrSlug);
        if (!quote.getStatus().equals(QuoteStatus.PUBLISHED)) {
            throw new IllegalStateException("Quote is already disabled");
        }
        quote.setStatus(QuoteStatus.DISABLE);
        this.quoteRepository.save(quote);
    }

    @Override
    @Transactional
    public void enable(String idOrSlug) {
        Quote quote = this.findByIdOrSlug(idOrSlug);
        if (quote.getStatus().equals(QuoteStatus.PUBLISHED)) {
            throw new IllegalStateException("Quote is already enabled");
        }
        quote.setStatus(QuoteStatus.PUBLISHED);
        this.quoteRepository.save(quote);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getAllQuotes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return this.withFavorites(
                this.quoteRepository.findAllPublishedQuotes(QuoteStatus.PUBLISHED, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getAllQuotesForAdmin(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        QuoteStatus filter = this.parseStatus(status);
        Page<Quote> quotes = (filter == null)
                ? this.quoteRepository.findAllForAdmin(pageable)
                : this.quoteRepository.findAllByStatusForAdmin(filter, pageable);
        return quotes.map(this.quoteMapper::toAdminResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteResponse getQuote(String idOrSlug) {
        Quote quote = this.findByIdOrSlug(idOrSlug);
        return this.quoteMapper.toResponse(quote, this.isFavorite(quote));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesByAuthor(Integer authorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return this.withFavorites(
                this.quoteRepository.findAllQuotesByAuthor(authorId, QuoteStatus.PUBLISHED, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesByTheme(String themeIdOrSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Quote> quotes = themeIdOrSlug.matches("\\d+")
                ? this.quoteRepository.findAllQuotesByTheme(
                        Integer.parseInt(themeIdOrSlug), QuoteStatus.PUBLISHED, pageable)
                : this.quoteRepository.findAllQuotesByThemeSlug(
                        themeIdOrSlug, QuoteStatus.PUBLISHED, pageable);
        return this.withFavorites(quotes);
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteResponse getRandomQuote() {
        long total = this.quoteRepository.countPublished(QuoteStatus.PUBLISHED);
        if (total == 0) {
            throw new EntityNotFoundException("No quotes available");
        }
        int offset = (int) ThreadLocalRandom.current().nextLong(total);
        return this.pickAtOffset(offset);
    }

    /**
     * The quote of the day is derived from the current UTC date, so every client sees the
     * same one all day long and a pull-to-refresh does not swap it.
     */
    @Override
    @Transactional(readOnly = true)
    public QuoteResponse getQuoteOfTheDay() {
        long total = this.quoteRepository.countPublished(QuoteStatus.PUBLISHED);
        if (total == 0) {
            throw new EntityNotFoundException("No quotes available");
        }
        long dayNumber = LocalDate.now(ZoneOffset.UTC).toEpochDay();
        return this.pickAtOffset((int) Math.floorMod(dayNumber, total));
    }

    @Override
    @Transactional
    public QuoteResponse save(QuoteRequest dto) {
        Quote quote = this.quoteMapper.toEntity(dto);
        quote.setAuthor(this.resolveAuthor(dto.getAuthorId()));
        quote.setTheme(this.resolveTheme(dto.getThemeId()));
        quote.setCreatedByUser(this.authUtil.getCurrentUser());
        quote = this.quoteRepository.save(quote);
        return this.quoteMapper.toAdminResponse(quote);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuoteResponse> searchByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return this.withFavorites(
                this.quoteRepository.searchByKeyword(keyword, QuoteStatus.PUBLISHED, pageable));
    }

    @Override
    @Transactional
    public QuoteResponse update(String idOrSlug, QuoteRequest dto) {
        Quote quote = this.findByIdOrSlug(idOrSlug);
        quote = this.quoteMapper.toEntityUpdate(quote, dto);
        quote.setAuthor(this.resolveAuthor(dto.getAuthorId()));
        quote.setTheme(this.resolveTheme(dto.getThemeId()));
        quote = this.quoteRepository.save(quote);
        return this.quoteMapper.toAdminResponse(quote);
    }

    @Override
    @Transactional
    public void disableByAuthor(Author author) {
        this.quoteRepository.changeStatusByAuthor(author.getId(), QuoteStatus.DISABLE);
    }

    @Override
    @Transactional
    public void enableByAuthor(Author author) {
        this.quoteRepository.changeStatusByAuthor(author.getId(), QuoteStatus.PUBLISHED);
    }

    @Override
    @Transactional
    public void disableByTheme(Theme theme) {
        this.quoteRepository.changeStatusByTheme(theme.getId(), QuoteStatus.DISABLE);
    }

    @Override
    @Transactional
    public void enableByTheme(Theme theme) {
        this.quoteRepository.changeStatusByTheme(theme.getId(), QuoteStatus.PUBLISHED);
    }

    private QuoteResponse pickAtOffset(int offset) {
        Quote quote = this.quoteRepository
                .findPublishedOrderedById(QuoteStatus.PUBLISHED, PageRequest.of(offset, 1))
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No quotes available"));
        return this.quoteMapper.toResponse(quote, this.isFavorite(quote));
    }

    /**
     * Maps a page of quotes, flagging the caller's favourites in a single extra query.
     * Anonymous callers skip the query entirely.
     */
    private Page<QuoteResponse> withFavorites(Page<Quote> quotes) {
        User currentUser = this.authUtil.getCurrentUserOrNull();
        if (currentUser == null || quotes.isEmpty()) {
            return quotes.map(this.quoteMapper::toResponse);
        }

        List<Integer> quoteIds = quotes.getContent().stream().map(Quote::getId).toList();
        Set<Integer> favorited = this.favoriteRepository.findFavoritedQuoteIds(currentUser.getId(), quoteIds);

        return quotes.map(quote -> this.quoteMapper.toResponse(quote, favorited.contains(quote.getId())));
    }

    private boolean isFavorite(Quote quote) {
        User currentUser = this.authUtil.getCurrentUserOrNull();
        return currentUser != null
                && this.favoriteRepository.existsByQuoteIdAndUserId(quote.getId(), currentUser.getId());
    }

    /**
     * Maps the (optional, front-facing) status token to a {@link QuoteStatus}.
     * Accepts synonyms used by the admin UI. Blank/unknown -> {@code null} = no filter.
     */
    private QuoteStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return switch (status.trim().toUpperCase()) {
            case "PUBLISHED", "ACTIVE" -> QuoteStatus.PUBLISHED;
            case "PENDING", "REVIEW_PENDING" -> QuoteStatus.REVIEW_PENDING;
            case "DISABLED", "DISABLE", "INACTIVE" -> QuoteStatus.DISABLE;
            case "ARCHIVED" -> QuoteStatus.ARCHIVED;
            case "DRAFT" -> QuoteStatus.DRAFT;
            case "REJECTED" -> QuoteStatus.REJECTED;
            case "DELETED" -> QuoteStatus.DELETED;
            default -> null;
        };
    }

    private Author resolveAuthor(Integer authorId) {
        return this.authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author not found"));
    }

    private Theme resolveTheme(Integer themeId) {
        return this.themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("Theme not found"));
    }

    private Quote findByIdOrSlug(String idOrSlug) {
        if (idOrSlug.matches("\\d+")) {
            return this.quoteRepository.findById(Integer.parseInt(idOrSlug))
                    .orElseThrow(() -> new EntityNotFoundException("Quote not found"));
        }
        return this.quoteRepository.findBySlug(idOrSlug)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found"));
    }
}
