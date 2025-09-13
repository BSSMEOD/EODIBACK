package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.GiveRecordRepository;
import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.GiveRecord;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemGiveService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final GiveRecordRepository giveRecordRepository;

    // 물품 지급 처리
    public void giveItemToStudent(Long itemId, Long receiverId, User giver) {
        // ADMIN 권한 확인
        validateAdminPermission(giver);
        
        // 물품 존재 여부 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 물품을 찾을 수 없습니다."));
        
        // 지급받을 학생 확인
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생을 찾을 수 없습니다."));
        
        // 물품이 이미 지급되었는지 확인
        if (item.getStatus() == Item.ItemStatus.GIVEN) {
            throw new IllegalStateException("해당 물품은 이미 지급 처리되었습니다.");
        }
        
        // 물품 지급 처리
        item.giveToStudent(receiver);
        
        // 지급 기록 생성 (감사 용도)
        GiveRecord giveRecord = GiveRecord.builder()
                .item(item)
                .giver(giver)
                .receiver(receiver)
                .status(true)
                .build();
        
        giveRecordRepository.save(giveRecord);
    }

    // ADMIN 권한 검증
    private void validateAdminPermission(User user) {
        if (user.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("ADMIN 권한이 없습니다.");
        }
    }
}