package com.eod.eod.domain.reward.infrastructure;

import com.eod.eod.domain.reward.model.RewardRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RewardRecordRepository extends JpaRepository<RewardRecord, Long> {

    // 학생 ID로 상점 지급 이력 조회
    List<RewardRecord> findByStudentId(Long studentId);

    // 날짜, 학년, 반으로 상점 지급 이력 조회
    @Query("SELECT r FROM RewardRecord r " +
            "JOIN FETCH r.student s " +
            "JOIN FETCH r.item i " +
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
