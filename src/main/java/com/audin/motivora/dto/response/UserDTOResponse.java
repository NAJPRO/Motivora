package com.audin.motivora.dto.response;

import java.time.LocalDateTime;

import com.audin.motivora.enums.UserStatus;

public record UserDTOResponse(
    Integer id,
    String pseudo,
    String email,
    String avatarUrl,
    UserStatus status,
    String role,
    boolean emailVerified,
    LocalDateTime emailVerifiedAt,
    LocalDateTime createdAt
) {}
