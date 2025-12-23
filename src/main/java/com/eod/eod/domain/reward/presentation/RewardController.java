package com.eod.eod.domain.reward.presentation;

import com.eod.eod.domain.reward.application.RewardGiveService;
import com.eod.eod.domain.reward.application.RewardQueryService;
import com.eod.eod.domain.reward.presentation.dto.request.RewardGiveRequest;
import com.eod.eod.domain.reward.presentation.dto.request.RewardHistoryRequest;
import com.eod.eod.domain.reward.presentation.dto.response.RewardEligibleResponse;
import com.eod.eod.domain.reward.presentation.dto.response.RewardGiveHistoryResponse;
import com.eod.eod.domain.reward.presentation.dto.response.RewardGiveResponse;
import com.eod.eod.domain.reward.presentation.dto.response.RewardHistoryResponse;
import com.eod.eod.domain.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Reward", description = "상점 관리 API")
@SecurityRequirement(name = "bearerAuth")
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
                            schema = @Schema(implementation = RewardGiveResponse.class)
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

    @Operation(summary = "상점 지급 이력 조회",
               description = "1) 사용자별 조회: ?user_id=1\n2) 날짜/학년/반별 조회: ?date=2025-08-05&grade=3&class=2\n\n" +
                       "※ 데이터가 없을 경우 빈 배열([])을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공 (데이터가 없을 경우 빈 배열 반환)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RewardHistoryResponse.class),
                            examples = {
                                    @ExampleObject(name = "사용자별 조회 - 데이터 있음",
                                            value = "{\"userId\": 1, \"rewards\": [{\"rewardId\": 12, \"itemId\": 5, \"itemName\": \"무선 이어폰\", \"givenBy\": \"김선생\", \"givenAt\": \"2025-07-31\"}]}"),
                                    @ExampleObject(name = "사용자별 조회 - 데이터 없음",
                                            value = "{\"userId\": 1, \"rewards\": []}"),
                                    @ExampleObject(name = "날짜/학년/반별 조회 - 데이터 없음",
                                            value = "{\"histories\": []}")
                            }
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"user_id만 제공하거나 date, grade, class를 모두 제공해야 합니다.\"}")
                    )),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"로그인이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (TEACHER 또는 ADMIN만 가능)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"접근 권한이 없습니다.\"}")
                    ))
    })
    @GetMapping("/history")
    public ResponseEntity<?> getRewardHistory(
            @Parameter(description = "상점 지급 이력 조회 요청 파라미터")
            @ParameterObject
            @Valid @ModelAttribute RewardHistoryRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
         Object response = rewardQueryService.getRewardHistory(request, currentUser);
        return ResponseEntity.ok(response);
    }
}
