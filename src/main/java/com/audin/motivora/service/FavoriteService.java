package com.audin.motivora.service;

import org.springframework.data.domain.Page;

import com.audin.motivora.dto.response.FavoriteResponse;

public interface FavoriteService {

    /**
     * Adds the quote to the caller's favourites. Idempotent: calling it twice leaves the
     * quote favourited, unlike a toggle, where a double tap silently undoes the first one.
     *
     * @return true if the favourite was created, false if it was already there
     */
    boolean addFavorite(Integer quoteId);

    /** Removes the quote from the caller's favourites. Idempotent. */
    void removeFavorite(Integer quoteId);

    FavoriteResponse getFavorite(Integer id);

    Page<FavoriteResponse> getAllFavorites(int page, int size);

    long countFavorites();
}
