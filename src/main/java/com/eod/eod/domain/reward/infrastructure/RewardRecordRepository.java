package com.eod.eod.domain.reward.infrastructure;

import com.eod.eod.domain.reward.model.RewardRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RewardRecordRepository extends JpaRepository<RewardRecord, Long>, JpaSpecificationExecutor<RewardRecord> {

    // 학생 ID로 상점 지급 이력 조회 (N+1 방지: item, teacher fetch join)
    @Query("SELECT r FROM RewardRecord r " +
            "JOIN FETCH r.item i " +
            "JOIN FETCH r.teacher t " +
            "WHERE r.student.id = :studentId")
    List<RewardRecord> findByStudentId(@Param("studentId") Long studentId);

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
