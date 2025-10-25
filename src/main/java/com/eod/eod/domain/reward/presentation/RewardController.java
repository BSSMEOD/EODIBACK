package com.eod.eod.domain.reward.presentation;

import com.eod.eod.domain.reward.application.RewardGiveService;
import com.eod.eod.domain.reward.application.RewardQueryService;
import com.eod.eod.domain.reward.presentation.dto.RewardEligibleResponse;
import com.eod.eod.domain.reward.presentation.dto.RewardGiveRequest;
import com.eod.eod.domain.reward.presentation.dto.RewardGiveResponse;
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

@Tag(name = "Reward", description = "상점 관리 API")
@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardQueryService rewardQueryService;
    private final RewardGiveService rewardGiveService;

    @Operation(summary = "상점 지급", description = "학생에게 상점을 지급합니다. (교사 전용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상점 지급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RewardGiveResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"상점이 성공적으로 지급되었습니다.\"}")
                    )),
            @ApiResponse(responseCode = "400", description = "이미 상점이 지급된 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"이미 상점이 지급된 항목입니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "교사 권한이 아닐 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"상점을 지급할 권한이 없습니다.\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"학생을 찾을 수 없습니다.\"}")
                    ))
    })
    @PostMapping
    public ResponseEntity<RewardGiveResponse> giveReward(
            @Parameter(description = "상점 지급 요청 정보", required = true)
            @Valid @RequestBody RewardGiveRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        rewardGiveService.giveRewardToStudent(request.getStudentId(), request.getItemId(), currentUser);
        return ResponseEntity.ok(RewardGiveResponse.success());
    }

    @Operation(summary = "상점 지급 여부 조회", description = "특정 학생이 특정 물품에 대해 상점을 받았는지 조회합니다. (교사 전용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RewardEligibleResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "상점을 받은 경우",
                                            value = "{\"student_id\": 1, \"item_id\": 1, \"id\": 1, \"created_at\": \"2025-07-31T00:00:00\"}"
                                    ),
                                    @ExampleObject(
                                            name = "상점을 받지 않은 경우",
                                            value = "{\"student_id\": 1, \"item_id\": 1, \"id\": null, \"created_at\": null}"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"로그인이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "교사 권한이 아닐 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"권한이 없는 사용자입니다.\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"올바르지 않은 사용자입니다.\"}")
                    ))
    })
    @GetMapping("/eligible")
    public ResponseEntity<RewardEligibleResponse> checkRewardEligibility(
            @Parameter(description = "학생 ID", required = true, example = "1")
            @RequestParam("student_id") Long studentId,
            @Parameter(description = "물품 ID", required = true, example = "1")
            @RequestParam("item_id") Long itemId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        RewardEligibleResponse response = rewardQueryService.checkRewardEligibility(studentId, itemId, currentUser);
        return ResponseEntity.ok(response);
    }
}