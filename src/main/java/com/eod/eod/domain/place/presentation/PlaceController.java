package com.eod.eod.domain.place.presentation;


import com.eod.eod.domain.place.application.PlaceServiceImpl;
import com.eod.eod.domain.place.model.Place;
import com.eod.eod.domain.place.presentation.dto.response.PlaceResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Place", description = "장소 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    private final PlaceServiceImpl placeServiceImpl;

    @Operation(summary = "장소 목록 조회", description = "등록된 모든 장소 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlaceResponseDto.class)
                    ))
    })
    @GetMapping
    public ResponseEntity<List<PlaceResponseDto>> findAll() {
        List<PlaceResponseDto> places = placeServiceImpl.findAll();
        return new ResponseEntity<>(places, HttpStatus.OK);

    }

}
