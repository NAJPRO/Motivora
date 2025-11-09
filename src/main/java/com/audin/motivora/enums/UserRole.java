package com.audin.motivora.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {

    USER("User"),
    ADMIN("Admin"),
    AUTHOR("Author");

    private final String label;
}
