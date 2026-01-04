package com.eod.eod.domain.user.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 사용자 도메인 엔티티
// OAuth 인증 기반 사용자 정보 관리
// ADMIN, TEACHER, USER 역할 지원
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    // 사용자 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 학생 정보 - 졸업 여부
    @Column(name = "is_graduate")
    private Boolean isGraduate;

    // 학생 정보 - 학년 (1~3)
    @Column(name = "grade")
    private Integer grade;

    // 학생 정보 - 반
    @Column(name = "class_no")
    private Integer classNo;

    // 학생 정보 - 번호
    @Column(name = "student_no")
    private Integer studentNo;

    // OAuth 제공자 (예: bsm, google 등)
    @Column(name = "oauth_provider", nullable = false, length = 20)
    private String oauthProvider;

    // OAuth 제공자의 사용자 고유 ID
    @Column(name = "oauth_id", nullable = false, length = 100)
    private String oauthId;

    // 사용자 이름
    @Column(name = "name", nullable = false, length = 30)
    private String name;

    // 이메일 (중복 불가)
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // 사용자 역할 (ADMIN, TEACHER, USER)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // 자기소개
    @Column(name = "introduce", columnDefinition = "TEXT")
    private String introduce;

    // 계정 생성 일시
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 빌더 패턴을 통한 엔티티 생성
    // createdAt은 자동으로 현재 시간으로 설정됨
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

    // 도메인 로직: 관리자 권한 확인
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    // 도메인 로직: 교사 권한 확인
    public boolean isTeacher() {
        return this.role == Role.TEACHER;
    }

    // 도메인 로직: 본인 여부 확인
    public boolean isSameUser(User other) {
        return this.id.equals(other.getId());
    }

    // 도메인 로직: 교사 또는 관리자 권한 확인
    public boolean isTeacherOrAdmin() {
        return this.role.equals(Role.ADMIN) || this.role.equals(Role.TEACHER);
    }

    // 도메인 로직: 학생 정보 업데이트 (BSM OAuth 로그인 시 최신 정보 반영)
    public void updateStudentInfo(Boolean isGraduate, Integer grade, Integer classNo, Integer studentNo) {
        this.isGraduate = isGraduate;
        this.grade = grade;
        this.classNo = classNo;
        this.studentNo = studentNo;
    }

    public Integer getStudentCode() {
        if (grade == null || classNo == null || studentNo == null) {
            return null;
        }
        return grade * 1000 + classNo * 100 + studentNo;
    }

    // 사용자 역할 정의
    // ADMIN: 관리자, TEACHER: 교사, USER: 일반 사용자(학생)
    public enum Role {
        ADMIN, TEACHER, USER
    }
}
