package com.eod.eod.domain.auth.application;

import com.eod.eod.domain.auth.infrastructure.MobileOAuthStateRepository;
import com.eod.eod.domain.auth.model.MobileOAuthState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MobileOAuthStateService {

    private static final long STATE_TTL_MINUTES = 5;

    private final MobileOAuthStateRepository mobileOAuthStateRepository;

    public void save(String state, String redirectUri) {
        validateRedirectUri(redirectUri);
        mobileOAuthStateRepository.save(new MobileOAuthState(
                state,
                redirectUri,
                LocalDateTime.now().plusMinutes(STATE_TTL_MINUTES)
        ));
    }

    public Optional<String> consumeRedirectUri(String state) {
        if (state == null || state.isBlank()) {
            return Optional.empty();
        }

        return mobileOAuthStateRepository.findById(state)
                .map(savedState -> {
                    mobileOAuthStateRepository.delete(savedState);
                    if (savedState.isExpired()) {
                        throw new IllegalArgumentException("만료된 모바일 OAuth state입니다.");
                    }
                    return savedState.getRedirectUri();
                });
    }

    private void validateRedirectUri(String redirectUri) {
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalArgumentException("모바일 redirectUri가 필요합니다.");
        }
        if (!redirectUri.startsWith("eodi://") && !redirectUri.startsWith("eodi-dev://")) {
            throw new IllegalArgumentException("허용되지 않은 모바일 redirectUri입니다.");
        }
    }
}
