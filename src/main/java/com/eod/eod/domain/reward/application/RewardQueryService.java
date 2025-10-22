package com.eod.eod.domain.reward.application;

import com.eod.eod.domain.reward.infrastructure.RewardRecordRepository;
import com.eod.eod.domain.reward.model.RewardRecord;
import com.eod.eod.domain.reward.presentation.dto.RewardEligibleResponse;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardQueryService {

    private final RewardRecordRepository rewardRecordRepository;
    private final UserRepository userRepository;

    // 상점 지급 여부 조회
    public RewardEligibleResponse checkRewardEligibility(Long studentId, Long itemId, User currentUser) {
        // 교사 권한 확인
        if (currentUser.getRole() != User.Role.TEACHER) {
            throw new AccessDeniedException("권한이 없는 사용자입니다.");
        }

        // 학생 존재 여부 확인
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 사용자입니다."));

        // 상점 지급 기록 조회
        Optional<RewardRecord> rewardRecord = rewardRecordRepository.findByStudentIdAndItemId(studentId, itemId);

        // 응답 생성
        if (rewardRecord.isPresent()) {
            RewardRecord record = rewardRecord.get();
            return new RewardEligibleResponse(
                    studentId,
                    itemId,
                    record.getId(),
                    record.getCreatedAt()
            );
        } else {
            // 상점을 받지 않은 경우
            return new RewardEligibleResponse(
                    studentId,
                    itemId,
                    null,
                    null
            );
        }
    }
}