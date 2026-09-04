package com.audin.motivora.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.NotificationPreference;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Integer> {

    Optional<NotificationPreference> findByUserId(Integer userId);

    /**
     * Candidates for the daily send: opted in, with an active account and at least one
     * live device token. The local-hour check happens in the scheduler, per timezone.
     */
    @Query("""
                SELECT p FROM NotificationPreference p
                JOIN FETCH p.user u
                WHERE p.dailyQuoteEnabled = true
                  AND u.status = com.audin.motivora.enums.UserStatus.ACTIVE
                  AND EXISTS (SELECT 1 FROM DeviceToken d WHERE d.user = u AND d.enabled = true)
            """)
    List<NotificationPreference> findDailyCandidates();

    void deleteByUserId(Integer userId);
}
