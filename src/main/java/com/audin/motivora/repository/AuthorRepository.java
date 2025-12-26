package com.audin.motivora.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
    Optional<Author> findBySlug(String slug);
    Optional<Author> findById(Integer id);
}
