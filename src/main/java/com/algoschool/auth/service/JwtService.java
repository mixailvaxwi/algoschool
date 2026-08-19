package com.algoschool.auth.service;

import com.algoschool.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private Key signInKey;

    /**
     * Ключ строится один раз на старте: любая проблема с секретом должна ронять
     * контекст здесь, с внятным сообщением, а не всплывать на первом логине.
     */
    @PostConstruct
    void initSignInKey() {
        String secret = jwtProperties.getSecret();

        // Binder у @ConfigurationProperties, в отличие от @Value, не падает на
        // неразрешённом плейсхолдере, а оставляет его текстом. Ловим это явно,
        // иначе ошибка выглядит как невнятный сбой Base64.
        if (secret.startsWith("${")) {
            throw new IllegalStateException(
                    "Переменная окружения JWT_SECRET не задана — приложению нечем подписывать токены. "
                            + "Сгенерируйте ключ: openssl rand -base64 32 (см. .env.example)");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (DecodingException e) {
            throw new IllegalStateException(
                    "JWT_SECRET должен быть строкой в Base64. Сгенерируйте ключ: openssl rand -base64 32", e);
        }

        try {
            this.signInKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (WeakKeyException e) {
            throw new IllegalStateException(
                    "JWT_SECRET слишком короткий: для HS256 нужно минимум 256 бит (32 байта до Base64). "
                            + "Сгенерируйте ключ: openssl rand -base64 32", e);
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtProperties.getExpiration()))
                .signWith(signInKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(signInKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
