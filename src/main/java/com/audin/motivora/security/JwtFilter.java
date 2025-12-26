package com.audin.motivora.security;


import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.audin.motivora.entity.Jwt;
import com.audin.motivora.service.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Allow preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        String username = null;
        UserDetails userDetails = null;
        boolean isTokenExpire = true;

        String path = request.getServletPath();
        log.info("Request path: {}", path);
        // Ignore public routes
        System.out.println(path);
        if (path.startsWith("/auth/") || path.startsWith("/test/") || path.startsWith("/reset-password/") || path.startsWith("/actuator")) {
            log.info("Public route accessed: {}", path);
            filterChain.doFilter(request, response);
            log.info("Filter chain finished");
            log.info("Filter chain finished with status: {}", response.getStatus());
            return;
        }

        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
            isTokenExpire = jwtService.isTokenExpire(token);
            username = this.jwtService.getUserName(token);

            if (isTokenExpire) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                return;
            }
        }
        System.out.println("TOKEN TROUVEEE :: : " + token);

        final Jwt jwtDB = this.jwtService.findByToken(token);
        if (!isTokenExpire &&
                username != null &&
                jwtDB.getUser().getEmail().equals(username) &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            userDetails = this.authService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        }

        filterChain.doFilter(request, response);
    }

}
