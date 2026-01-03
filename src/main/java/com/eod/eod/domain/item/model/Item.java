package com.eod.eod.domain.item.model;

import com.eod.eod.domain.user.model.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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

    @Column(name = "reporter_name", length = 50)
    private String reporterName;

    @Column(name = "image", nullable = false, columnDefinition = "TEXT")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ItemStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ItemCategory category;

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
                String name, String reporterName, String image, ItemStatus status, ItemCategory category, LocalDateTime foundAt) {
        this.student = student;
        this.admin = admin;
        this.foundPlaceId = foundPlaceId;
        this.foundPlaceDetail = foundPlaceDetail;
        this.name = name;
        this.reporterName = reporterName;
        this.image = image;
        this.status = status;
        this.category = category;
        this.foundAt = foundAt;
        this.createdAt = LocalDateTime.now();
        this.approvalStatus = ApprovalStatus.PENDING;
    }

    public static Item registerLostItem(User admin, User student, Long foundPlaceId, String foundPlaceDetail,
                                        String name, String reporterName, String imageUrl, ItemCategory category, LocalDateTime foundAt) {
        ItemValidator.validateFoundAt(foundAt);
        ItemValidator.validateCategory(category);
        String sanitizedName = ItemSanitizer.sanitizeName(name);
        String sanitizedDetail = ItemSanitizer.sanitizeDetail(foundPlaceDetail);
        String sanitizedReporterName = ItemSanitizer.sanitizeReporterName(reporterName);

        return Item.builder()
                .student(student != null ? student : admin)
                .admin(admin)
                .foundPlaceId(ItemValidator.requirePlaceId(foundPlaceId))
                .foundPlaceDetail(sanitizedDetail)
                .name(sanitizedName)
                .reporterName(sanitizedReporterName)
                .image(imageUrl == null ? "" : imageUrl)
                .status(ItemStatus.LOST)
                .category(category)
                .foundAt(foundAt)
                .build();
    }

    // 물품 정보 수정
    public void updateItem(User updater, User student, Long foundPlaceId, String foundPlaceDetail,
                          String name, String reporterName, String imageUrl, ItemCategory category, LocalDateTime foundAt) {
        ItemValidator.validateFoundAt(foundAt);
        ItemValidator.validateCategory(category);

        this.student = student != null ? student : this.student;
        this.foundPlaceId = ItemValidator.requirePlaceId(foundPlaceId);
        this.foundPlaceDetail = ItemSanitizer.sanitizeDetail(foundPlaceDetail);
        this.name = ItemSanitizer.sanitizeName(name);
        this.reporterName = ItemSanitizer.sanitizeReporterName(reporterName);
        this.image = imageUrl == null ? "" : imageUrl;
        this.category = category;
        this.foundAt = foundAt;
    }

    // 물품 지급 처리
    public void giveToStudent(User receiver, User giver) {
        if (this.status == ItemStatus.GIVEN) {
            throw new IllegalStateException("해당 물품은 이미 지급 처리되었습니다.");
        }
        this.student = receiver;
        this.status = ItemStatus.GIVEN;
    }

    // 승인/거절 처리
    public void processApproval(ApprovalStatus approvalStatus, User approver) {
        validateApprovalNotProcessed();

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

    // 승인 처리 가능 여부 검증
    private void validateApprovalNotProcessed() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 승인 요청입니다.");
        }
    }

    // 지급 여부 확인
    public boolean isGiven() {
        return this.status == ItemStatus.GIVEN;
    }

    /**
     * 폐기 기간 연장
     * @param extensionDays 연장할 일수
     */
    public void extendDisposalDate(int extensionDays) {
        if (extensionDays <= 0) {
            throw new IllegalArgumentException("연장 일수는 양수여야 합니다.");
        }
        this.discardedAt = discardedAt.plusDays(extensionDays);
    }

    /**
     * 물품을 폐기 예정 상태로 변경 (습득일로부터 6개월 후 폐기 예정)
     */
    public void markAsToBeDiscarded() {
        if (this.status != ItemStatus.LOST) {
            throw new IllegalStateException("분실물 상태의 물품만 폐기 예정으로 변경할 수 있습니다.");
        }
        this.status = ItemStatus.TO_BE_DISCARDED;
        // 습득일로부터 6개월 후를 폐기 예정일로 설정
        this.discardedAt = this.foundAt.plusMonths(6);
    }

    /**
     * 물품을 폐기 상태로 변경
     */
    public void discard() {
        if (this.status != ItemStatus.TO_BE_DISCARDED) {
            throw new IllegalStateException("폐기 예정 상태의 물품만 폐기할 수 있습니다.");
        }
        this.status = ItemStatus.DISCARDED;
        if (this.discardedAt == null) {
            this.discardedAt = LocalDateTime.now();
        }
    }

    public String getDiscardedAtAsDate() {
        return this.discardedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public enum ItemStatus {
        LOST, TO_BE_DISCARDED, DISCARDED, GIVEN
    }

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }

    public enum ItemCategory {
        SCHOOL_UNIFORM("교복"),
        GYM_UNIFORM("체육복"),
        GROUP_UNIFORM("단체복"),
        CASUAL_CLOTHES("사복"),
        WIRELESS_EARBUDS("무선 이어폰"),
        ELECTRONICS("전자기기"),
        GLASSES("안경"),
        ETC("기타");

        private final String koreanName;

        ItemCategory(String koreanName) {
            this.koreanName = koreanName;
        }

        @JsonValue
        public String getKoreanName() {
            return koreanName;
        }

        @JsonCreator
        public static ItemCategory from(String koreanName) {
            for (ItemCategory category : ItemCategory.values()) {
                if (category.koreanName.equals(koreanName)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + koreanName);
        }
    }
}