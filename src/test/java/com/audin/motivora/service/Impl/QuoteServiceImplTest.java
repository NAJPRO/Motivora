package com.audin.motivora.service.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
import com.audin.motivora.utils.AuthUtil;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class QuoteServiceImplTest {

    @Mock QuoteMapper quoteMapper;
    @Mock QuoteRepository quoteRepository;
    @Mock AuthorRepository authorRepository;
    @Mock ThemeRepository themeRepository;
    @Mock FavoriteRepository favoriteRepository;
    @Mock AuthUtil authUtil;

    @InjectMocks QuoteServiceImpl service;

    private QuoteRequest request(int authorId, int themeId) {
        QuoteRequest req = new QuoteRequest();
        req.setContent("content");
        req.setAuthorId(authorId);
        req.setThemeId(themeId);
        return req;
    }

    @Test
    void saveResolvesAuthorThemeAndCreatorFromPrincipal() {
        QuoteRequest req = request(1, 2);
        Quote entity = new Quote();
        Author author = new Author();
        author.setId(1);
        Theme theme = new Theme();
        theme.setId(2);
        User current = new User();
        current.setId(3);

        when(quoteMapper.toEntity(req)).thenReturn(entity);
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(themeRepository.findById(2)).thenReturn(Optional.of(theme));
        when(authUtil.getCurrentUser()).thenReturn(current);
        when(quoteRepository.save(entity)).thenReturn(entity);
        when(quoteMapper.toAdminResponse(entity))
                .thenReturn(new QuoteResponse(1, "s", "content", null, null, "PUBLISHED",
                        false, null, null, null));

        service.save(req);

        assertThat(entity.getAuthor()).isSameAs(author);
        assertThat(entity.getTheme()).isSameAs(theme);
        assertThat(entity.getCreatedByUser()).isSameAs(current);
        verify(quoteRepository).save(entity);
    }

    @Test
    void saveFailsWhenAuthorMissing() {
        QuoteRequest req = request(9, 2);
        when(quoteMapper.toEntity(req)).thenReturn(new Quote());
        when(authorRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void disablePublishedQuoteSetsDisabled() {
        Quote quote = new Quote();
        quote.setStatus(QuoteStatus.PUBLISHED);
        when(quoteRepository.findById(1)).thenReturn(Optional.of(quote));

        service.disable("1");

        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.DISABLE);
        verify(quoteRepository).save(quote);
    }

    @Test
    void disableAlreadyDisabledThrowsConflict() {
        Quote quote = new Quote();
        quote.setStatus(QuoteStatus.DISABLE);
        when(quoteRepository.findById(1)).thenReturn(Optional.of(quote));

        assertThatThrownBy(() -> service.disable("1"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getRandomQuoteThrowsWhenNoneAvailable() {
        when(quoteRepository.countPublished(QuoteStatus.PUBLISHED)).thenReturn(0L);

        assertThatThrownBy(() -> service.getRandomQuote())
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getQuoteOfTheDayThrowsWhenNoneAvailable() {
        when(quoteRepository.countPublished(QuoteStatus.PUBLISHED)).thenReturn(0L);

        assertThatThrownBy(() -> service.getQuoteOfTheDay())
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getQuoteOfTheDayIsStableForAGivenDay() {
        Quote quote = new Quote();
        quote.setId(42);
        when(quoteRepository.countPublished(QuoteStatus.PUBLISHED)).thenReturn(10L);
        when(quoteRepository.findPublishedOrderedById(eq(QuoteStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(quote)));

        ArgumentCaptor<Pageable> first = ArgumentCaptor.forClass(Pageable.class);
        service.getQuoteOfTheDay();
        verify(quoteRepository).findPublishedOrderedById(eq(QuoteStatus.PUBLISHED), first.capture());

        int offset = first.getValue().getPageNumber();
        service.getQuoteOfTheDay();

        ArgumentCaptor<Pageable> both = ArgumentCaptor.forClass(Pageable.class);
        verify(quoteRepository, times(2)).findPublishedOrderedById(eq(QuoteStatus.PUBLISHED), both.capture());
        assertThat(both.getAllValues()).allMatch(page -> page.getPageNumber() == offset);
    }

    @Test
    void adminListUsesTheAdminShapeSoTheSubmitterIsIncluded() {
        Quote quote = new Quote();
        when(quoteRepository.findAllForAdmin(any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(quote)));

        service.getAllQuotesForAdmin(null, 0, 20);

        verify(quoteMapper).toAdminResponse(quote);
    }

    @Test
    void adminListWithoutStatusReturnsAllStatuses() {
        when(quoteRepository.findAllForAdmin(any(Pageable.class))).thenReturn(Page.<Quote>empty());

        service.getAllQuotesForAdmin(null, 0, 20);

        verify(quoteRepository).findAllForAdmin(any(Pageable.class));
        verify(quoteRepository, never()).findAllByStatusForAdmin(any(), any());
    }

    @Test
    void adminListMapsStatusTokenToEnumFilter() {
        when(quoteRepository.findAllByStatusForAdmin(eq(QuoteStatus.DISABLE), any(Pageable.class)))
                .thenReturn(Page.<Quote>empty());

        service.getAllQuotesForAdmin("DISABLED", 0, 20);

        verify(quoteRepository).findAllByStatusForAdmin(eq(QuoteStatus.DISABLE), any(Pageable.class));
        verify(quoteRepository, never()).findAllForAdmin(any());
    }
}
