package com.audin.motivora.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.entity.Author;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.enums.QuoteStatus;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE Quote q SET q.status = :status WHERE q.author.id = :id")
    int changeStatusByAuthor(@Param("id") Integer id, @Param("status") QuoteStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Quote q SET q.status = :status WHERE q.theme.id = :id")
    int changeStatusByTheme(@Param("id") Integer id, @Param("status") QuoteStatus status);

    @EntityGraph(attributePaths = {"author", "theme", "createdByUser"})
    Optional<Quote> findBySlug(String slug);

    @EntityGraph(attributePaths = {"author", "theme", "createdByUser"})
    @Query("""
                SELECT q FROM Quote q
                WHERE q.status = :status
                  AND q.theme.isActive = true
            """)
    Page<Quote> findAllPublishedQuotes(@Param("status") QuoteStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "theme", "createdByUser"})
    @Query("""
                SELECT q FROM Quote q
                WHERE q.author.id = :id
                  AND q.status = :status
                  AND q.theme.isActive = true
            """)
    Page<Quote> findAllQuotesByAuthor(
            @Param("id") Integer id,
            @Param("status") QuoteStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"author", "theme", "createdByUser"})
    @Query("""
                SELECT q FROM Quote q
                WHERE LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  AND q.status = :status
                  AND q.theme.isActive = true
            """)
    Page<Quote> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("status") QuoteStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"author", "theme", "createdByUser"})
    @Query("""
                SELECT q FROM Quote q
                WHERE q.theme.id = :themeId
                  AND q.status = :status
                  AND q.theme.isActive = true
            """)
    Page<Quote> findAllQuotesByTheme(
            @Param("themeId") Integer themeId,
            @Param("status") QuoteStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"author", "theme", "createdByUser"})
    @Query("""
                SELECT q FROM Quote q
                WHERE q.theme.slug = :slug
                  AND q.status = :status
                  AND q.theme.isActive = true
            """)
    Page<Quote> findAllQuotesByThemeSlug(
            @Param("slug") String slug,
            @Param("status") QuoteStatus status,
            Pageable pageable);

    @Query("""
                SELECT COUNT(q) FROM Quote q
                WHERE q.status = :status
                  AND q.theme.isActive = true
            """)
    long countPublished(@Param("status") QuoteStatus status);

    /**
     * Published quotes ordered by id. Used to pick the n-th one by offset — both for a
     * random quote and for the deterministic "quote of the day" — instead of
     * {@code ORDER BY RANDOM()}, which sorts the whole table on every call.
     */
    @EntityGraph(attributePaths = {"author", "theme", "createdByUser"})
    @Query("""
                SELECT q FROM Quote q
                WHERE q.status = :status
                  AND q.theme.isActive = true
                ORDER BY q.id ASC
            """)
    Page<Quote> findPublishedOrderedById(@Param("status") QuoteStatus status, Pageable pageable);

    @Query("""
                SELECT COUNT(q) FROM Quote q
                WHERE q.status = :status
                  AND q.theme.isActive = true
                  AND q.theme.id IN :themeIds
            """)
    long countPublishedInThemes(@Param("status") QuoteStatus status, @Param("themeIds") List<Integer> themeIds);

    @EntityGraph(attributePaths = {"author", "theme"})
    @Query("""
                SELECT q FROM Quote q
                WHERE q.status = :status
                  AND q.theme.isActive = true
                  AND q.theme.id IN :themeIds
                ORDER BY q.id ASC
            """)
    Page<Quote> findPublishedInThemesOrderedById(
            @Param("status") QuoteStatus status,
            @Param("themeIds") List<Integer> themeIds,
            Pageable pageable);

    // Admin moderation: every status, including inactive themes.
    @EntityGraph(attributePaths = {"author", "theme", "createdByUser"})
    @Query("SELECT q FROM Quote q")
    Page<Quote> findAllForAdmin(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "theme", "createdByUser"})
    @Query("SELECT q FROM Quote q WHERE q.status = :status")
    Page<Quote> findAllByStatusForAdmin(@Param("status") QuoteStatus status, Pageable pageable);

    long countByStatus(QuoteStatus status);
}
