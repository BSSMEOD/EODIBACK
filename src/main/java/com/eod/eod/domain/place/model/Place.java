package com.eod.eod.domain.place.model;

import com.eod.eod.domain.place.presentation.dto.response.PlaceResponseDto;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "places")
@Getter
public class Place {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="place")
    String place;

    public PlaceResponseDto toResponseDto(Place place){
        PlaceResponseDto responseDto = new PlaceResponseDto();
        responseDto.setId(place.id);
        responseDto.setName(place.place);
        return responseDto;
    }

}
