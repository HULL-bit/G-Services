package com.gservices.api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Émission / vérification des jetons JWT (HS256) pour l'API REST mobile.
 * Le back-office JSF continue d'utiliser la session : seule l'API est stateless.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlMillis;

    public JwtService(
            @Value("${gservices.api.jwt-secret:g-services-cle-jwt-a-changer-en-production-32car-mini}") String secret,
            @Value("${gservices.api.jwt-ttl-hours:168}") long ttlHours) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.ttlMillis = ttlHours * 3_600_000L;
    }

    public String generer(String login) {
        Date now = new Date();
        return Jwts.builder()
                .subject(login)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMillis))
                .signWith(key)
                .compact();
    }

    /** @return le login (subject) si le jeton est valide, sinon lève une exception. */
    public String verifierEtLire(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public long ttlSecondes() {
        return ttlMillis / 1000;
    }
}
