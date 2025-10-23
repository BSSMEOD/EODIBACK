package com.eod.eod.domain.reward.infrastructure;

import com.eod.eod.domain.reward.model.RewardRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRecordRepository extends JpaRepository<RewardRecord, Long> {

    // 학생 ID로 상점 지급 이력 조회
    List<RewardRecord> findByStudentId(Long studentId);
}