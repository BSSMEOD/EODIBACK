package com.eod.eod.domain.item.presentation;

import com.eod.eod.domain.item.application.ItemApprovalService;
import com.eod.eod.domain.item.application.ItemGiveService;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.presentation.dto.ItemApprovalRequest;
import com.eod.eod.domain.item.presentation.dto.ItemApprovalResponse;
import com.eod.eod.domain.item.presentation.dto.ItemGiveRequest;
import com.eod.eod.domain.item.presentation.dto.ItemGiveResponse;
import com.eod.eod.domain.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Item", description = "물품 관리 API")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemGiveService itemGiveService;
    private final ItemApprovalService itemApprovalService;

    @Operation(summary = "물품 지급", description = "학생에게 특정 물품을 지급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "물품 지급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemGiveResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"물품 지급이 완료되었습니다.\"}")
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패 또는 이미 지급된 경우)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"해당 물품은 이미 지급 처리되었습니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"ADMIN 권한이 없습니다.\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "물품 또는 학생을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"해당 물품을 찾을 수 없습니다.\"}")
                    )),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"인증에 실패했습니다.\"}")
                    ))
    })
    @PostMapping("/{item-id}/give")
    public ResponseEntity<ItemGiveResponse> giveItem(
            @Parameter(description = "지급할 물품 ID", required = true, example = "1")
            @PathVariable Long itemId,
            @Parameter(description = "물품 지급 요청 정보", required = true)
            @Valid @RequestBody ItemGiveRequest itemGiveRequest,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        // 물품 지급 서비스 호출
        itemGiveService.giveItemToStudent(itemId, itemGiveRequest.getStudentId(), currentUser);

        return ResponseEntity.ok(ItemGiveResponse.success());
    }

    @Operation(summary = "물품 승인/거절", description = "분실물 소유권을 승인하거나 거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "승인/거절 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemApprovalResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "item_id": 1,
                                        "approval_status": "APPROVED",
                                        "approved_by_id": 12,
                                        "approved_by": "이하은",
                                        "approved_at": "2025-08-02",
                                        "message": "소유권이 승인되었습니다."
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 승인 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"잘못된 승인 요청입니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"ADMIN 권한이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 분실물",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"해당 분실물을 찾을 수 없습니다.\"}")
                    )),
            @ApiResponse(responseCode = "409", description = "이미 처리된 승인 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"이미 처리된 승인 요청입니다.\"}")
                    )),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"이메일 혹은 비밀번호가 일치하지 않습니다.\"}")
                    ))
    })
    @PatchMapping("/{item-id}/approve")
    public ResponseEntity<ItemApprovalResponse> approveItem(
            @Parameter(description = "승인/거절할 물품 ID", required = true, example = "1")
            @PathVariable("item-id") Long itemId,
            @Parameter(description = "물품 승인 요청 정보", required = true)
            @Valid @RequestBody ItemApprovalRequest itemApprovalRequest,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        // 승인/거절 처리 서비스 호출
        Item item = itemApprovalService.processApproval(
                itemId,
                itemApprovalRequest.toApprovalStatus(),
                currentUser
        );

        return ResponseEntity.ok(ItemApprovalResponse.from(item));
    }
}