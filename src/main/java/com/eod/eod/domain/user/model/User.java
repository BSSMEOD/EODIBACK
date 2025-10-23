package com.eod.eod.domain.user.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_code")
    private Integer studentCode;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "introduce", columnDefinition = "TEXT")
    private String introduce;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(Integer studentCode, String name, String email, Role role, String introduce) {
        this.studentCode = studentCode;
        this.name = name;
        this.email = email;
        this.role = role;
        this.introduce = introduce;
        this.createdAt = LocalDateTime.now();
    }

    // 도메인 로직: ADMIN 권한 여부 확인
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isTeacher() {
        return this.role == Role.TEACHER;
    }

    // 교사 권한 검증
    public void validateTeacherRole() {
        if (!this.isTeacher()) {
            throw new IllegalStateException("상점을 지급할 권한이 없습니다.");
        }
    }

    public enum Role {
        ADMIN, TEACHER, USER
    }
}