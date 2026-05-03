package com.eod.eod.domain.auth.infrastructure;

import com.eod.eod.domain.auth.model.DiscordOAuthState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface DiscordOAuthStateRepository extends JpaRepository<DiscordOAuthState, String> {

    void deleteByExpiresAtBefore(LocalDateTime threshold);
}
