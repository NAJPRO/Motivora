package com.audin.motivora.mapper.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.audin.motivora.dto.response.FavoriteResponse;
import com.audin.motivora.entity.Favorite;
import com.audin.motivora.mapper.FavoriteMapper;
import com.audin.motivora.mapper.QuoteMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FavoriteMapperImpl implements FavoriteMapper {

    private final QuoteMapper quoteMapper;

    /** The quote is flagged as favourited: a favourite row only exists for its own owner. */
    @Override
    public FavoriteResponse toDto(Favorite entity) {
        return new FavoriteResponse(
            entity.getId(),
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getQuote() != null ? quoteMapper.toResponse(entity.getQuote(), true) : null,
            entity.getCreatedAt()
        );
    }

    @Override
    public List<FavoriteResponse> toDto(List<Favorite> entities) {
        ArrayList<FavoriteResponse> responses = new ArrayList<>();
        for (Favorite entity : entities) {
            responses.add(this.toDto(entity));
        }
        return responses;
    }

}
