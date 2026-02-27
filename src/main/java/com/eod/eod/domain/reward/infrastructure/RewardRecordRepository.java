package com.eod.eod.domain.reward.infrastructure;

import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.reward.model.RewardRecord;
import com.eod.eod.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RewardRecordRepository extends JpaRepository<RewardRecord, Long>, JpaSpecificationExecutor<RewardRecord> {

    // 학생 ID로 상점 지급 이력 조회 (N+1 방지: item, teacher fetch join)
    @Query("SELECT r FROM RewardRecord r " +
            "JOIN FETCH r.item i " +
            "JOIN FETCH r.teacher t " +
            "WHERE r.student.id = :studentId")
    List<RewardRecord> findByStudentId(@Param("studentId") Long studentId);

    // 물품 ID로 상점 지급 중복 여부 확인
    boolean existsByItemId(Long itemId);

    // 물품 ID로 상점 지급 기록 조회
    Optional<RewardRecord> findByItemId(Long itemId);

    // 상점 지급 대기 물품 수: 지급 완료(GIVEN) + 학생 신고자 있음 + 아직 상점 미지급
    @Query("SELECT COUNT(i) FROM Item i " +
            "WHERE i.status = :status " +
            "AND i.student.role = :role " +
            "AND NOT EXISTS (SELECT r FROM RewardRecord r WHERE r.item = i)")
    long countRewardEligibleItems(@Param("status") Item.ItemStatus status, @Param("role") User.Role role);

    // 날짜, 학년, 반으로 상점 지급 이력 조회 (N+1 방지: item, teacher fetch join)
    @Query("SELECT r FROM RewardRecord r " +
            "JOIN FETCH r.student s " +
            "JOIN FETCH r.item i " +
            "JOIN FETCH r.teacher t " +
            "WHERE FUNCTION('DATE', r.createdAt) = FUNCTION('DATE', :date) " +
            "AND s.grade = :grade " +
            "AND s.classNo = :classNumber " +
            "ORDER BY r.createdAt DESC")
    List<RewardRecord> findByDateAndGradeAndClass(
            @Param("date") LocalDateTime date,
            @Param("grade") Integer grade,
            @Param("classNumber") Integer classNumber
    );
}
