package com.eod.eod.domain.item.presentation;

import com.eod.eod.domain.item.application.ItemGiveService;
import com.eod.eod.domain.item.presentation.dto.ItemGiveRequest;
import com.eod.eod.domain.item.presentation.dto.ItemGiveResponse;
import com.eod.eod.domain.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemGiveService itemGiveService;

    // 물품 지급 API
    @PostMapping("/{itemId}/give")
    public ResponseEntity<ItemGiveResponse> giveItem(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemGiveRequest itemGiveRequest,
            @AuthenticationPrincipal User currentUser
    ) {
        // 물품 지급 서비스 호출
        itemGiveService.giveItemToStudent(itemId, itemGiveRequest.getStudentId(), currentUser);
        
        return ResponseEntity.ok(ItemGiveResponse.success());
    }
}