package com.audin.motivora.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.Favorite;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    //@Query("FROM Favorite f WHERE f.quote.id = :quoteId AND f.user.id = :userId")
    boolean existsByQuoteIdAndUserId(Integer quoteId, Integer userId);

    void deleteByQuoteId(Integer quoteId);
}
