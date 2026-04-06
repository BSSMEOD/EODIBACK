package com.eod.eod.domain.item.model;

import com.eod.eod.domain.item.exception.ItemBadRequestException;
import com.eod.eod.domain.item.exception.ItemConflictException;
import com.eod.eod.domain.user.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClaimStatus status;

    @Column(name = "claimed_at", nullable = false, updatable = false)
    private LocalDateTime claimedAt;

    @Builder
    public ItemClaim(Item item, User claimant, LocalDate visitDate) {
        validateVisitDate(visitDate);
        this.item = item;
        this.claimant = claimant;
        this.visitDate = visitDate;
        this.status = ClaimStatus.PENDING;
        this.claimedAt = LocalDateTime.now();
    }

    /**
     * 소유권 주장 승인
     */
    public void approve() {
        if (this.status != ClaimStatus.PENDING) {
            throw new ItemConflictException("대기 중인 소유권 주장만 승인할 수 있습니다.");
        }
        this.status = ClaimStatus.APPROVED;
    }

    /**
     * 소유권 주장 거절
     */
    public void reject() {
        if (this.status != ClaimStatus.PENDING) {
            throw new ItemConflictException("대기 중인 소유권 주장만 거절할 수 있습니다.");
        }
        this.status = ClaimStatus.REJECTED;
    }

    private static void validateVisitDate(LocalDate visitDate) {
        if (visitDate == null) {
            throw new ItemBadRequestException("방문 날짜는 필수입니다.");
        }
        if (visitDate.isBefore(LocalDate.now())) {
            throw new ItemBadRequestException("방문 날짜는 오늘 이전일 수 없습니다.");
        }
    }

    public enum ClaimStatus {
        PENDING, APPROVED, REJECTED
    }
}
