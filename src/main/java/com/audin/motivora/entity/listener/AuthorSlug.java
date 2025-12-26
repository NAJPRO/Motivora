package com.audin.motivora.entity.listener;

import com.audin.motivora.entity.Author;
import com.audin.motivora.utils.Slug;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class AuthorSlug {
    @PrePersist
    public void generateSlug(Author entity) {
        if (entity.getSlug() == null && entity.getName() != null) {
            entity.setSlug(Slug.toSlug(entity.getName()));
        }
    }

    @PreUpdate
    public void updateSlug(Author entity) {
        if (entity.getName() != null &&
                !entity.getSlug().equals(Slug.toSlug(entity.getName()))) {
            entity.setSlug(Slug.toSlug(entity.getName()));
        }
    }
}
