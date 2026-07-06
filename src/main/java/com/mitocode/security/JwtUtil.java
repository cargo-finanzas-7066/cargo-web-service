package com.mitocode.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import com.mitocode.iam.persistence.entities.UserEntity;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;
    private final String issuer;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration,
                   @Value("${jwt.issuer}") String issuer) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET debe contener al menos 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.issuer = issuer;
    }

    public String generate(UserEntity user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer(issuer)
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Integer getUserId(String token) {
        return Integer.valueOf(parse(token).getSubject());
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer(issuer).build()
                .parseSignedClaims(token).getPayload();
    }
}
