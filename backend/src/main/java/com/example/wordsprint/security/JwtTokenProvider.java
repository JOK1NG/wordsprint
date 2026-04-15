package com.example.wordsprint.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final Set<String> REJECTED_SECRETS = Set.of(
            "replace-with-your-secret-key-at-least-32-characters",
            "replace-with-your-secret",
            "changeme",
            "secret",
            "your-secret-key"
    );

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        validateSecret(jwtProperties.getSecret());
        this.secretKey = buildSecretKey(jwtProperties.getSecret());
    }

    private void validateSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret must not be empty. Please set a strong secret in application.yml or via environment variable.");
        }
        if (REJECTED_SECRETS.contains(secret.toLowerCase().trim())) {
            throw new IllegalStateException("jwt.secret is using a known placeholder value. Please set a strong, unique secret (at least 32 characters) in application.yml or via environment variable.");
        }
        if (secret.length() < 32) {
            log.warn("jwt.secret is shorter than 32 characters — consider using a longer secret for production.");
        }
    }

    public String generateToken(Long userId) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(jwtProperties.getExpireHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public long getExpireSeconds() {
        return jwtProperties.getExpireHours() * 3600;
    }

    private Claims parseClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey buildSecretKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (RuntimeException ex) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
