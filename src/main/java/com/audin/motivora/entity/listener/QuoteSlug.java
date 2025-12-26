package com.audin.motivora.entity.listener;

import com.audin.motivora.entity.Quote;
import com.audin.motivora.utils.Slug;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class QuoteSlug {
 @PrePersist
    public void generateSlug(Quote entity) {
        if (entity.getSlug() == null && entity.getContent() != null) {
            entity.setSlug(Slug.toSlug(entity.getContent()));
        }
    }

    @PreUpdate
    public void updateSlug(Quote entity) {
        if (entity.getContent() != null &&
                !entity.getSlug().equals(Slug.toSlug(entity.getContent()))) {
            entity.setSlug(Slug.toSlug(entity.getContent()));
        }
    }
}
