package com.audin.motivora.dto.response.common;

/**
 * Single shape for endpoints whose result is an acknowledgement.
 *
 * Replaces the raw {@code String} bodies and ad-hoc {@code Map} literals that used to be
 * returned side by side: a typed client cannot model four shapes for the same kind of call.
 */
public record MessageResponse(String message) {

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
