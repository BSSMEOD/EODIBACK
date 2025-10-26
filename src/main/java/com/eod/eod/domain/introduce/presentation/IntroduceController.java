package com.eod.eod.domain.introduce.presentation;

import com.eod.eod.domain.introduce.application.IntroduceQueryService;
import com.eod.eod.domain.introduce.application.IntroduceService;
import com.eod.eod.domain.introduce.presentation.dto.IntroduceQueryResponse;
import com.eod.eod.domain.introduce.presentation.dto.IntroduceUpdateRequest;
import com.eod.eod.domain.introduce.presentation.dto.IntroduceUpdateResponse;
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
@RequestMapping("/introduce")
@RequiredArgsConstructor
@Tag(name = "Introduce", description = "소개 페이지 관리 API")
public class IntroduceController {

    private final IntroduceService introduceService;
    private final IntroduceQueryService introduceQueryService;

    @Operation(summary = "소개 페이지 조회", description = "모든 사용자가 소개 페이지를 조회할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "소개 페이지 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IntroduceQueryResponse.class),
                            examples = @ExampleObject(value = "{\"content\": \"분실물 관리 서비스 '어디'입니다. 사용 방법은 1) 검색으로 분실물을 찾아보고 2) 없다면 등록을 통해 제보를 남겨주세요.\", \"updated_at\": \"2025-10-24T00:00:00Z\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "소개 페이지를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"소개 페이지를 찾을 수 없습니다.\"}")
                    )
            )
    })
    @GetMapping
    public ResponseEntity<IntroduceQueryResponse> getIntroduce() {
        IntroduceQueryResponse response = introduceQueryService.getIntroduce();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소개 페이지 수정", description = "관리자가 소개 페이지 내용을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "소개 페이지 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IntroduceUpdateResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"소개 페이지가 성공적으로 수정되었습니다.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (ADMIN 권한 필요)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"접근 권한이 없습니다.\"}")
                    )
            )
    })
    @PatchMapping
    public ResponseEntity<IntroduceUpdateResponse> updateIntroduce(
            @Valid @RequestBody IntroduceUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {

        IntroduceUpdateResponse response = introduceService.updateIntroduce(request.getContent(), currentUser);
        return ResponseEntity.ok(response);
    }
}