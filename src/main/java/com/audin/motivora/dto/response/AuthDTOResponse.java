package com.audin.motivora.dto.response;

/**
 * Authentication result.
 *
 * {@code token} and {@code data} are kept as aliases so the existing admin web front
 * keeps working; new clients should read {@code accessToken} / {@code refreshToken} /
 * {@code expiresIn}, which a mobile app needs to schedule its silent refresh.
 */
public record AuthDTOResponse(
        String token,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserDTOResponse data) {
}
