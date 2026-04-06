package com.eod.eod.domain.item.model;

import com.eod.eod.domain.item.exception.ItemBadRequestException;
import com.eod.eod.domain.item.exception.ItemConflictException;
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

@Entity
@Table(name = "items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ItemCategory category;

    @Column(name = "found_at", nullable = false)
    private LocalDateTime foundAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "found_at_precision", nullable = false, length = 20)
    private DatePrecision foundAtPrecision;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
                String name, String image, ItemStatus status, ItemCategory category, LocalDateTime foundAt, DatePrecision foundAtPrecision) {
        this.student = student;
        this.admin = admin;
        this.foundPlaceId = foundPlaceId;
        this.foundPlaceDetail = foundPlaceDetail;
        this.name = name;
        this.image = image;
        this.status = status;
        this.category = category;
        this.foundAt = foundAt;
        this.foundAtPrecision = foundAtPrecision != null ? foundAtPrecision : DatePrecision.DAY;
        this.createdAt = LocalDateTime.now();
        this.discardedAt = this.createdAt.plusMonths(6);
        this.approvalStatus = ApprovalStatus.PENDING;
    }

    public static Item registerLostItem(User admin, User student, Long foundPlaceId, String foundPlaceDetail,
                                        String name, String imageUrl, ItemCategory category, LocalDateTime foundAt, DatePrecision foundAtPrecision) {
        ItemValidator.validateFoundAt(foundAt);
        ItemValidator.validateCategory(category);
        String sanitizedName = ItemSanitizer.sanitizeName(name);
        String sanitizedDetail = ItemSanitizer.sanitizeDetail(foundPlaceDetail);

        return Item.builder()
                .student(student)
                .admin(admin)
                .foundPlaceId(ItemValidator.requirePlaceId(foundPlaceId))
                .foundPlaceDetail(sanitizedDetail)
                .name(sanitizedName)
                .image(imageUrl == null ? "" : imageUrl)
                .status(ItemStatus.LOST)
                .category(category)
                .foundAt(foundAt)
                .foundAtPrecision(foundAtPrecision)
                .build();
    }

    // 물품 정보 수정
    public void updateItem(User updater, User student, Long foundPlaceId, String foundPlaceDetail,
                          String name, String imageUrl, ItemCategory category, LocalDateTime foundAt, DatePrecision foundAtPrecision) {
        ItemValidator.validateFoundAt(foundAt);
        ItemValidator.validateCategory(category);

        this.student = student != null ? student : this.student;
        this.foundPlaceId = ItemValidator.requirePlaceId(foundPlaceId);
        this.foundPlaceDetail = ItemSanitizer.sanitizeDetail(foundPlaceDetail);
        this.name = ItemSanitizer.sanitizeName(name);
        this.image = imageUrl == null ? "" : imageUrl;
        this.category = category;
        this.foundAt = foundAt;
        this.foundAtPrecision = foundAtPrecision != null ? foundAtPrecision : DatePrecision.DAY;
    }

    // 물품 지급 처리
    public void giveToStudent(User receiver, User giver) {
        if (this.status == ItemStatus.GIVEN) {
            throw new ItemConflictException("해당 물품은 이미 지급 처리되었습니다.");
        }
        this.status = ItemStatus.GIVEN;
    }

    // 승인/거절 처리
    public void processApproval(ApprovalStatus approvalStatus, User approver) {
        validateApprovalNotProcessed();

        if (approvalStatus == ApprovalStatus.APPROVED) {
            this.approvalStatus = ApprovalStatus.APPROVED;
            this.status = ItemStatus.GIVEN;
        } else if (approvalStatus == ApprovalStatus.REJECTED) {
            this.approvalStatus = ApprovalStatus.REJECTED;
        } else {
            throw new ItemBadRequestException("잘못된 승인 요청입니다.");
        }

        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    // 승인 처리 가능 여부 검증
    private void validateApprovalNotProcessed() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new ItemConflictException("이미 처리된 승인 요청입니다.");
        }
    }

    // 지급 여부 확인
    public boolean isGiven() {
        return this.status == ItemStatus.GIVEN;
    }

    public void validateClaimRequestable() {
        if (this.approvalStatus == ApprovalStatus.REJECTED) {
            throw new ItemConflictException("반려된 물품은 다시 회수 요청할 수 없습니다.");
        }
    }

    public void validateClaimableBy(User claimant) {
        if (this.student != null && this.student.getId() != null && this.student.getId().equals(claimant.getId())) {
            throw new ItemConflictException("분실물 습득자는 본인 물품에 대해 소유권을 주장할 수 없습니다.");
        }
        validateClaimRequestable();
    }

    public void softDelete() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 폐기 기간 연장
     * @param extensionDays 연장할 일수
     */
    public void extendDisposalDate(int extensionDays) {
        if (extensionDays <= 0) {
            throw new ItemBadRequestException("연장 일수는 양수여야 합니다.");
        }
        if (this.discardedAt == null) {
            this.discardedAt = this.createdAt.plusMonths(6);
        }
        this.discardedAt = this.discardedAt.plusDays(extensionDays);

        // 폐기일이 2주 이상 남아있으면 LOST 상태로 복구
        if (this.discardedAt.isAfter(LocalDateTime.now().plusWeeks(2))) {
            this.status = ItemStatus.LOST;
        }
    }

    public void extendDisposalDateWith(DisposalReason disposalReason) {
        if (this.status != ItemStatus.TO_BE_DISCARDED) {
            throw new ItemConflictException("폐기 예정 상태의 물품만 기간을 연장할 수 있습니다.");
        }
        if (!this.equals(disposalReason.getItem())) {
            throw new ItemBadRequestException("해당 보류 사유는 이 물품의 것이 아닙니다.");
        }
        extendDisposalDate(disposalReason.getExtensionDays());
    }

    /**
     * 물품을 폐기 예정 상태로 변경 (등록일로부터 6개월 후 폐기 예정)
     */
    public void markAsToBeDiscarded() {
        if (this.status != ItemStatus.LOST) {
            throw new ItemConflictException("분실물 상태의 물품만 폐기 예정으로 변경할 수 있습니다.");
        }
        this.status = ItemStatus.TO_BE_DISCARDED;
    }

    /**
     * 물품을 폐기 상태로 변경
     */
    public void discard() {
        if (this.status != ItemStatus.TO_BE_DISCARDED) {
            throw new ItemConflictException("폐기 예정 상태의 물품만 폐기할 수 있습니다.");
        }
        this.status = ItemStatus.DISCARDED;
        if (this.discardedAt == null) {
            this.discardedAt = LocalDateTime.now();
        }
    }

    public String getDiscardedAt() {
        return this.discardedAt != null
                ? this.discardedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : null;
    }

    public enum ItemStatus {
        LOST, TO_BE_DISCARDED, DISCARDED, GIVEN
    }

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }

    public enum DatePrecision {
        YEAR, MONTH, DAY
    }

    public enum ItemCategory {
        ELECTRONICS("전자기기"),
        SCHOOL_UNIFORM("의류"),
        ACCESSORIES("액세서리"),
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
            throw new ItemBadRequestException("유효하지 않은 카테고리입니다: " + koreanName);
        }
    }
}
