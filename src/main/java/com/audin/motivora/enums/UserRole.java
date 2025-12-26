package com.audin.motivora.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {

    USER("User"),
    ADMIN("Admin"),
    MODERATOR("Moderator"),
    CREATOR("Creator");

    private final String label;
}
