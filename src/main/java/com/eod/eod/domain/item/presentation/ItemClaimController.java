package com.eod.eod.domain.item.presentation;

import com.eod.eod.common.validation.EnumValue;
import com.eod.eod.domain.item.application.ItemClaimQueryService;
import com.eod.eod.domain.item.application.ItemClaimService;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.item.presentation.dto.request.ItemClaimRequest;
import com.eod.eod.domain.item.presentation.dto.response.ClaimCountResponse;
import com.eod.eod.domain.item.presentation.dto.response.ClaimItemListResponse;
import com.eod.eod.domain.item.presentation.dto.response.ClaimRequestsResponse;
import com.eod.eod.domain.item.presentation.dto.response.ItemClaimResponse;
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
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Tag(name = "Item Claim", description = "분실물 소유권 주장 API")
@Validated
public class ItemClaimController {

    private final ItemClaimService itemClaimService;
    private final ItemClaimQueryService itemClaimQueryService;

    @Operation(summary = "소유권 주장", description = "사용자가 분실물에 대한 소유권을 주장합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "소유권 주장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemClaimResponse.class)
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
    @PostMapping("/{itemId}/claims")
    public ResponseEntity<ItemClaimResponse> claimItem(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemClaimRequest request,
            @AuthenticationPrincipal User currentUser) {

        ItemClaimResponse response = itemClaimService.claimItem(
                itemId,
                request.getClaimReason(),
                currentUser
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회수 신청 건수 조회", description = "현재 등록된 회수 신청 건수를 반환합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClaimCountResponse.class)
                    )),
            @ApiResponse(responseCode = "401", description = "인증 실패 또는 ADMIN 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"ADMIN 권한이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"서버 내부 오류가 발생했습니다.\"}")
                    ))
    })
    @GetMapping("/claims/count")
    public ResponseEntity<ClaimCountResponse> getClaimCount(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.isAdmin()) {
            throw new IllegalStateException("ADMIN 권한이 필요합니다.");
        }

        long count = itemClaimQueryService.countPendingClaims();
        return ResponseEntity.ok(ClaimCountResponse.of(count));
    }

    

    @Operation(summary = "회수 요청 관리 페이지 조회", description = "관리자가 회수 요청 목록을 조회합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClaimRequestsResponse.class)
                    )),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"인증이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"ADMIN 권한이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"서버 내부 오류가 발생했습니다.\"}")
                    ))
    })
    @GetMapping("/claims/requests")
    public ResponseEntity<ClaimRequestsResponse> getClaimRequests(
            @Parameter(description = "페이지 번호 (기본값: 1)")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "페이지당 항목 수 (기본값: 10)")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "회수 요청 상태 (PENDING, APPROVED, REJECTED, null일 경우 전체 조회)")
            @EnumValue(enumClass = ItemClaim.ClaimStatus.class, allowBlank = true,
                    message = "유효하지 않은 회수 상태 값입니다.")
            @RequestParam(required = false) String status,
            @Parameter(description = "정렬 방식 (LATEST: 최신순, OLDEST: 오래된순, 기본값: LATEST)")
            @RequestParam(required = false) String sort,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.isAdmin()) {
            throw new IllegalStateException("ADMIN 권한이 필요합니다.");
        }

        ClaimRequestsResponse response = itemClaimQueryService.getClaimRequests(page, size, status, sort);
        return ResponseEntity.ok(response);
    }

  
    @Operation(summary = "회수 신청이 있는 분실물 목록 조회", description = "PENDING 상태의 회수 신청이 있는 분실물 목록을 조회합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClaimItemListResponse.class)
                    )),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"ADMIN 권한이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "회수 신청 요청이 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"회수 신청 요청이 존재하지 않습니다.\"}")
                    )),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"서버 내부 오류가 발생했습니다.\"}")
                    ))
    })
    @GetMapping("/claims")
    public ResponseEntity<ClaimItemListResponse> getItemsWithClaims(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.isAdmin()) {
            throw new IllegalStateException("ADMIN 권한이 필요합니다.");
        }

        ClaimItemListResponse response = itemClaimQueryService.getPendingClaimItems();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소유권 주장 승인", description = "관리자가 소유권 주장을 승인합니다. 같은 물품에 대한 다른 PENDING 주장들은 자동으로 거절됩니다. ADMIN 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "승인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"소유권 주장이 승인되었습니다.\"}")
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 처리된 주장)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"대기 중인 소유권 주장만 승인할 수 있습니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"ADMIN 권한이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "소유권 주장을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"해당 소유권 주장을 찾을 수 없습니다.\"}")
                    ))
    })
    @PostMapping("/claims/{claimId}/approve")
    public ResponseEntity<Void> approveClaim(
            @PathVariable Long claimId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        itemClaimService.approveClaim(claimId, currentUser);
        return ResponseEntity.ok().build();
    }


}
