package com.audin.motivora.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.audin.motivora.enums.DevicePlatform;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A push destination: one row per app install that granted notification permission.
 *
 * The token is issued by the OS/Expo and rotates, so it is registered on every app start
 * and disabled when the provider reports it as no longer deliverable.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "device_tokens", uniqueConstraints = {
        @UniqueConstraint(name = "uq_device_token", columnNames = "token")
})
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(length = 100)
    private String deviceId;

    @Column(length = 120)
    private String deviceName;

    @Column(nullable = false)
    private boolean enabled;

    private LocalDateTime lastUsedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
