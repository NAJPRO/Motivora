package com.audin.motivora.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.audin.motivora.enums.QuoteStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "quotes")
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String slug;
    
    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "authorId", nullable = true)
    private Author author;

    @ManyToOne
    @JoinColumn(name = "createdByUserId", nullable = true)
    private User createdByUser;

    @ManyToOne
    @JoinColumn(name = "themeId", nullable = false)
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private QuoteStatus status = QuoteStatus.PUBLISHED;

    @Column(updatable = false)
    private LocalDateTime publishedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
