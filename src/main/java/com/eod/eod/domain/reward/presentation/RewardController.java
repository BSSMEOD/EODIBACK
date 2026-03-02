package com.eod.eod.domain.reward.presentation;

import com.eod.eod.domain.reward.application.RewardGiveService;
import com.eod.eod.domain.reward.application.RewardQueryService;
import com.eod.eod.domain.reward.presentation.dto.request.RewardGiveRequest;
import com.eod.eod.domain.reward.presentation.dto.request.RewardHistoryRequest;
import com.eod.eod.domain.reward.presentation.dto.response.RewardEligibleCountResponse;
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
        rewardGiveService.giveRewardToStudent(request.getItemId(), currentUser);
        return ResponseEntity.ok(RewardGiveResponse.success());
    }

    @Operation(summary = "상점 지급 가능 여부 조회", description = "특정 물품의 습득 신고자 정보 및 상점 지급 현황을 조회합니다. (교사 전용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RewardEligibleResponse.class))),
            @ApiResponse(responseCode = "404", description = "물품을 찾을 수 없음")
    })
    @GetMapping("/eligible/{itemId}")
    public ResponseEntity<RewardEligibleResponse> getRewardEligible(
            @PathVariable Long itemId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        RewardEligibleResponse response = rewardQueryService.getRewardEligible(itemId, currentUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상점 지급 대기 건수 조회", description = "지급 완료된 물품 중 습득 신고자가 있고 아직 상점이 지급되지 않은 건수를 조회합니다. 상점 지급 시 자동 차감됩니다. (교사 전용)")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RewardEligibleCountResponse.class)))
    @GetMapping("/eligible/count")
    public ResponseEntity<RewardEligibleCountResponse> getRewardEligibleCount(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser
    ) {
        long count = rewardQueryService.countRewardEligibleItems(currentUser);
        return ResponseEntity.ok(RewardEligibleCountResponse.of(count));
    }

    @Operation(summary = "상점 지급 이력 검색",
               description = "**동적 필터링 검색**\n\n" +
                       "모든 파라미터는 선택적이며, 조합하여 사용할 수 있습니다.\n\n" +
                       "**검색 파라미터:**\n" +
                       "- `userId`: 특정 사용자로 필터링\n" +
                       "- `itemId`: 특정 아이템으로 필터링\n" +
                       "- `from`: 시작 날짜 (inclusive, yyyy-MM-dd)\n" +
                       "- `to`: 종료 날짜 (exclusive, yyyy-MM-dd)\n" +
                       "- `grade`: 학년으로 필터링\n" +
                       "- `class`: 반으로 필터링\n\n" +
                       "**레거시 파라미터 (하위 호환성):**\n" +
                       "- `user_id`: userId와 동일\n" +
                       "- `date`: from과 동일\n\n" +
                       "**예시:**\n" +
                       "- `/rewards/history` - 전체 조회\n" +
                       "- `/rewards/history?userId=3` - 사용자 3번의 이력\n" +
                       "- `/rewards/history?itemId=10` - 아이템 10번이 지급된 이력\n" +
                       "- `/rewards/history?from=2025-12-01&to=2025-12-31` - 12월 이력\n" +
                       "- `/rewards/history?userId=3&from=2025-12-01` - 사용자 3번의 12월 이후 이력\n" +
                       "- `/rewards/history?grade=3&class=2` - 3학년 2반 이력\n\n" +
                       "※ 데이터가 없을 경우 빈 배열([])을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공 (데이터가 없을 경우 빈 배열 반환)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RewardHistoryResponse.class),
                            examples = {
                                    @ExampleObject(name = "검색 결과 있음",
                                            value = "{\"userId\": null, \"rewards\": [{\"rewardId\": 12, \"itemId\": 5, \"itemName\": \"무선 이어폰\", \"givenBy\": \"김선생\", \"givenAt\": \"2025-07-31\"}]}"),
                                    @ExampleObject(name = "검색 결과 없음",
                                            value = "{\"userId\": null, \"rewards\": []}")
                            }
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
    public ResponseEntity<RewardHistoryResponse> getRewardHistory(
            @Parameter(description = "상점 지급 이력 검색 파라미터 (모두 선택적)")
            @ParameterObject
            @Valid @ModelAttribute RewardHistoryRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        RewardHistoryResponse response = rewardQueryService.searchRewardHistory(request, currentUser);
        return ResponseEntity.ok(response);
    }
}
