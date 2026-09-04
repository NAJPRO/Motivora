package com.audin.motivora.security;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.config.JwtProperties;
import com.audin.motivora.dto.response.SessionResponse;
import com.audin.motivora.dto.response.TokenPairResponse;
import com.audin.motivora.entity.Jwt;
import com.audin.motivora.entity.RefreshToken;
import com.audin.motivora.entity.User;
import com.audin.motivora.repository.JwtRepository;
import com.audin.motivora.security.device.DeviceContext;
import com.audin.motivora.service.AuthService;
import com.audin.motivora.utils.AuthUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final AuthService authService;
    private final JwtRepository jwtRepository;
    private final AuthUtil authUtil;
    private final JwtProperties jwtProperties;

    /**
     * Issues a token pair for one device.
     *
     * Only the sessions previously opened from the *same* device are revoked, so a user
     * can stay signed in on their phone and on the admin web front at the same time.
     */
    @Transactional
    public TokenPairResponse generateToken(String username, DeviceContext device) {
        User user = this.authService.findByEmail(username);

        this.revokeDeviceSessions(user, device);

        Instant now = Instant.now();
        RefreshToken refreshToken = RefreshToken.builder()
                .expire(false)
                .createdAt(now)
                .token(UUID.randomUUID().toString())
                .expireAt(now.plus(this.jwtProperties.getRefreshTokenTtl()))
                .build();

        String accessToken = this.buildAccessToken(user);

        Jwt jwt = Jwt.builder()
                .expire(false)
                .expireAt(now.plus(this.jwtProperties.getAccessTokenTtl()))
                .user(user)
                .refreshToken(refreshToken)
                .token(accessToken)
                .deviceId(device.deviceId())
                .deviceName(device.deviceName())
                .platform(device.platform())
                .build();

        this.jwtRepository.save(jwt);

        return TokenPairResponse.bearer(
                accessToken,
                refreshToken.getToken(),
                this.jwtProperties.getAccessTokenTtl().toSeconds());
    }

    /**
     * Rotates a refresh token: the presented one is revoked and a fresh pair is issued.
     *
     * Failures throw {@link SecurityException} so the client receives a 401 and its HTTP
     * interceptor can route the user to the login screen — a 400 would read as a generic
     * error and leave the app stuck.
     */
    @Transactional
    public TokenPairResponse refreshToken(String refreshTokenValue, DeviceContext device) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new SecurityException("Refresh token is missing");
        }

        Jwt jwt = this.jwtRepository.findByRefreshToken(refreshTokenValue)
                .orElseThrow(() -> this.onUnknownRefreshToken(refreshTokenValue));

        Instant expireAt = jwt.getRefreshToken().getExpireAt();
        if (expireAt == null || expireAt.isBefore(Instant.now())) {
            this.revoke(jwt);
            this.jwtRepository.save(jwt);
            throw new SecurityException("Refresh token has expired");
        }

        // Rotation: the presented token must not be usable twice.
        this.revoke(jwt);
        this.jwtRepository.save(jwt);

        DeviceContext sessionDevice = device.isIdentified()
                ? device
                : new DeviceContext(jwt.getDeviceId(), jwt.getDeviceName(), jwt.getPlatform());

        return this.generateToken(jwt.getUser().getEmail(), sessionDevice);
    }

    /** Revokes the session carrying this access token (the caller's own session). */
    @Transactional
    public void logout(String accessToken) {
        Jwt jwt = this.jwtRepository.findByToken(accessToken, false)
                .orElseThrow(() -> new SecurityException("Token invalide"));
        this.revoke(jwt);
        this.jwtRepository.save(jwt);
    }

    /** Revokes every session of the current user, on every device. */
    @Transactional
    public void logoutAllDevices() {
        this.disableTokens(this.authUtil.getCurrentUser());
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listSessions(User user, String currentAccessToken) {
        return this.jwtRepository.findAllByUserEmail(user.getEmail()).stream()
                .sorted(Comparator.comparing(Jwt::getId).reversed())
                .map(jwt -> new SessionResponse(
                        jwt.getId(),
                        jwt.getDeviceId(),
                        jwt.getDeviceName(),
                        jwt.getPlatform() != null ? jwt.getPlatform().name() : null,
                        jwt.getToken().equals(currentAccessToken),
                        jwt.getExpireAt(),
                        jwt.getCreatedAt()))
                .toList();
    }

    /** Revokes one of the current user's sessions ("sign out this device"). */
    @Transactional
    public void revokeSession(User user, Integer sessionId) {
        Jwt jwt = this.jwtRepository.findById(sessionId)
                .filter(session -> session.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Session not found"));
        this.revoke(jwt);
        this.jwtRepository.save(jwt);
    }

    public Jwt findByToken(String token) {
        return this.jwtRepository.findByToken(token, false)
                .orElseThrow(() -> new SecurityException("Token invalide"));
    }

    public String getUserName(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

    public Date getExpireDate(String token) {
        return this.getClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpire(String token) {
        return this.getClaim(token, Claims::getExpiration).before(new Date());
    }

    /** Revokes every session of a user. Used when privileges or account status change. */
    @Transactional
    public void disableTokens(User user) {
        List<Jwt> jwts = this.jwtRepository.findAllByUserEmail(user.getEmail());
        jwts.forEach(this::revoke);
        this.jwtRepository.saveAll(jwts);
    }

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void deleteExpiredTokens() {
        List<Jwt> obsolete = this.jwtRepository.findRevokedAndExpired(Instant.now());
        if (!obsolete.isEmpty()) {
            this.jwtRepository.deleteAll(obsolete);
        }
    }

    /**
     * A refresh token that matches no valid session was either already rotated or forged.
     * If it belongs to a known past session, every session of that user is revoked: this is
     * the standard replay response for a leaked refresh token.
     */
    private SecurityException onUnknownRefreshToken(String refreshTokenValue) {
        this.jwtRepository.findAnyByRefreshToken(refreshTokenValue).ifPresent(replayed -> {
            log.warn("Refresh token replay detected for user id {}", replayed.getUser().getId());
            this.disableTokens(replayed.getUser());
        });
        return new SecurityException("Refresh token is invalid");
    }

    private void revokeDeviceSessions(User user, DeviceContext device) {
        List<Jwt> sessions = device.isIdentified()
                ? this.jwtRepository.findAllByUserEmailAndDeviceId(user.getEmail(), device.deviceId())
                : this.jwtRepository.findAllByUserEmailWithoutDevice(user.getEmail());
        sessions.forEach(this::revoke);
        this.jwtRepository.saveAll(sessions);
    }

    private void revoke(Jwt jwt) {
        jwt.setExpire(true);
        jwt.setExpireAt(null);
        jwt.getRefreshToken().setExpire(true);
        jwt.getRefreshToken().setExpireAt(null);
    }

    private String buildAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(this.jwtProperties.getAccessTokenTtl());

        Map<String, String> claims = Map.of(
                "email", user.getEmail(),
                "role", user.getRole().getName().toString());

        return Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .subject(user.getEmail())
                .claims(claims)
                .signWith(this.getSignInKey())
                .compact();
    }

    private <T> T getClaim(String token, Function<Claims, T> function) {
        return function.apply(this.getAllClaims(token));
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.jwtProperties.getSecret()));
    }
}
