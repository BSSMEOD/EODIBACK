package com.eod.eod.domain.auth.application;

import com.eod.eod.domain.auth.infrastructure.DiscordOAuthStateRepository;
import com.eod.eod.domain.auth.model.DiscordOAuthState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordOAuthStateService {

    private static final long STATE_TTL_MINUTES = 10L;

    private final DiscordOAuthStateRepository discordOAuthStateRepository;

    @Transactional
    public void save(String state, String discordId) {
        LocalDateTime now = LocalDateTime.now();
        discordOAuthStateRepository.deleteByExpiresAtBefore(now);
        discordOAuthStateRepository.save(new DiscordOAuthState(state, discordId, now.plusMinutes(STATE_TTL_MINUTES)));
    }

    @Transactional
    public Optional<String> consumeDiscordId(String state) {
        if (state == null || state.isBlank()) {
            return Optional.empty();
        }

        Optional<DiscordOAuthState> savedState = discordOAuthStateRepository.findById(state);
        if (savedState.isEmpty()) {
            return Optional.empty();
        }

        DiscordOAuthState discordOAuthState = savedState.get();
        discordOAuthStateRepository.delete(discordOAuthState);

        if (discordOAuthState.isExpired(LocalDateTime.now())) {
            log.warn("Discord OAuth state 만료됨 (discordId={}). Discord 연결 없이 로그인 진행됩니다.",
                    discordOAuthState.getDiscordId());
            return Optional.empty();
        }

        return Optional.of(discordOAuthState.getDiscordId());
    }
}
