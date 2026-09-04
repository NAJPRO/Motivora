package com.audin.motivora.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.audin.motivora.config.ApiVersionConfig;
import com.audin.motivora.exception.JwtAuthenticationEntryPoint;
import com.audin.motivora.service.AuthService;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class ConfigSecurityApplication {
    private final JwtFilter jwtFilter;
    private final ConfigEncodingPassword encodingPassword;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtAuthenticationEntryPoint entryPoint) throws Exception {

        httpSecurity
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for simplicity in this example
            .cors(Customizer.withDefaults())
            .logout(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Credentials exchange only. The rest of /auth (logout, sessions)
                    // acts on the caller's own session and requires a valid token.
                    .requestMatchers(HttpMethod.POST,
                            v("/auth/login"), v("/auth/register"), v("/auth/refresh-token"))
                        .permitAll()
                    .requestMatchers(HttpMethod.POST, v("/reset-password/**")).permitAll()
                    .requestMatchers(HttpMethod.POST, v("/verify-email/**")).permitAll()
                    .requestMatchers("/actuator", "/actuator/health", "/actuator/health/**", "/actuator/info")
                        .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/api-docs/**", "/swagger-ui/**").permitAll()
                    // Public catalogue: what the mobile app browses before signing in.
                    .requestMatchers(HttpMethod.GET, v("/quotes/**"), v("/themes/**"), v("/authors/**"))
                        .permitAll()
                    .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                    // Administration requires the ADMIN role
                    .requestMatchers(v("/admin/**")).hasRole("ADMIN")
                    .anyRequest().authenticated()
                )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    /**
     * Every controller is served under the version prefix (see {@code ApiVersionConfig}),
     * and {@code LegacyApiVersionFilter} rewrites unversioned URLs before this chain runs,
     * so matchers only need the versioned form.
     */
    private static String v(String path) {
        return "/" + ApiVersionConfig.CURRENT_VERSION + path;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(AuthService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(this.encodingPassword.passwordEncoder());
        return authProvider;
    }

}
