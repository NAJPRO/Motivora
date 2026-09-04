package com.audin.motivora.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.audin.motivora.entity.Jwt;
import com.audin.motivora.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Authenticates requests carrying a {@code Bearer} access token.
 *
 * A request without a token simply moves on: the authorization rules decide whether the
 * endpoint is public. A request with a token that is expired, revoked or tampered with is
 * rejected with a JSON 401 so a mobile HTTP interceptor can trigger its refresh flow.
 */
@Service
@Slf4j
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        try {
            if (this.jwtService.isTokenExpire(token)) {
                this.unauthorized(response, "TOKEN_EXPIRED", "Access token has expired");
                return;
            }

            String username = this.jwtService.getUserName(token);
            Jwt storedToken = this.jwtService.findByToken(token);

            if (username != null
                    && storedToken.getUser().getEmail().equals(username)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = this.authService.loadUserByUsername(username);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
            }
        } catch (Exception ex) {
            // Malformed / revoked / unknown token -> reject without leaking details.
            log.warn("JWT validation failed: {}", ex.getMessage());
            this.unauthorized(response, "TOKEN_INVALID", "Access token is invalid");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        this.objectMapper.writeValue(response.getWriter(), Map.of(
                "code", HttpServletResponse.SC_UNAUTHORIZED,
                "error", code,
                "message", message,
                "timestamp", LocalDateTime.now().toString()));
    }
}
