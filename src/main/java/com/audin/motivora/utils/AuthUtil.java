package com.audin.motivora.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.audin.motivora.entity.User;

@Component

public class AuthUtil {

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new IllegalStateException("No authenticated user found in SecurityContext");
    }
}
