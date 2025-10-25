package com.eod.eod.domain.reward.infrastructure;

import com.eod.eod.domain.reward.model.RewardRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RewardRecordRepository extends JpaRepository<RewardRecord, Long> {

    // 특정 학생과 물품에 대한 상점 지급 기록 조회
    Optional<RewardRecord> findByStudentIdAndItemId(Long studentId, Long itemId);
}
