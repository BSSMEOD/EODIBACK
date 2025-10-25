package com.eod.eod.domain.item.model;

import com.eod.eod.domain.user.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(name = "found_place_id", nullable = false)
    private Long foundPlaceId;

    @Column(name = "found_place_detail", nullable = false, columnDefinition = "TEXT")
    private String foundPlaceDetail;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "image", nullable = false, columnDefinition = "TEXT")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ItemStatus status;

    @Column(name = "found_at", nullable = false)
    private LocalDateTime foundAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "discarded_at")
    private LocalDateTime discardedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Builder
    public Item(User student, User admin, Long foundPlaceId, String foundPlaceDetail,
                String name, String image, ItemStatus status, LocalDateTime foundAt) {
        this.student = student;
        this.admin = admin;
        this.foundPlaceId = foundPlaceId;
        this.foundPlaceDetail = foundPlaceDetail;
        this.name = name;
        this.image = image;
        this.status = status;
        this.foundAt = foundAt;
        this.createdAt = LocalDateTime.now();
        this.approvalStatus = ApprovalStatus.PENDING;
    }

    // 물품 지급 처리
    public void giveToStudent(User receiver, User giver) {
        validateAdminRole(giver);
        if (this.status == ItemStatus.GIVEN) {
            throw new IllegalStateException("해당 물품은 이미 지급 처리되었습니다.");
        }
        this.student = receiver;
        this.status = ItemStatus.GIVEN;
    }

    // 승인/거절 처리
    public void processApproval(ApprovalStatus approvalStatus, User approver) {
        validateApprovalNotProcessed();
        validateAdminRole(approver);

        if (approvalStatus == ApprovalStatus.APPROVED) {
            this.approvalStatus = ApprovalStatus.APPROVED;
        } else if (approvalStatus == ApprovalStatus.REJECTED) {
            this.approvalStatus = ApprovalStatus.REJECTED;
        } else {
            throw new IllegalArgumentException("잘못된 승인 요청입니다.");
        }

        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    // 승인 처리 (private - 내부 사용)
    private void approve(User approver) {
        validateApprovalNotProcessed();
        validateAdminRole(approver);
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    // 거절 처리 (private - 내부 사용)
    private void reject(User approver) {
        validateApprovalNotProcessed();
        validateAdminRole(approver);
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    // 승인 처리 가능 여부 검증
    private void validateApprovalNotProcessed() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 승인 요청입니다.");
        }
    }

    // Admin 권한 검증
    private void validateAdminRole(User user) {
        if (!user.isAdmin()) {
            throw new IllegalStateException("ADMIN 권한이 필요합니다.");
        }
    }

    // 지급 여부 확인
    public boolean isGiven() {
        return this.status == ItemStatus.GIVEN;
    }

    public enum ItemStatus {
        LOST, TO_BE_DISCARDED, DISCARDED, GIVEN
    }

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
}