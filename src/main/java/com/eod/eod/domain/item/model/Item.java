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
    }

    // 물품 지급 처리
    public void giveToStudent(User receiver) {
        if (this.status == ItemStatus.GIVEN) {
            throw new IllegalStateException("해당 물품은 이미 지급 처리되었습니다.");
        }
        this.student = receiver;
        this.status = ItemStatus.GIVEN;
    }

    // 지급 여부 확인
    public boolean isGiven() {
        return this.status == ItemStatus.GIVEN;
    }

    public enum ItemStatus {
        LOST, TO_BE_DISCARDED, DISCARDED, GIVEN
    }
}