package com.eod.eod.domain.item.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "분실물 등록 폼")
public class ItemRegistrationForm {

    @NotBlank(message = "필수 항목이 누락되었습니다.")
    @Size(min = 1, max = 100, message = "물품 이름은 1~100자 사이로 입력해주세요.")
    @Schema(description = "물품 이름", example = "아이패드", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "필수 항목이 누락되었습니다.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "습득 일자는 yyyy-MM-dd 형식이어야 합니다.")
    @Schema(description = "습득 일자(yyyy-MM-dd)", example = "2025-01-12", requiredMode = Schema.RequiredMode.REQUIRED)
    private String foundDate;

    @NotNull(message = "필수 항목이 누락되었습니다.")
    @Schema(description = "습득 장소 ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long placeId;

    @NotBlank(message = "필수 항목이 누락되었습니다.")
    @Size(max = 255, message = "장소 설명은 최대 255자까지 입력 가능합니다.")
    @Schema(description = "구체적인 장소 설명", example = "본관 2층 로비", requiredMode = Schema.RequiredMode.REQUIRED)
    private String placeDetail;

    @Schema(description = "분실물 이미지 파일 (JPEG/PNG)")
    private MultipartFile image;

}
