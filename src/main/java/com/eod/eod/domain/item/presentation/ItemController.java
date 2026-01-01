package com.eod.eod.domain.item.presentation;

import com.eod.eod.domain.item.application.ItemQueryService;
import com.eod.eod.domain.item.application.ItemRegistrationService;
    import com.eod.eod.domain.item.application.command.ItemRegistrationCommand;
import com.eod.eod.domain.item.application.command.ItemUpdateCommand;
import com.eod.eod.domain.item.presentation.dto.request.ItemRegistrationForm;
import com.eod.eod.domain.item.presentation.dto.request.ItemSearchRequest;
import com.eod.eod.domain.item.presentation.dto.request.ItemUpdateForm;
import com.eod.eod.domain.item.presentation.dto.response.ItemCreateResponse;
import com.eod.eod.domain.item.presentation.dto.response.ItemDetailResponse;
import com.eod.eod.domain.item.presentation.dto.response.ItemSearchResponse;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Item", description = "물품 기본 관리 API")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemRegistrationService itemRegistrationService;
    private final ItemQueryService itemQueryService;

    @Operation(summary = "분실물 등록", description = "JSON 형식으로 분실물을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "분실물 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemCreateResponse.class)
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
    @PostMapping
    public ResponseEntity<ItemCreateResponse> registerItem(
            @Parameter(description = "분실물 등록 폼", required = true)
            @Valid @RequestBody ItemRegistrationForm form,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        ItemRegistrationCommand command = new ItemRegistrationCommand(
                form.getName(),
                form.getReporterStudentCode(),
                form.getReporterName(),
                form.getFoundAt(),
                form.getPlaceId(),
                form.getPlaceDetail(),
                form.getImageUrl(),
                form.getCategory()
        );
        Long itemId = itemRegistrationService.registerItem(command, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ItemCreateResponse.success(itemId));
    }

    @Operation(summary = "분실물 검색", description = "장소 ID 리스트, 상태, 습득일 기간, 카테고리로 분실물을 검색합니다. 페이징을 지원합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemSearchResponse.class)
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 상태 값 또는 카테고리)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"유효하지 않은 상태 값입니다: INVALID\"}")
                    ))
    })
    @GetMapping("/search")
    public ResponseEntity<ItemSearchResponse> searchItems(
            @Parameter(description = "분실물 검색 요청 파라미터")
            @Valid ItemSearchRequest request
    ) {
        ItemSearchResponse response = itemQueryService.searchItems(
                request.getQuery(),
                request.getPlaceIds(),
                request.getStatus(),
                request.getFoundAtFrom(),
                request.getFoundAtTo(),
                request.getCategory(),
                request.getPage(),
                request.getSize()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "물품 상세 조회", description = "특정 물품의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemDetailResponse.class)
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
        ItemDetailResponse response = itemQueryService.getItemDetail(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "물품 정보 수정", description = "물품의 정보를 수정합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "필수 값 누락 또는 잘못된 입력",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\":\"필수 항목이 누락되었습니다.\"}")
                    )),
            @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\":\"ADMIN 권한이 없습니다.\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "물품을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\":\"해당 물품을 찾을 수 없습니다.\"}")
                    ))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateItem(
            @Parameter(description = "수정할 물품 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "물품 수정 폼", required = true)
            @Valid @RequestBody ItemUpdateForm form,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser
    ) {
        ItemUpdateCommand command = new ItemUpdateCommand(
                form.getName(),
                form.getReporterStudentCode(),
                form.getReporterName(),
                form.getFoundAt(),
                form.getPlaceId(),
                form.getPlaceDetail(),
                form.getImageUrl(),
                form.getCategory()
        );
        itemRegistrationService.updateItem(id, command, currentUser);
        return ResponseEntity.ok().build();
    }
}
