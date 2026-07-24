package com.servicehub.auth.infrastructure.security;

import com.servicehub.auth.domain.AuthenticatedUser;
import com.servicehub.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(
                properties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(properties.expiration());

        return Jwts.builder()
                .subject(user.userId().toString())
                .claim("tenantId", user.tenantId().toString())
                .claim("email", user.email())
                .claim("role", user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UUID userId = UUID.fromString(claims.getSubject());
        UUID tenantId = UUID.fromString(
                claims.get("tenantId", String.class)
        );
        String email = claims.get("email", String.class);
        UserRole role = UserRole.valueOf(
                claims.get("role", String.class)
        );

        return new AuthenticatedUser(
                userId,
                tenantId,
                email,
                role
        );
    }

    public long getExpirationSeconds() {
        return properties.expiration();
    }
}