package com.eod.eod.domain.item.model;

import com.eod.eod.domain.user.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "item_claims")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User claimant;

    @Column(name = "claim_reason", nullable = false, columnDefinition = "TEXT")
    private String claimReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClaimStatus status;

    @Column(name = "claimed_at", nullable = false, updatable = false)
    private LocalDateTime claimedAt;

    @Builder
    public ItemClaim(Item item, User claimant, String claimReason) {
        this.item = item;
        this.claimant = claimant;
        this.claimReason = claimReason;
        this.status = ClaimStatus.PENDING;
        this.claimedAt = LocalDateTime.now();
    }

    /**
     * 소유권 주장 승인
     */
    public void approve() {
        if (this.status != ClaimStatus.PENDING) {
            throw new IllegalStateException("대기 중인 소유권 주장만 승인할 수 있습니다.");
        }
        this.status = ClaimStatus.APPROVED;
    }

    /**
     * 소유권 주장 거절
     */
    public void reject() {
        if (this.status != ClaimStatus.PENDING) {
            throw new IllegalStateException("대기 중인 소유권 주장만 거절할 수 있습니다.");
        }
        this.status = ClaimStatus.REJECTED;
    }

    public enum ClaimStatus {
        PENDING, APPROVED, REJECTED
    }
}