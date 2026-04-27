package com.eod.eod.common.jwt;

import com.eod.eod.domain.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "test-jwt-secret-key-for-testing-purposes-only-minimum-32-characters";

    @Test
    void accessToken에_사용자_이름과_studentCode를_담는다() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtProperties());
        User user = User.builder()
                .grade(1)
                .classNo(2)
                .studentNo(3)
                .oauthProvider("bsm")
                .oauthId("123456")
                .name("홍길동")
                .email("hong@test.com")
                .role(User.Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        String token = jwtTokenProvider.createAccessToken(user);

        Claims claims = parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("email", String.class)).isEqualTo("hong@test.com");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.get("type", String.class)).isEqualTo("access");
        assertThat(claims.get("name", String.class)).isEqualTo("홍길동");
        assertThat(claims.get("studentCode", Integer.class)).isEqualTo(1203);
        assertThat(claims.get("grade")).isNull();
        assertThat(claims.get("classNo")).isNull();
        assertThat(claims.get("studentNo")).isNull();
    }

    private JwtProperties jwtProperties() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setAccessTokenExpiration(Duration.ofHours(1));
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));
        return jwtProperties;
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
