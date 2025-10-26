package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.GiveRecordRepository;
import com.eod.eod.domain.item.model.GiveRecord;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemGiveService {

    private final ItemFacade itemFacade;
    private final UserRepository userRepository;
    private final GiveRecordRepository giveRecordRepository;

    // 물품 지급 처리
    public void giveItemToStudent(Long itemId, Long receiverId, User currentUser) {
        // 물품 조회
        Item item = itemFacade.getItemById(itemId);

        // 지급받을 학생 확인
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생을 찾을 수 없습니다."));

        // 물품 지급 처리 (Item 도메인에서 Admin 권한 및 지급 여부 검증)
        item.giveToStudent(receiver, currentUser);

        // 지급 기록 생성 (감사 용도)
        GiveRecord giveRecord = GiveRecord.builder()
                .item(item)
                .giver(currentUser)
                .receiver(receiver)
                .build();

        giveRecordRepository.save(giveRecord);
    }
}