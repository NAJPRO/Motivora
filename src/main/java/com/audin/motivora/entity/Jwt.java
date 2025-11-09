package com.audin.motivora.entity;

import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
