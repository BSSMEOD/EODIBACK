package com.eod.eod.domain.auth.application;

import com.eod.eod.common.jwt.JwtTokenProvider;
import com.eod.eod.domain.auth.infrastructure.RefreshTokenRepository;
import com.eod.eod.domain.auth.model.RefreshToken;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

// Access와 Refresh 토큰 발급과 저장/삭제를 담당하는 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    
    private static final int REFRESH_TOKEN_EXPIRATION_SECONDS = 604800; // 7일

    // Access Token 생성
    public String createAccessToken(Long userId, String email) {
        return jwtTokenProvider.createAccessToken(userId, email);
    }

    // Refresh Token 생성
    public String createRefreshToken(Long userId) {
        return jwtTokenProvider.createRefreshToken(userId);
    }

    // Refresh Token 검증
    public void validateRefreshToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        if (!"refresh".equals(jwtTokenProvider.getTokenType(token))) {
            throw new IllegalArgumentException("Refresh Token이 아닙니다.");
        }
    }

    // DB에서 Refresh Token 조회
    public RefreshToken findRefreshToken(String token) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("저장된 Refresh Token을 찾을 수 없습니다."));

        if (storedToken.isExpired()) {
            deleteRefreshToken(storedToken);
            throw new IllegalArgumentException("만료된 Refresh Token입니다.");
        }

        return storedToken;
    }

    // Refresh Token 저장 또는 업데이트
    @Transactional
    public void saveOrUpdateRefreshToken(User user, String token) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRATION_SECONDS);

        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByUser(user);

        if (existingTokenOpt.isPresent()) {
            // 기존 토큰 업데이트
            RefreshToken existingToken = existingTokenOpt.get();
            existingToken.updateToken(token, expiresAt);
        } else {
            // 새 토큰 생성
            RefreshToken newToken = RefreshToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(expiresAt)
                    .build();
            refreshTokenRepository.save(newToken);
        }
    }

    // 사용자의 Refresh Token 전체 삭제
    @Transactional
    public void deleteAllRefreshTokensByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    // 특정 Refresh Token 삭제
    @Transactional
    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }
    
    // Refresh Token 만료 시간(초) 반환
    public int getRefreshTokenExpirationSeconds() {
        return REFRESH_TOKEN_EXPIRATION_SECONDS;
    }
}
