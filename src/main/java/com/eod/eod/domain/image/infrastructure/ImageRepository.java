package com.eod.eod.domain.image.infrastructure;

import com.eod.eod.domain.image.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
