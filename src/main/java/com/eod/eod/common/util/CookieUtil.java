package com.eod.eod.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

public class CookieUtil {

    // Refresh Token용 Cookie 생성 (더 강한 보안)
    public static void addSecureCookie(HttpServletResponse response, String name, String value, int maxAge) {
        // SameSite=Strict로 더 강한 보안 적용
        response.addHeader("Set-Cookie",
            String.format("%s=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=Strict",
                name, value, maxAge));
    }

    // Cookie 조회
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
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
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }
}
