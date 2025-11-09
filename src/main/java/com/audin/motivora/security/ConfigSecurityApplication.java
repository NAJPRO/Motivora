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

import com.audin.motivora.exception.JwtAuthenticationEntryPoint;
import com.audin.motivora.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
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
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/test/**").permitAll()
                    .anyRequest().authenticated()
                )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
            // .exceptionHandling(ex -> ex
            //         .authenticationEntryPoint((request, response, authException) -> {
            //             response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            //         }))
            .sessionManagement(
                session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
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
