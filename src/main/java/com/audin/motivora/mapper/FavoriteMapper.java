package com.audin.motivora.mapper;

import java.util.List;

import com.audin.motivora.dto.request.FavoriteRequest;
import com.audin.motivora.dto.response.FavoriteResponse;
import com.audin.motivora.entity.Favorite;

public interface FavoriteMapper {
    FavoriteResponse toDto(Favorite entity);
    List<FavoriteResponse> toDto(List<Favorite> entities);
}
