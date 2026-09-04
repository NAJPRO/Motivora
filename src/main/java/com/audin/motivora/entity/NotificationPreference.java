package com.audin.motivora.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * When a user wants to hear from the app.
 *
 * {@code dailyQuoteHour} is a local hour paired with {@code timezone}: someone asking for
 * 08:00 wants 08:00 where they live, which is why the scheduler runs hourly and resolves
 * the local time per user rather than sending one global batch.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_preferences", uniqueConstraints = {
        @UniqueConstraint(name = "uq_notification_preference_user", columnNames = "user_id")
})
public class NotificationPreference {

    public static final int DEFAULT_HOUR = 8;
    public static final String DEFAULT_TIMEZONE = "UTC";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean dailyQuoteEnabled;

    @Column(nullable = false)
    private int dailyQuoteHour;

    @Column(nullable = false, length = 64)
    private String timezone;

    /** Guards against sending twice on the same local day. */
    private LocalDate lastSentOn;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
