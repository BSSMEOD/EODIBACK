package com.eod.eod.domain.reward.application;

import com.eod.eod.domain.reward.infrastructure.RewardRecordRepository;
import com.eod.eod.domain.reward.model.RewardRecord;
import com.eod.eod.domain.reward.presentation.dto.RewardHistoryResponse;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardQueryService {

    private final RewardRecordRepository rewardRecordRepository;
    private final UserRepository userRepository;

    // 상점 지급 이력 조회
    public RewardHistoryResponse getRewardHistory(Long userId, User currentUser) {
        // 권한 검증 (TEACHER 또는 ADMIN만 조회 가능)
        if (!currentUser.isTeacherOrAdmin()) {
            throw new IllegalStateException("접근 권한이 없습니다.");
        }

        // 조회 대상 사용자 확인
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 상점 지급 이력 조회
        List<RewardRecord> records = rewardRecordRepository.findByStudentId(userId);

        // Response 변환
        return RewardHistoryResponse.from(userId, records);
    }
}