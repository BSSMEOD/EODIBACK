package com.eod.eod.domain.introduce.presentation;

import com.eod.eod.domain.introduce.application.IntroduceService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/introduce")
@RequiredArgsConstructor
@Tag(name = "Introduce", description = "소개 페이지 관리 API")
public class IntroduceController {

    private final IntroduceService introduceService;

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