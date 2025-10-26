package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.item.presentation.dto.ItemClaimResponse;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemClaimService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemClaimRepository itemClaimRepository;

    public ItemClaimResponse claimItem(Long itemId, Long studentId, String claimReason, User currentUser) {
        // 아이템 존재 여부 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분실물을 찾을 수 없습니다."));

        // 학생 존재 여부 확인
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생을 찾을 수 없습니다."));

        // 본인 확인 (User 도메인 로직 사용)
        if (!student.isSameUser(currentUser)) {
            throw new IllegalArgumentException("본인만 소유권을 주장할 수 있습니다.");
        }

        // 중복 주장 확인
        if (itemClaimRepository.existsByItemIdAndClaimantId(itemId, studentId)) {
            throw new IllegalStateException("이미 해당 분실물에 대해 소유권을 주장하셨습니다.");
        }

        // 소유권 주장 생성
        ItemClaim claim = ItemClaim.builder()
                .item(item)
                .claimant(student)
                .claimReason(claimReason)
                .build();

        itemClaimRepository.save(claim);

        return ItemClaimResponse.success();
    }
}