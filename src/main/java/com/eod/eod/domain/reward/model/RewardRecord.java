package com.eod.eod.domain.reward.model;

import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.user.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public RewardRecord(User student, Item item, User teacher) {
        validateTeacherRole(teacher);
        validateItemGiven(item);
        this.student = student;
        this.item = item;
        this.teacher = teacher;
        this.createdAt = LocalDateTime.now();
    }

    // 교사/관리자 권한 검증
    private void validateTeacherRole(User user) {
        if (!user.isTeacherOrAdmin()) {
            throw new IllegalStateException("상점을 지급할 권한이 없습니다.");
        }
    }

    // 물품이 지급 완료 상태인지 검증
    private void validateItemGiven(Item item) {
        if (!item.isGiven()) {
            throw new IllegalStateException("지급 처리되지 않은 물품입니다.");
        }
    }
}