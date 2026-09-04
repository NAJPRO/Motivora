package com.audin.motivora.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

    Optional<User> findByEmail(String email);

    @Query("""
                SELECT u FROM User u
                WHERE :search IS NULL
                   OR LOWER(u.pseudo) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<User> search(@Param("search") String search, Pageable pageable);
}
