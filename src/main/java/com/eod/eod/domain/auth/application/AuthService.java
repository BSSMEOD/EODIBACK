package com.eod.eod.domain.auth.application;

import com.eod.eod.domain.auth.model.RefreshToken;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    // Refresh Token으로 Access/Refresh 토큰 재발급
    public RefreshTokenResult refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        tokenService.validateRefreshToken(refreshToken);

        // DB에서 Refresh Token 조회 및 만료 확인
        RefreshToken storedToken = tokenService.findRefreshToken(refreshToken);
        User user = storedToken.getUser();

        // 새로운 Access Token 및 Refresh Token 생성
        String newAccessToken = tokenService.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = tokenService.createRefreshToken(user.getId());

        // 기존 Refresh Token 삭제 후 새로운 Refresh Token 저장
        tokenService.deleteRefreshToken(storedToken);
        tokenService.saveOrUpdateRefreshToken(user, newRefreshToken);

        return new RefreshTokenResult(newAccessToken, newRefreshToken);
    }

    // 로그아웃 시 사용자의 Refresh Token 전체 삭제
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        tokenService.deleteAllRefreshTokensByUser(user);
    }

    // OAuth2 로그인 성공 시 토큰 발급
    public TokenPair issueTokensForOAuth2Login(User user) {
        String accessToken = tokenService.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenService.createRefreshToken(user.getId());

        // Refresh Token DB에 저장
        tokenService.saveOrUpdateRefreshToken(user, refreshToken);

        return new TokenPair(accessToken, refreshToken);
    }

    // Refresh Token 갱신 결과를 담는 내부 클래스
    public static class RefreshTokenResult {
        private final String accessToken;
        private final String refreshToken;

        public RefreshTokenResult(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    // 토큰 쌍을 담는 내부 클래스
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}
