package com.eod.eod.domain.item.model;

import com.eod.eod.domain.user.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reasons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DisposalReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "extension_days", nullable = false)
    private Integer extensionDays;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DisposalReason(Item item, User teacher, String reason, Integer extensionDays) {
        this.item = item;
        this.teacher = teacher;
        this.reason = reason;
        this.extensionDays = extensionDays;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 폐기 보류 사유 생성
     */
    public static DisposalReason create(Item item, User teacher, String reason, Integer extensionDays) {
        validateTeacherRole(teacher);
        validateItem(item);
        String sanitizedReason = sanitizeReason(reason);
        Integer validatedExtensionDays = validateExtensionDays(extensionDays);

        return DisposalReason.builder()
                .item(item)
                .teacher(teacher)
                .reason(sanitizedReason)
                .extensionDays(validatedExtensionDays)
                .build();
    }

    private static void validateTeacherRole(User user) {
        if (!user.isTeacherOrAdmin()) {
            throw new IllegalStateException("선생님 또는 관리자 권한이 필요합니다.");
        }
    }

    private static void validateItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("물품 정보는 필수입니다.");
        }
        if (item.getStatus() != Item.ItemStatus.TO_BE_DISCARDED) {
            throw new IllegalStateException("폐기 예정 상태의 물품만 보류할 수 있습니다.");
        }
    }

    private static String sanitizeReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("보류 사유는 필수입니다.");
        }
        String trimmed = reason.trim();
        if (trimmed.length() > 1000) {
            throw new IllegalArgumentException("보류 사유는 1000자를 초과할 수 없습니다.");
        }
        return trimmed;
    }

    private static Integer validateExtensionDays(Integer extensionDays) {
        if (extensionDays == null) {
            throw new IllegalArgumentException("연장 일수는 필수입니다.");
        }
        if (extensionDays <= 0) {
            throw new IllegalArgumentException("연장 일수는 양수여야 합니다.");
        }
        if (extensionDays > 365) {
            throw new IllegalArgumentException("연장 일수는 365일을 초과할 수 없습니다.");
        }
        return extensionDays;
    }
}