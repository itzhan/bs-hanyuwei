package com.xiangyongshe.swim_admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AuthUser user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getExpireMinutes() * 60);
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .subject(user.getUsername())
                .claims(Map.of("uid", user.getId()))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
