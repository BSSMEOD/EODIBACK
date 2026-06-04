package com.eod.eod.domain.auth.application;

import com.eod.eod.domain.auth.infrastructure.MobileAuthTokenRepository;
import com.eod.eod.domain.auth.model.MobileAuthToken;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
public class MobileAuthTokenService {

    private static final long ONE_TIME_TOKEN_TTL_MINUTES = 5;

    private final MobileAuthTokenRepository mobileAuthTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String create(User user, String accessToken, String refreshToken) {
        String oneTimeToken = generateToken();
        mobileAuthTokenRepository.save(new MobileAuthToken(
                oneTimeToken,
                user,
                accessToken,
                refreshToken,
                LocalDateTime.now().plusMinutes(ONE_TIME_TOKEN_TTL_MINUTES)
        ));
        return oneTimeToken;
    }

    public MobileAuthToken consume(String oneTimeToken) {
        MobileAuthToken token = mobileAuthTokenRepository.findByOneTimeToken(oneTimeToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 oneTimeToken입니다."));

        if (!token.isConsumable()) {
            throw new IllegalArgumentException("만료되었거나 이미 사용된 oneTimeToken입니다.");
        }

        token.consume();
        return token;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
