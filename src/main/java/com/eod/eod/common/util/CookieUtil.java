package com.eod.eod.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private static final String COOKIE_PATH = "/";

    private final Environment environment;

    public enum SameSitePolicy {
        LAX("Lax"),
        NONE("None");

        private final String attribute;

        SameSitePolicy(String attribute) {
            this.attribute = attribute;
        }

        public String getAttribute() {
            return attribute;
        }
    }

    // Token Cookie 생성 (단위: ms)
    public void addTokenCookie(HttpServletResponse response, String name, String value, long maxAgeMillis) {
        addTokenCookie(response, name, value, maxAgeMillis, SameSitePolicy.LAX);
    }

    public void addTokenCookie(HttpServletResponse response, String name, String value,
                               long maxAgeMillis, SameSitePolicy sameSitePolicy) {
        ResponseCookie cookie = baseCookieBuilder(name, value, sameSitePolicy)
                .maxAge(Duration.ofMillis(maxAgeMillis))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // Cookie 조회
    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }

        return Optional.empty();
    }

    // Cookie 삭제
    public void deleteCookie(HttpServletResponse response, String name) {
        deleteCookie(response, name, SameSitePolicy.LAX);
    }

    public void deleteCookie(HttpServletResponse response, String name, SameSitePolicy sameSitePolicy) {
        ResponseCookie cookie = baseCookieBuilder(name, "", sameSitePolicy)
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private ResponseCookie.ResponseCookieBuilder baseCookieBuilder(String name, String value, SameSitePolicy sameSitePolicy) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                // SameSite=None requires Secure, otherwise respect environment defaults
                .secure(isProduction() || sameSitePolicy == SameSitePolicy.NONE)
                .path(COOKIE_PATH)
                .sameSite(sameSitePolicy.getAttribute());
    }

    private boolean isProduction() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod") || profile.equalsIgnoreCase("production"));
    }
}
