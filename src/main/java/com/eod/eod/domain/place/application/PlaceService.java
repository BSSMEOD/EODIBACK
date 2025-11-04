package com.eod.eod.domain.place.application;

import com.eod.eod.domain.place.model.Place;
import com.eod.eod.domain.place.presentation.dto.PlaceResponseDto;

import java.util.List;

public interface PlaceService {
    List<PlaceResponseDto> findAll();
}
