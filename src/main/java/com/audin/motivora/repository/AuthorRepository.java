package com.audin.motivora.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {

    Optional<Author> findBySlug(@Param("slug") String slug);

    /** Public catalogue: disabled authors must not surface in the mobile app. */
    Page<Author> findByIsActiveTrue(Pageable pageable);

    Optional<Author> findByIdAndIsActiveTrue(Integer id);

    Optional<Author> findBySlugAndIsActiveTrue(String slug);
}
