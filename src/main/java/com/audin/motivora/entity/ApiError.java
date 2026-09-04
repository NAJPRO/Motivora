package com.audin.motivora.entity;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Single error shape for the whole API.
 *
 * {@code error} is a stable machine code the client branches on; {@code message} is
 * human-readable and may change wording. {@code fieldErrors} carries validation failures,
 * so a mobile form can highlight the offending inputs instead of parsing a bare map.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private String message;
    private int code;
    private String error;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;

    public ApiError(String message, int code, LocalDateTime timestamp) {
        this.message = message;
        this.code = code;
        this.timestamp = timestamp;
    }

    public static ApiError of(int code, String error, String message) {
        ApiError apiError = new ApiError();
        apiError.setCode(code);
        apiError.setError(error);
        apiError.setMessage(message);
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }

    public static ApiError validation(String message, Map<String, String> fieldErrors) {
        ApiError apiError = ApiError.of(400, "VALIDATION_FAILED", message);
        apiError.setFieldErrors(fieldErrors);
        return apiError;
    }
}
