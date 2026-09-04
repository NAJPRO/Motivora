package com.audin.motivora.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.audin.motivora.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Integer id, Integer userId);

    long countByUserIdAndIsReadFalse(Integer userId);

    @Modifying
    @Query("""
                UPDATE Notification n
                SET n.isRead = true, n.readAt = :readAt
                WHERE n.user.id = :userId AND n.isRead = false
            """)
    int markAllAsRead(@Param("userId") Integer userId, @Param("readAt") LocalDateTime readAt);

    void deleteByUserId(Integer userId);
}
