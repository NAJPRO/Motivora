package com.audin.motivora.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.audin.motivora.entity.User;

@Component
public class AuthUtil {

    public User getCurrentUser() {
        User user = this.getCurrentUserOrNull();
        if (user == null) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }
        return user;
    }

    /**
     * The authenticated user, or {@code null} on a public endpoint reached anonymously.
     * Lets public reads be enriched for signed-in callers (e.g. {@code isFavorite})
     * without forcing authentication.
     */
    public User getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getPrincipal() instanceof User user ? user : null;
    }
}
