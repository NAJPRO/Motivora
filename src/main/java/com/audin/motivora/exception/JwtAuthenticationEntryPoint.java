package com.audin.motivora.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(
                Map.of(
                        "code", 401,
                        "message", "Authentication failed: " + authException.getMessage(),
                        "timestamp", LocalDateTime.now().toString())));
    }
}
