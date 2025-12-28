package com.eod.eod.domain.auth.application;

import com.eod.eod.domain.auth.model.RefreshToken;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import leehj050211.bsmOauth.BsmOauth;
import leehj050211.bsmOauth.dto.resource.BsmUserResource;
import leehj050211.bsmOauth.exception.BsmOAuthCodeNotFoundException;
import leehj050211.bsmOauth.exception.BsmOAuthInvalidClientException;
import leehj050211.bsmOauth.exception.BsmOAuthTokenNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final BsmOauth bsmOauth;

    // Refresh Token으로 Access/Refresh 토큰 재발급
    public RefreshTokenResult refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        tokenService.validateRefreshToken(refreshToken);

        // DB에서 Refresh Token 조회 및 만료 확인
        RefreshToken storedToken = tokenService.findRefreshToken(refreshToken);
        User user = storedToken.getUser();

        // 새로운 Access Token 및 Refresh Token 생성
        String newAccessToken = tokenService.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
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
        String accessToken = tokenService.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
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


    public void oauth(String authCode) throws IOException {
        try {
            String nickname = this.getUserNickname(authCode);
            log.debug("OAuth 사용자 nickname: {}", nickname);
        } catch (BsmOAuthCodeNotFoundException e) {
            log.warn("임시 인증코드를 찾을 수 없음: {}", authCode);
        } catch (BsmOAuthTokenNotFoundException e) {
            log.warn("유저 토큰을 찾을 수 없음");
        } catch (BsmOAuthInvalidClientException e) {
            log.error("클라이언트 ID 또는 시크릿이 잘못됨");
        }
    }

    private String getUserNickname(String authCode) throws IOException, BsmOAuthInvalidClientException, BsmOAuthCodeNotFoundException, BsmOAuthTokenNotFoundException {
        // 임시 인증코드를 유저 토큰으로 교환
        // 유저 토큰은 유저마다 고유하기에 따로 보관하여 일정시간마다 유저의 정보를 갱신하는 용도로도 사용할 수 있습니다
        String token = bsmOauth.getToken(authCode);
        // 토큰으로 유저의 정보를 가져옴
        BsmUserResource resource = bsmOauth.getResource(token);
        return resource.getNickname();
    }
}
