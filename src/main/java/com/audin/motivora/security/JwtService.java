package com.audin.motivora.security;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.entity.Jwt;
import com.audin.motivora.entity.RefreshToken;
import com.audin.motivora.entity.User;
import com.audin.motivora.exception.BusinessException;
import com.audin.motivora.repository.JwtRepository;
import com.audin.motivora.service.AuthService;
import com.audin.motivora.utils.AuthUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class JwtService {
    private final AuthService authService;

    private final String SECRET = "a13c4b5bb2bfcf4ea8134baec33b2fd7008cfb5765836469a3e3c6ff1cfa52ab";
    public static final long JWT_EXPIRATION = 30 * 60 * 1000; // 15 minutes
    public static final long REFRESH_TOKEN_EXPIRATION = 60 * 60 * 1000; // 15 minutes

    private static final String BEARER = "Bearer";
    private final JwtRepository jwtRepository;
    private final AuthUtil authUtil;

    public Map<String, String> generateToken(String username) {
        log.info("username received for token generation: {}", username);
        User user = this.authService.findByEmail(username);
        log.info("User found: {} | {}", user.getPseudo(), user.getEmail());
        final Map<String, String> token = new HashMap<>(this.generateJwt(user));
        log.info("Generated token map: {}", token);
        final String finalToken = token.get(BEARER);

        this.disableTokens(user);
        log.info("Disabled previous tokens for user: {}", user.getEmail());
        RefreshToken refreshToken = RefreshToken.builder()
                .expire(false)
                .createdAt(Instant.now())
                .token(UUID.randomUUID().toString())
                .expireAt(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION))
                .build();

        Jwt jwt = Jwt.builder()
                .expire(false)
                .expireAt(Instant.now().plusMillis(JWT_EXPIRATION))
                .user(user)
                .refreshToken(refreshToken)
                .token(finalToken)
                .build();

        this.jwtRepository.save(jwt);
        token.put("refresh", refreshToken.getToken());
        return token;
    }

    public Jwt findByToken(String token) {
        return this.jwtRepository.findByToken(token, false)
                .orElseThrow(() -> new SecurityException("Token invalide"));
    }

    public void logout() {
        User user = this.authUtil.getCurrentUser();
        // (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Jwt jwt = this.jwtRepository.findByUserValidToken(
                user.getEmail(), false)
                .orElseThrow(() -> new SecurityException("Token invalide"));
        jwt.setExpire(true);
        jwt.setExpireAt(null);
        jwt.getRefreshToken().setExpire(true);
        jwt.getRefreshToken().setExpireAt(null);
        this.jwtRepository.save(jwt);
    }

    public Map<String, String> refrechToken(String refreshTokenMap) {
        Jwt jwt = this.jwtRepository.findByRefrechToken(refreshTokenMap)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (jwt.getRefreshToken().getExpireAt().isBefore(Instant.now())) {
            throw new BusinessException("Token invalide");
        }
        this.disableTokens(jwt.getUser());
        return this.generateToken(jwt.getUser().getEmail());
    }

    public String getUserName(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

    public Date getExpireDate(String token) {
        return this.getClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpire(String token) {
        Date expirationDate = this.getClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    private <T> T getClaim(String token, Function<Claims, T> function) {
        final Claims claims = this.getAllClaims(token);
        return function.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    private Map<String, String> generateJwt(User user) {
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + JWT_EXPIRATION;

        final Map<String, String> claims = Map.of(
                "email", user.getEmail(),
                "role", user.getRole().getName().toString());

        final String bearer = Jwts.builder()
                .issuedAt(new Date(currentTime))
                .expiration(new Date(expirationTime))
                .subject(user.getEmail())
                .claims(claims)
                .signWith(getSignInKey())
                .compact();
        return Map.of(BEARER, bearer);
    }

    private SecretKey getSignInKey() {
        final byte[] key = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(key);
    }

    @Transactional
    public void disableTokens(User user) {
        final List<Jwt> jwts = this.jwtRepository.findAllByUserEmail(user.getEmail()); // retourne List<Jwt>
        jwts.forEach(jwt -> {
            jwt.setExpire(true);
            jwt.setExpireAt(null);
            jwt.getRefreshToken().setExpire(true);
            jwt.getRefreshToken().setExpireAt(null);
        });
        this.jwtRepository.saveAll(jwts);
    }

    // @Scheduled(fixedRate = 86400000) // every 24 hours
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void deleteExpiredTokens() {
        this.jwtRepository.deleteByExpire(true);
    }
}
