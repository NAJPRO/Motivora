package com.audin.motivora.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.audin.motivora.dto.response.FavoriteResponse;
import com.audin.motivora.entity.Favorite;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.User;
import com.audin.motivora.mapper.FavoriteMapper;
import com.audin.motivora.repository.FavoriteRepository;
import com.audin.motivora.repository.QuoteRepository;
import com.audin.motivora.service.FavoriteService;
import com.audin.motivora.service.QuoteService;
import com.audin.motivora.utils.AuthUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final FavoriteRepository favoriteRepository;
    private final QuoteRepository quoteRepository;
    private final AuthUtil authUtil;

    @Override
    public void toogleFavoriteQuote(Integer quoteId) {
        User currentUser = authUtil.getCurrentUser();
        if (favoriteRepository.existsByQuoteIdAndUserId(quoteId, currentUser.getId())) {
            favoriteRepository.deleteByQuoteId(quoteId);
        } else {
            Quote quote = quoteRepository.findById(quoteId)
                    .orElseThrow(() -> new EntityNotFoundException("Quote does not exist"));
            favoriteRepository.save(
                    Favorite.builder()
                            .user(currentUser)
                            .quote(quote)
                            .build());
        }
    }

    @Override
    public FavoriteResponse getFavorite(Integer id) {
        Favorite favorite = favoriteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Favorite not exist"));
        return favoriteMapper.toDto(favorite);
    }

    @Override
    public List<FavoriteResponse> getAllFavorites() {
        return this.favoriteMapper.toDto(this.favoriteRepository.findAll());
    }

}
