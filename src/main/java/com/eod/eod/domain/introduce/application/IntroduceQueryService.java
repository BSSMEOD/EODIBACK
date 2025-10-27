package com.eod.eod.domain.introduce.application;

import com.eod.eod.domain.introduce.infrastructure.IntroduceRepository;
import com.eod.eod.domain.introduce.model.Introduce;
import com.eod.eod.domain.introduce.presentation.dto.IntroduceQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntroduceQueryService {

    private final IntroduceRepository introduceRepository;

    // 소개 페이지 조회 (모든 사용자 접근 가능)
    public IntroduceQueryResponse getIntroduce() {
        // 가장 최근 소개 페이지 조회
        Introduce introduce = introduceRepository.findFirstByOrderByIdDesc()
                .orElseThrow(() -> new IllegalArgumentException("소개 페이지를 찾을 수 없습니다."));

        return IntroduceQueryResponse.from(introduce);
    }
}