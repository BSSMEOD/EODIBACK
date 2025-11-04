package com.eod.eod.domain.place.application;


import com.eod.eod.domain.place.infrastructure.PlaceRepository;
import com.eod.eod.domain.place.model.Place;
import com.eod.eod.domain.place.presentation.dto.PlaceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;

    public List<PlaceResponseDto> findAll() {
        List<Place> places = placeRepository.findAll();

        return places.stream()
                .map(place -> place.toResponseDto(place))
                .toList();
    }
}
