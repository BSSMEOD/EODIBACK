package com.eod.eod.domain.item.presentation;

import com.eod.eod.domain.item.application.ItemClaimService;
import com.eod.eod.domain.item.presentation.dto.ItemClaimRequest;
import com.eod.eod.domain.item.presentation.dto.ItemClaimResponse;
import com.eod.eod.domain.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Tag(name = "Item Claim", description = "분실물 소유권 주장 API")
public class ItemClaimController {

    private final ItemClaimService itemClaimService;

    @Operation(summary = "소유권 주장", description = "사용자가 분실물에 대한 소유권을 주장합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "소유권 주장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemClaimResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"소유권 주장이 정상적으로 등록되었습니다.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"인증이 필요합니다.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 분실물",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"해당 분실물을 찾을 수 없습니다.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 소유권을 주장한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"이미 해당 분실물에 대해 소유권을 주장하셨습니다.\"}")
                    )
            )
    })
    @PostMapping("/{itemId}/claim")
    public ResponseEntity<ItemClaimResponse> claimItem(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemClaimRequest request,
            @AuthenticationPrincipal User currentUser) {

        ItemClaimResponse response = itemClaimService.claimItem(
                itemId,
                request.getStudentId(),
                request.getClaimReason(),
                currentUser
        );

        return ResponseEntity.ok(response);
    }
}