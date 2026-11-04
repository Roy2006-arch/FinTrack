package com.fintrack.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMinutes;
    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expirationMinutes = expirationMinutes;
    }
    public Instant expiry() { return Instant.now().plusSeconds(expirationMinutes * 60); }
    public String issue(Long uid, String sessionId, Instant expiresAt) {
        return Jwts.builder().issuer("fintrack").subject(uid.toString()).id(sessionId).issuedAt(new Date()).expiration(Date.from(expiresAt)).signWith(key).compact();
    }
    public CurrentUser parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).requireIssuer("fintrack").build().parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null || claims.getId() == null) throw new IllegalArgumentException("Invalid token claims");
        return new CurrentUser(Long.valueOf(claims.getSubject()), claims.getId());
    }
}
