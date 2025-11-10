package com.eod.eod.domain.item.presentation;

import com.eod.eod.domain.item.application.ItemApprovalService;
import com.eod.eod.domain.item.application.ItemDetailService;
import com.eod.eod.domain.item.application.ItemGiveService;
import com.eod.eod.domain.item.application.ItemRegistrationService;
import com.eod.eod.domain.item.application.ItemSearchService;
import com.eod.eod.domain.item.presentation.dto.*;
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
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.annotation.Validated;

@Tag(name = "Item", description = "물품 관리 API")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemGiveService itemGiveService;
    private final ItemApprovalService itemApprovalService;
    private final ItemRegistrationService itemRegistrationService;
    private final ItemDetailService itemDetailService;
    private final ItemSearchService itemSearchService;

    @Operation(summary = "분실물 등록", description = "Multipart Form 데이터로 분실물을 등록하고 이미지 파일은 외부 서버에 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "분실물 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemCreateResponse.class),
                            examples = @ExampleObject(value = "{\"item_id\":104,\"message\":\"분실물이 성공적으로 등록되었습니다.\"}")
                    )),
            @ApiResponse(responseCode = "400", description = "필수 값 누락 또는 잘못된 입력",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\":\"필수 항목이 누락되었습니다.\"}")
                    )),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "등록되지 않은 장소",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\":\"등록되지 않은 장소입니다.\"}")
                    ))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemCreateResponse> registerItem(
            @Parameter(description = "분실물 등록 폼", required = true)
            @Valid @ModelAttribute ItemRegistrationForm form,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        Long itemId = itemRegistrationService.registerItem(
                form.getName(),
                form.getFoundDate(),
                form.getPlaceId(),
                form.getPlaceDetail(),
                form.getImage(),
                currentUser
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ItemCreateResponse.success(itemId));
    }

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
            @PathVariable("item-id") Long itemId,
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
                                        "approver": {
                                            "id": 12,
                                            "name": "이하은"
                                        },
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
        ItemApprovalResponse response = itemApprovalService.processApproval(
                itemId,
                itemApprovalRequest.toApprovalStatus(),
                currentUser
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "분실물 검색", description = "장소 ID와 상태로 분실물을 검색합니다. 페이징을 지원합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemSearchResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "content": [
                                            {
                                                "id": 101,
                                                "name": "무선 이어폰",
                                                "found_date": "2025-07-01",
                                                "found_place": "SRC",
                                                "place_detail": "3층 남자기숙사 중앙홀",
                                                "thumbnail_url": ""
                                            }
                                        ],
                                        "page": 1,
                                        "size": 10,
                                        "total_elements": 132,
                                        "total_pages": 14,
                                        "is_last": false
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 상태 값)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"유효하지 않은 상태 값입니다: INVALID\"}")
                    ))
    })
    @GetMapping("/search")
    public ResponseEntity<ItemSearchResponse> searchItems(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "장소 ID (선택 사항)", example = "2")
            @RequestParam(name = "place_id", required = false) Long placeId,
            @Parameter(description = "물품 상태 (LOST, TO_BE_DISCARDED, DISCARDED, GIVEN) - 필수", example = "LOST", required = true)
            @NotBlank(message = "물품 상태는 필수입니다.")
            @RequestParam String status
    ) {
        ItemSearchResponse response = itemSearchService.searchItems(placeId, status, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "물품 상세 조회", description = "특정 물품의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "id": 1,
                                        "name": "무테 긱시크 안경",
                                        "image_url": "",
                                        "found_at": "2025-06-19 12:20",
                                        "found_place": "기타",
                                        "found_place_detail": "운동장"
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404", description = "물품 또는 장소를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"해당 물품을 찾을 수 없습니다.\"}")
                    ))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ItemDetailResponse> getItemDetail(
            @Parameter(description = "조회할 물품 ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        ItemDetailResponse response = itemDetailService.getItemDetail(id);
        return ResponseEntity.ok(response);
    }
}
