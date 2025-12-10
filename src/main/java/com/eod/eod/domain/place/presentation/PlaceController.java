package com.eod.eod.domain.place.presentation;


import com.eod.eod.domain.place.application.PlaceServiceImpl;
import com.eod.eod.domain.place.model.Place;
import com.eod.eod.domain.place.presentation.dto.response.PlaceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    private final PlaceServiceImpl placeServiceImpl;

    @GetMapping
    public ResponseEntity<List<PlaceResponseDto>> findAll() {
        List<PlaceResponseDto> places = placeServiceImpl.findAll();
        return new ResponseEntity<>(places, HttpStatus.OK);

    }

}
