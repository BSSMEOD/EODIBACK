package com.eod.eod.domain.reward.application;

import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.reward.infrastructure.RewardRecordRepository;
import com.eod.eod.domain.reward.model.RewardRecord;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
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
        // 학생 존재 여부 확인
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        // 물품 존재 여부 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("물품을 찾을 수 없습니다."));

        // 상점 지급 기록 생성 (RewardRecord 도메인에서 권한 및 검증 처리)
        RewardRecord rewardRecord = RewardRecord.builder()
                .student(student)
                .item(item)
                .teacher(currentUser)
                .build();

        rewardRecordRepository.save(rewardRecord);
    }
}