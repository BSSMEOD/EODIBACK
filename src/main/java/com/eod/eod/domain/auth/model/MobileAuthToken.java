package com.eod.eod.domain.auth.model;

import com.eod.eod.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_auth_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MobileAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "one_time_token", nullable = false, unique = true, length = 128)
    private String oneTimeToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    public MobileAuthToken(String oneTimeToken, User user, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.oneTimeToken = oneTimeToken;
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public boolean isConsumable() {
        return consumedAt == null && LocalDateTime.now().isBefore(expiresAt);
    }

    public void consume() {
        this.consumedAt = LocalDateTime.now();
    }
}
