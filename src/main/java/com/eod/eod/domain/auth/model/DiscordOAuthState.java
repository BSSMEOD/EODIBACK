package com.eod.eod.domain.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "discord_oauth_states")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscordOAuthState {

    @Id
    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "discord_id", nullable = false, length = 20)
    private String discordId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DiscordOAuthState(String state, String discordId, LocalDateTime expiresAt) {
        this.state = state;
        this.discordId = discordId;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }
}
