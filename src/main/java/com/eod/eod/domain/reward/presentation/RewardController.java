package com.eod.eod.domain.reward.presentation;

import com.eod.eod.domain.reward.application.RewardGiveService;
import com.eod.eod.domain.reward.application.RewardQueryService;
import com.eod.eod.domain.reward.presentation.dto.RewardGiveHistoryResponse;
import com.eod.eod.domain.reward.presentation.dto.RewardHistoryResponse;
import com.eod.eod.domain.reward.presentation.dto.RewardHistoryRequest;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
               description = "1) 사용자별 조회: ?user_id=1\n2) 날짜/학년/반별 조회: ?date=2025-08-05&grade=3&class=2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RewardHistoryResponse.class)
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
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"지급 이력이 없습니다.\"}")
                    ))
    })
    @GetMapping("/history")
    public ResponseEntity<RewardHistoryResponse> getRewardHistory(
            @Parameter(description = "상점 지급 이력 조회 요청 파라미터")
            @Valid @ModelAttribute RewardHistoryRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        // 상점 지급 이력 조회
        RewardHistoryResponse response = rewardQueryService.getRewardHistory(request.getUserId(), currentUser);

        return ResponseEntity.ok(response);
        // 파라미터 조합에 따라 분기
        if (userId != null) {
            // 사용자별 조회
            RewardHistoryResponse response = rewardQueryService.getRewardHistory(userId, currentUser);
            return ResponseEntity.ok(response);
        } else if (date != null && grade != null && classNumber != null) {
            // 날짜/학년/반별 조회
            RewardGiveHistoryResponse response = rewardQueryService.getGiveHistoryByDateAndClass(date, grade, classNumber, currentUser);
            return ResponseEntity.ok(response);
        } else {
            throw new IllegalArgumentException("user_id 또는 (date, grade, class) 조합이 필요합니다.");
        }
    }
}
