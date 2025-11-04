package com.eod.eod.domain.place.infrastructure;

import com.eod.eod.domain.place.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place,Long> {
}
