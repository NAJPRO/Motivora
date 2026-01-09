package com.audin.motivora.entity;

import java.util.List;

import com.audin.motivora.entity.listener.ThemeSlug;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@EntityListeners(ThemeSlug.class)
@Table(name = "themes", indexes = {
    @Index(columnList = "slug, name, isActive")
})
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String slug;
    @Column(nullable = false)
    private String name;
    private String description;
    private String color;
    private String imageUrl;
    private boolean isActive = true;

    @OneToMany(mappedBy = "theme", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Quote> quotes;
}
