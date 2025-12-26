package com.audin.motivora.service;

import java.util.List;

import com.audin.motivora.dto.response.FavoriteResponse;

public interface FavoriteService {
    void toogleFavoriteQuote(Integer quoteId);
    FavoriteResponse getFavorite(Integer id);
    List<FavoriteResponse> getAllFavorites();

}
