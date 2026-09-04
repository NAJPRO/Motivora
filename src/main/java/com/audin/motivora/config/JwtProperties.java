package com.audin.motivora.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT lifetimes and signing key.
 *
 * Mobile clients cannot ask the user to re-enter their password every hour, so the
 * refresh token lives for weeks while the access token stays short-lived. Both are
 * configurable per environment instead of being compiled in.
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    /** Base64-encoded HMAC key, at least 256 bits. */
    private String secret;

    /** Access token lifetime. Short: it is replayed on every request. */
    private Duration accessTokenTtl = Duration.ofMinutes(30);

    /** Refresh token lifetime. Long: it keeps a mobile install signed in. */
    private Duration refreshTokenTtl = Duration.ofDays(60);
}
