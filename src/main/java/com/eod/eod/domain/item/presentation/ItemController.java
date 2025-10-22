package com.eod.eod.domain.item.presentation;

import com.eod.eod.domain.item.application.ItemGiveService;
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
}