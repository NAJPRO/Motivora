package com.audin.motivora.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.audin.motivora.entity.listener.AuthorSlug;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
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
@EntityListeners(AuthorSlug.class)
@Table(name = "authors", indexes = {
    @Index(columnList = "slug")
})
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String slug;

    //@Lob
    //@Basic(fetch = FetchType.LAZY)
    private String bio;

    private String avatarUrl;

    @Column(nullable = false)
    private boolean isActive = true;


    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Quote> quotes;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
