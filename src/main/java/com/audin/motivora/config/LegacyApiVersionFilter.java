package com.audin.motivora.config;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Keeps the pre-versioning URLs working: {@code /api/quotes} is handled as
 * {@code /api/v1/quotes}.
 *
 * The path is rewritten on a request wrapper rather than forwarded, so the security filter
 * chain and the MVC mapping both see the same, versioned path — a forward would skip the
 * security filters entirely.
 *
 * Transitional: remove once the admin front calls the versioned URLs.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LegacyApiVersionFilter extends OncePerRequestFilter {

    private static final String VERSION_PREFIX = "/" + ApiVersionConfig.CURRENT_VERSION;

    /** Paths served outside the versioned API. */
    private static final String[] UNVERSIONED_PREFIXES = {
            "/uploads/", "/actuator", "/swagger-ui", "/v3/api-docs", "/api-docs"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        if (this.needsRewrite(path)) {
            chain.doFilter(new VersionedRequest(request, VERSION_PREFIX + path), response);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean needsRewrite(String path) {
        if (path == null || path.isEmpty()
                || path.equals(VERSION_PREFIX)
                || path.startsWith(VERSION_PREFIX + "/")) {
            return false;
        }
        for (String prefix : UNVERSIONED_PREFIXES) {
            if (path.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    private static final class VersionedRequest extends HttpServletRequestWrapper {

        private final String rewrittenPath;

        private VersionedRequest(HttpServletRequest request, String rewrittenPath) {
            super(request);
            this.rewrittenPath = rewrittenPath;
        }

        @Override
        public String getServletPath() {
            return this.rewrittenPath;
        }

        @Override
        public String getRequestURI() {
            return this.getContextPath() + this.rewrittenPath;
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            url.append(this.getScheme()).append("://").append(this.getServerName());
            if (this.getServerPort() != 80 && this.getServerPort() != 443) {
                url.append(':').append(this.getServerPort());
            }
            return url.append(this.getRequestURI());
        }
    }
}
