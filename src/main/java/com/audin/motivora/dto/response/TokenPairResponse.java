package com.audin.motivora.dto.response;

/**
 * Token pair returned to the client.
 *
 * The refresh token is part of the JSON body because mobile clients have no usable
 * cookie jar; they store it in the OS keychain. Web clients keep receiving it in the
 * HttpOnly cookie as well and can simply ignore this field.
 */
public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn) {

    public static TokenPairResponse bearer(String accessToken, String refreshToken, long expiresInSeconds) {
        return new TokenPairResponse(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }
}
