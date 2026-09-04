package com.audin.motivora.security;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.audin.motivora.config.ApiVersionConfig;
import com.audin.motivora.entity.ApiError;
import com.audin.motivora.security.ratelimit.RateLimitStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Throttles the sensitive unauthenticated endpoints — login, registration, password reset,
 * email verification — which are the ones worth brute-forcing or abusing to send mail.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // after LegacyApiVersionFilter, before the security chain
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitStore rateLimitStore;
    private final ObjectMapper objectMapper;

    @Value("${app.rate-limit.max-requests:10}")
    private int maxRequests;

    @Value("${app.rate-limit.window-seconds:60}")
    private long windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = this.stripVersion(request.getServletPath());
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !this.isRateLimited(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = this.clientIp(request) + ":" + this.group(path);
        if (this.rateLimitStore.isExceeded(key, this.maxRequests, Duration.ofSeconds(this.windowSeconds))) {
            this.tooManyRequests(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** Matching is done on the unversioned path, so /v1/auth/login and /auth/login behave alike. */
    private String stripVersion(String path) {
        if (path == null) {
            return "";
        }
        String versionPrefix = "/" + ApiVersionConfig.CURRENT_VERSION;
        return path.startsWith(versionPrefix + "/") ? path.substring(versionPrefix.length()) : path;
    }

    private boolean isRateLimited(String path) {
        return path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/reset-password/")
                || path.startsWith("/verify-email/");
    }

    private String group(String path) {
        if (path.startsWith("/reset-password/")) {
            return "reset";
        }
        return path.startsWith("/verify-email/") ? "verify" : "auth";
    }

    /**
     * Deliberately does not read {@code X-Forwarded-For}: a client can forge it and mint a
     * fresh bucket per request. Behind a proxy, {@code server.forward-headers-strategy}
     * makes Spring resolve the real client address here, having validated the hop itself.
     */
    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private void tooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        this.objectMapper.writeValue(response.getWriter(), ApiError.of(
                429, "RATE_LIMIT_EXCEEDED", "Too many requests. Please try again later."));
    }
}
