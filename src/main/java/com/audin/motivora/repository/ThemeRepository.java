package com.audin.motivora.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.Theme;


@Repository
public interface ThemeRepository extends JpaRepository<Theme, Integer> {

    Optional<Theme> findBySlug(String slug);

    /** Public catalogue: disabled themes must not surface in the mobile app. */
    List<Theme> findByIsActiveTrueOrderByNameAsc();

    Optional<Theme> findByIdAndIsActiveTrue(Integer id);

    Optional<Theme> findBySlugAndIsActiveTrue(String slug);

    long countByIsActiveTrue();
}
