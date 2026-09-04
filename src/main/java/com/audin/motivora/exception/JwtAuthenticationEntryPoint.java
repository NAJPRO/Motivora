package com.audin.motivora.exception;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.audin.motivora.entity.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Emits the same {@link ApiError} shape as {@code CustomExceptionHandler} when a protected
 * endpoint is reached without credentials, so a client has one error contract to model.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        this.objectMapper.writeValue(response.getWriter(), ApiError.of(
                HttpServletResponse.SC_UNAUTHORIZED,
                "AUTHENTICATION_REQUIRED",
                "Authentication is required to access this resource"));
    }
}
