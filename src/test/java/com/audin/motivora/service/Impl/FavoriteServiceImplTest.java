package com.audin.motivora.service.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import org.springframework.data.domain.Pageable;

import com.audin.motivora.entity.Favorite;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.User;
import com.audin.motivora.mapper.FavoriteMapper;
import com.audin.motivora.repository.FavoriteRepository;
import com.audin.motivora.repository.QuoteRepository;
import com.audin.motivora.utils.AuthUtil;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock FavoriteMapper favoriteMapper;
    @Mock FavoriteRepository favoriteRepository;
    @Mock QuoteRepository quoteRepository;
    @Mock AuthUtil authUtil;

    @InjectMocks FavoriteServiceImpl service;

    private User user(int id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    @Test
    void removeFavoriteDeletesOnlyCurrentUsersRow() {
        when(authUtil.getCurrentUser()).thenReturn(user(5));

        service.removeFavorite(7);

        verify(favoriteRepository).deleteByQuoteIdAndUserId(7, 5);
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavoriteIsIdempotentWhenAlreadyFavorited() {
        when(authUtil.getCurrentUser()).thenReturn(user(5));
        when(favoriteRepository.existsByQuoteIdAndUserId(7, 5)).thenReturn(true);

        assertThat(service.addFavorite(7)).isFalse();

        verify(favoriteRepository, never()).save(any());
        verify(favoriteRepository, never()).deleteByQuoteIdAndUserId(anyInt(), anyInt());
    }

    @Test
    void addFavoriteSavesForCurrentUser() {
        User current = user(5);
        Quote quote = new Quote();
        quote.setId(7);
        when(authUtil.getCurrentUser()).thenReturn(current);
        when(favoriteRepository.existsByQuoteIdAndUserId(7, 5)).thenReturn(false);
        when(quoteRepository.findById(7)).thenReturn(Optional.of(quote));

        assertThat(service.addFavorite(7)).isTrue();

        ArgumentCaptor<Favorite> captor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(current);
        assertThat(captor.getValue().getQuote()).isSameAs(quote);
    }

    @Test
    void addFavoriteFailsWhenQuoteMissing() {
        when(authUtil.getCurrentUser()).thenReturn(user(5));
        when(favoriteRepository.existsByQuoteIdAndUserId(7, 5)).thenReturn(false);
        when(quoteRepository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addFavorite(7))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getAllFavoritesIsScopedToCurrentUserAndPaginated() {
        when(authUtil.getCurrentUser()).thenReturn(user(5));
        when(favoriteRepository.findAllByUserId(eq(5), any(Pageable.class))).thenReturn(Page.empty());

        service.getAllFavorites(0, 20);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(favoriteRepository).findAllByUserId(eq(5), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void getFavoriteFailsWhenNotOwnedByCurrentUser() {
        when(authUtil.getCurrentUser()).thenReturn(user(5));
        when(favoriteRepository.findByIdAndUserId(3, 5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFavorite(3))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
