package com.algoschool.auth.service;

import com.algoschool.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            Base64.getEncoder().encodeToString("a-test-secret-that-is-long-enough-for-hs256!".getBytes());

    private final UserDetails user = User.withUsername("student1")
            .password("x").authorities(List.of()).build();

    private JwtService service;

    private JwtService serviceWith(String secret, long expiration) {
        JwtProperties props = new JwtProperties();
        props.setSecret(secret);
        props.setExpiration(expiration);
        return new JwtService(props);
    }

    @BeforeEach
    void setUp() {
        service = serviceWith(SECRET, 60_000);
        service.initSignInKey();
    }

    @Test
    void signsAndReadsBackSubject() {
        String token = service.generateToken(user);
        assertThat(service.extractUsername(token)).isEqualTo("student1");
        assertThat(service.isTokenValid(token, user)).isTrue();
    }

    @Test
    void rejectsTokenIssuedForAnotherUser() {
        String token = service.generateToken(user);
        UserDetails other = User.withUsername("student2").password("x").authorities(List.of()).build();
        assertThat(service.isTokenValid(token, other)).isFalse();
    }

    /**
     * Просроченный токен отбраковывает сам парсер jjwt — он бросает
     * ExpiredJwtException ещё до того, как отработает проверка срока в
     * isTokenValid. Наружу это превращается в 401: JwtAuthenticationFilter
     * ловит JwtException (см. проверку матрицы доступа).
     */
    @Test
    void rejectsExpiredToken() {
        JwtService expiring = serviceWith(SECRET, -1_000); // выдаём уже истёкший
        expiring.initSignInKey();
        String token = expiring.generateToken(user);

        assertThatThrownBy(() -> expiring.isTokenValid(token, user))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String token = service.generateToken(user);
        JwtService foreign = serviceWith(
                Base64.getEncoder().encodeToString("a-completely-different-secret-key-32b!".getBytes()), 60_000);
        foreign.initSignInKey();
        assertThatThrownBy(() -> foreign.extractUsername(token)).isInstanceOf(RuntimeException.class);
    }

    /** Незаданная переменная окружения должна ронять старт с внятным текстом. */
    @Test
    void failsFastWhenSecretPlaceholderIsUnresolved() {
        JwtService broken = serviceWith("${JWT_SECRET}", 60_000);
        assertThatThrownBy(broken::initSignInKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void failsFastWhenSecretIsTooShortForHs256() {
        JwtService weak = serviceWith(Base64.getEncoder().encodeToString("short".getBytes()), 60_000);
        assertThatThrownBy(weak::initSignInKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256");
    }

    @Test
    void failsFastWhenSecretIsNotBase64() {
        JwtService bad = serviceWith("!!! не base64 !!!", 60_000);
        assertThatThrownBy(bad::initSignInKey).isInstanceOf(IllegalStateException.class);
    }
}
