package com.audin.motivora.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.Theme;
import java.util.List;


@Repository
public interface ThemeRepository extends JpaRepository<Theme, Integer> {
    Optional<Theme> findBySlug(String slug);
}
