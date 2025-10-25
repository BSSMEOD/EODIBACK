package com.eod.eod.domain.introduce.infrastructure;

import com.eod.eod.domain.introduce.model.Introduce;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntroduceRepository extends JpaRepository<Introduce, Long> {

    // 가장 최근 소개 페이지 조회 (ID 내림차순 첫 번째)
    Optional<Introduce> findFirstByOrderByIdDesc();
}