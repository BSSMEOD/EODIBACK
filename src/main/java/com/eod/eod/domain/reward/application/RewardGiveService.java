package com.eod.eod.domain.reward.application;

import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.reward.infrastructure.RewardRecordRepository;
import com.eod.eod.domain.reward.model.RewardRecord;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardGiveService {

    private final RewardRecordRepository rewardRecordRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    // 상점 지급 처리
    public void giveRewardToStudent(Long studentId, Long itemId, User currentUser) {
        // 교사 권한 확인 (User 도메인 로직 사용)
        if (!currentUser.isTeacher()) {
            throw new AccessDeniedException("상점을 지급할 권한이 없습니다.");
        }

        // 학생 존재 여부 확인
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        // 물품 존재 여부 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("물품을 찾을 수 없습니다."));

        // 상점 지급 가능 여부 검증 (Item 도메인에서 예외 처리)
        item.validateRewardEligibility();

        // 상점 지급 기록 생성
        RewardRecord rewardRecord = RewardRecord.builder()
                .student(student)
                .item(item)
                .teacher(currentUser)
                .build();

        rewardRecordRepository.save(rewardRecord);
    }
}