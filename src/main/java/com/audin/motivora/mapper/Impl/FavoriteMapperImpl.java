package com.audin.motivora.mapper.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.audin.motivora.dto.response.FavoriteResponse;
import com.audin.motivora.entity.Favorite;
import com.audin.motivora.mapper.FavoriteMapper;

@Component
public class FavoriteMapperImpl implements FavoriteMapper{

    @Override
    public FavoriteResponse toDto(Favorite entity) {
        
        FavoriteResponse response = new FavoriteResponse(
            entity.getId(),
            entity.getUser().getId(),
            null // Assuming QuoteResponse mapping is handled elsewhere
        );
        return response;
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
