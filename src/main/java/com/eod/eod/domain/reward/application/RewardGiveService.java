package com.eod.eod.domain.reward.application;

import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.reward.infrastructure.RewardRecordRepository;
import com.eod.eod.domain.reward.model.RewardRecord;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardGiveService {

    private final RewardRecordRepository rewardRecordRepository;
    private final ItemRepository itemRepository;

    // 상점 지급 처리
    public void giveRewardToStudent(Long itemId, User currentUser) {
        // 물품 존재 여부 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("물품을 찾을 수 없습니다."));

        // 습득 신고자(item.student)를 상점 수령인으로 사용
        User student = item.getStudent();
        if (student == null) {
            throw new IllegalStateException("습득 신고자가 없는 물품입니다.");
        }

        // 중복 상점 지급 방지
        if (rewardRecordRepository.existsByItemId(itemId)) {
            throw new IllegalStateException("이미 상점이 지급된 물품입니다.");
        }

        // 상점 지급 기록 생성 (RewardRecord 도메인에서 권한 및 검증 처리)
        RewardRecord rewardRecord = RewardRecord.builder()
                .student(student)
                .item(item)
                .teacher(currentUser)
                .build();

        rewardRecordRepository.save(rewardRecord);
    }
}