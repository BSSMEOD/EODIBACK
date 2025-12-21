package com.eod.eod.domain.item.presentation;

import com.eod.eod.domain.item.application.DisposalReasonService;
import com.eod.eod.domain.item.presentation.dto.request.DisposalExtensionRequest;
import com.eod.eod.domain.item.presentation.dto.request.DisposalReasonRequest;
import com.eod.eod.domain.item.presentation.dto.response.DisposalCountResponse;
import com.eod.eod.domain.item.presentation.dto.response.DisposalExtensionResponse;
import com.eod.eod.domain.item.presentation.dto.response.DisposalReasonResponse;
import com.eod.eod.domain.item.presentation.dto.response.DisposalReasonSubmitResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Item Disposal", description = "물품 폐기 관리 API")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemDisposalController {

    private final DisposalReasonService disposalReasonService;

    @Operation(summary = "폐기 보류 사유 제출", description = "폐기 예정 물품에 대한 보류 사유를 제출합니다. 선생님 또는 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "보류 사유 제출 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DisposalReasonSubmitResponse.class)
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패 또는 잘못된 물품 상태)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"폐기 예정 상태의 물품만 보류할 수 있습니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "선생님 또는 관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"선생님 또는 관리자 권한이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "물품을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"해당 물품을 찾을 수 없습니다.\"}")
                    ))
    })
    @PostMapping("/{item-id}/disposal-reason")
    public ResponseEntity<DisposalReasonSubmitResponse> submitDisposalReason(
            @Parameter(description = "폐기 보류할 물품 ID", required = true, example = "1")
            @PathVariable("item-id") Long itemId,
            @Parameter(description = "폐기 보류 사유 제출 요청", required = true)
            @Valid @RequestBody DisposalReasonRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        disposalReasonService.submitDisposalReason(itemId, request.getReason(), request.getDays(), currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DisposalReasonSubmitResponse.of("보류 사유가 성공적으로 제출되었습니다."));
    }

    @Operation(summary = "폐기 보류 사유 조회", description = "특정 물품의 폐기 보류 사유를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DisposalReasonResponse.class)
                    )),
            @ApiResponse(responseCode = "404", description = "물품 또는 보류 사유를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"해당 물품의 폐기 보류 사유를 찾을 수 없습니다.\"}")
                    ))
    })
    @GetMapping("/{item-id}/disposal-reason")
    public ResponseEntity<DisposalReasonResponse> getDisposalReason(
            @Parameter(description = "조회할 물품 ID", required = true, example = "1")
            @PathVariable("item-id") Long itemId
    ) {
        DisposalReasonResponse response = DisposalReasonResponse.from(
                disposalReasonService.getDisposalReason(itemId)
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "폐기 기간 연장", description = "폐기 보류 사유를 기반으로 폐기 기간을 연장합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "폐기 기간 연장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DisposalExtensionResponse.class)
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 보류 사유 ID 또는 잘못된 물품 상태)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"폐기 예정 상태의 물품만 기간을 연장할 수 있습니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"관리자 권한이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "물품 또는 보류 사유를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"해당 보류 사유를 찾을 수 없습니다.\"}")
                    ))
    })
    @PatchMapping("/{item-id}/discarded")
    public ResponseEntity<DisposalExtensionResponse> extendDisposalPeriod(
            @Parameter(description = "폐기 기간을 연장할 물품 ID", required = true, example = "1")
            @PathVariable("item-id") Long itemId,
            @Parameter(description = "폐기 기간 연장 요청", required = true)
            @Valid @RequestBody DisposalExtensionRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        disposalReasonService.extendDisposalPeriod(itemId, request.getReasonId(), currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DisposalExtensionResponse.of("페기 보류 되었습니다."));
    }

    @Operation(summary = "폐기 예정 물품 개수 조회", description = "현재 등록된 폐기 예정 물품의 총 건수를 반환합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DisposalCountResponse.class)
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
    @GetMapping("/disposal/count")
    public ResponseEntity<DisposalCountResponse> getDisposalCount(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.isAdmin()) {
            throw new IllegalStateException("ADMIN 권한이 필요합니다.");
        }

        long count = disposalReasonService.countItemsToBeDiscarded();
        return ResponseEntity.ok(DisposalCountResponse.of(count));
    }
}
