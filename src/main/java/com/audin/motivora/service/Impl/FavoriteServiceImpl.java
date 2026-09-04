package com.audin.motivora.service.Impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.dto.response.FavoriteResponse;
import com.audin.motivora.entity.Favorite;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.User;
import com.audin.motivora.mapper.FavoriteMapper;
import com.audin.motivora.repository.FavoriteRepository;
import com.audin.motivora.repository.QuoteRepository;
import com.audin.motivora.service.FavoriteService;
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
    @Transactional
    public boolean addFavorite(Integer quoteId) {
        User currentUser = this.authUtil.getCurrentUser();
        if (this.favoriteRepository.existsByQuoteIdAndUserId(quoteId, currentUser.getId())) {
            return false;
        }

        Quote quote = this.quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote does not exist"));

        this.favoriteRepository.save(
                Favorite.builder()
                        .user(currentUser)
                        .quote(quote)
                        .build());
        return true;
    }

    @Override
    @Transactional
    public void removeFavorite(Integer quoteId) {
        User currentUser = this.authUtil.getCurrentUser();
        this.favoriteRepository.deleteByQuoteIdAndUserId(quoteId, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteResponse getFavorite(Integer id) {
        User currentUser = this.authUtil.getCurrentUser();
        Favorite favorite = this.favoriteRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Favorite not found"));
        return this.favoriteMapper.toDto(favorite);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriteResponse> getAllFavorites(int page, int size) {
        User currentUser = this.authUtil.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return this.favoriteRepository.findAllByUserId(currentUser.getId(), pageable)
                .map(this.favoriteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFavorites() {
        return this.favoriteRepository.countByUserId(this.authUtil.getCurrentUser().getId());
    }
}
