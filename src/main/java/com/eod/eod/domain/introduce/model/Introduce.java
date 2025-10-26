package com.eod.eod.domain.introduce.model;

import com.eod.eod.domain.user.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "introduce")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Introduce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Introduce(String content) {
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 소개 페이지 수정
    public void updateContent(String newContent, User updater) {
        validateAdminRole(updater);
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    // Admin 권한 검증
    private void validateAdminRole(User user) {
        if (!user.isAdmin()) {
            throw new IllegalStateException("접근 권한이 없습니다.");
        }
    }
}