package com.audin.motivora.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.Favorite;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    boolean existsByQuoteIdAndUserId(Integer quoteId, Integer userId);

    void deleteByQuoteIdAndUserId(Integer quoteId, Integer userId);

    @EntityGraph(attributePaths = {"quote", "quote.author", "quote.theme"})
    List<Favorite> findAllByUserId(Integer userId);

    @EntityGraph(attributePaths = {"quote", "quote.author", "quote.theme"})
    Optional<Favorite> findByIdAndUserId(Integer id, Integer userId);

    @EntityGraph(attributePaths = {"quote", "quote.author", "quote.theme"})
    Page<Favorite> findAllByUserId(Integer userId, Pageable pageable);

    /**
     * Which of these quotes the user has favourited. One query for a whole page,
     * so the mobile list can render its filled/empty heart without a second round trip.
     */
    @Query("SELECT f.quote.id FROM Favorite f WHERE f.user.id = :userId AND f.quote.id IN :quoteIds")
    Set<Integer> findFavoritedQuoteIds(@Param("userId") Integer userId, @Param("quoteIds") Collection<Integer> quoteIds);

    long countByUserId(Integer userId);
}
