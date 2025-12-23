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

    @Column(name = "is_graduate")
    private Boolean isGraduate;

    @Column(name = "grade")
    private Integer grade;

    @Column(name = "class_no")
    private Integer classNo;

    @Column(name = "student_no")
    private Integer studentNo;

    @Column(name = "oauth_provider", nullable = false, length = 20)
    private String oauthProvider;

    @Column(name = "oauth_id", nullable = false, length = 100)
    private String oauthId;

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
    public User(Boolean isGraduate, Integer grade, Integer classNo, Integer studentNo,
                String oauthProvider, String oauthId, String name, String email, Role role, String introduce) {
        this.isGraduate = isGraduate;
        this.grade = grade;
        this.classNo = classNo;
        this.studentNo = studentNo;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.introduce = introduce;
        this.createdAt = LocalDateTime.now();
    }

    // 도메인 로직: 권한 확인
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isTeacher() {
        return this.role == Role.TEACHER;
    }
    // 도메인 로직: 본인 확인
    public boolean isSameUser(User other) {
        return this.id.equals(other.getId());
    }

    public boolean isTeacherOrAdmin() {
        return this.role.equals(Role.ADMIN) || this.role.equals(Role.TEACHER);
    }

    public enum Role {
        ADMIN, TEACHER, USER
    }
}
