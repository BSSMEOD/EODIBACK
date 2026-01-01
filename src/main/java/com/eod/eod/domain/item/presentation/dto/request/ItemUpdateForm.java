package com.eod.eod.domain.item.presentation.dto.request;

import com.eod.eod.domain.item.model.Item;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemUpdateForm {

    @NotBlank(message = "필수 항목이 누락되었습니다.")
    @Size(min = 1, max = 100, message = "물품 이름은 1~100자 사이로 입력해주세요.")
    @Schema(description = "물품 이름", example = "아이패드", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "필수 항목이 누락되었습니다.")
    @Size(min = 1101, max = 3417, message = "신고자 학생 코드는 1101에서 3417 사이여야 합니다.")
    @Schema(description = "신고자 학생 코드", example = "2109", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reporterStudentCode;

    @Size(max = 50, message = "신고자 이름은 최대 50자까지 입력 가능합니다.")
    @Schema(description = "신고자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reporterName;

    @NotBlank(message = "필수 항목이 누락되었습니다.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "습득 일자는 yyyy-MM-dd 형식이어야 합니다.")
    @Schema(description = "습득 일자(yyyy-MM-dd)", example = "2025-01-12", requiredMode = Schema.RequiredMode.REQUIRED)
    private String foundAt;

    @NotNull(message = "필수 항목이 누락되었습니다.")
    @Schema(description = "습득 장소 ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long placeId;

    @NotBlank(message = "필수 항목이 누락되었습니다.")
    @Size(max = 255, message = "장소 설명은 최대 255자까지 입력 가능합니다.")
    @Schema(description = "구체적인 장소 설명", example = "본관 2층 로비", requiredMode = Schema.RequiredMode.REQUIRED)
    private String placeDetail;

    @Schema(description = "분실물 이미지 URL")
    private String imageUrl;

    @NotNull(message = "필수 항목이 누락되었습니다.")
    @Schema(
        description = "물품 카테고리",
        example = "전자기기",
        allowableValues = {"교복", "체육복", "단체복", "사복", "무선 이어폰", "전자기기", "안경", "기타"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Item.ItemCategory category;

}
