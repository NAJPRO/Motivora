package com.audin.motivora.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Optional<Quote> findBySlug(String slug);

    @Query("""
                SELECT q FROM Quote q
                WHERE q.status = :status
                  AND q.theme.isActive = true
            """)
    Page<Quote> findAllPublishedQuotes(@Param("status") QuoteStatus status, Pageable pageable);

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

    List<Quote> findAllByStatus(QuoteStatus status);
}
