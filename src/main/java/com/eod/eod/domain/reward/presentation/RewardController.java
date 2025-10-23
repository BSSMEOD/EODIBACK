package com.eod.eod.domain.reward.presentation;

import com.eod.eod.domain.reward.application.RewardQueryService;
import com.eod.eod.domain.reward.presentation.dto.RewardHistoryResponse;
import com.eod.eod.domain.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reward", description = "상점 관리 API")
@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardQueryService rewardQueryService;

    @Operation(summary = "상점 지급 이력 조회", description = "특정 사용자의 상점 지급 이력을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RewardHistoryResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "user_id": 1,
                                        "rewards": [
                                            {
                                                "reward_id": 12,
                                                "item_id": 5,
                                                "item_name": "무선 이어폰",
                                                "given_by": "김선생",
                                                "given_at": "2025-07-31"
                                            }
                                        ]
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"로그인이 필요합니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"접근 권한이 없습니다.\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"해당 사용자를 찾을 수 없습니다.\"}")
                    ))
    })
    @GetMapping("/history")
    public ResponseEntity<RewardHistoryResponse> getRewardHistory(
            @Parameter(description = "조회할 사용자 ID", required = true, example = "1")
            @RequestParam("user_id") Long userId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        // 상점 지급 이력 조회
        RewardHistoryResponse response = rewardQueryService.getRewardHistory(userId, currentUser);

        return ResponseEntity.ok(response);
    }
}
