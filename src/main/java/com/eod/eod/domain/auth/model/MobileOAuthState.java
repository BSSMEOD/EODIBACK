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
@Table(name = "mobile_oauth_states")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MobileOAuthState {

    @Id
    @Column(name = "state", nullable = false, length = 128)
    private String state;

    @Column(name = "redirect_uri", nullable = false, length = 500)
    private String redirectUri;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public MobileOAuthState(String state, String redirectUri, LocalDateTime expiresAt) {
        this.state = state;
        this.redirectUri = redirectUri;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
