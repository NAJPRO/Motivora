package com.audin.motivora.entity;

import com.audin.motivora.enums.DevicePlatform;

import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "jwts")
public class Jwt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;
    
    @ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE })
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "refreshToken_id")
    private RefreshToken refreshToken;

    @Column(columnDefinition = "TEXT")
    private String token;

    private boolean expire;

    private Instant expireAt;

    /**
     * Identifies the client installation this session belongs to (a mobile app
     * install, a browser). Lets several devices stay logged in simultaneously:
     * a new login only revokes the sessions of the same device.
     */
    @Column(length = 100)
    private String deviceId;

    @Column(length = 120)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DevicePlatform platform;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
