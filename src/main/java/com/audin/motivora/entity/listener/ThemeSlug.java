package com.audin.motivora.entity.listener;

import com.audin.motivora.entity.Theme;
import com.audin.motivora.utils.Slug;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class ThemeSlug {
 @PrePersist
    public void generateSlug(Theme entity) {
        if (entity.getSlug() == null && entity.getName() != null) {
            entity.setSlug(Slug.toSlug(entity.getName()));
        }
    }

    @PreUpdate
    public void updateSlug(Theme entity) {
        System.out.println("SLUG BEFORE UPDATE : " + entity.getSlug());
        System.out.println("NAME BEFORE UPDATE : " + entity.getName());

        if (entity.getName() != null &&
                !entity.getSlug().equals(Slug.toSlug(entity.getName()))) {
            entity.setSlug(Slug.toSlug(entity.getName()));
        }
        System.out.println("SLUG AFTER UPDATE : " + entity.getSlug());
        System.out.println("NAME BEFORE UPDATE : " + entity.getName());


    }
}
