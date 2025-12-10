package com.eod.eod.domain.introduce.application;

import com.eod.eod.domain.introduce.infrastructure.IntroduceRepository;
import com.eod.eod.domain.introduce.model.Introduce;
import com.eod.eod.domain.introduce.presentation.dto.response.IntroduceUpdateResponse;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IntroduceService {

    private final IntroduceRepository introduceRepository;

    @Transactional
    public IntroduceUpdateResponse updateIntroduce(String content, User currentUser) {
        // 가장 최근 소개 페이지 조회 또는 새로 생성
        Introduce introduce = introduceRepository.findFirstByOrderByIdDesc()
                .orElseGet(() -> new Introduce(content));

        // 도메인 메서드를 통한 수정 (도메인에서 권한 검증)
        introduce.updateContent(content, currentUser);

        // 새로 생성한 경우에만 저장 (기존 엔티티는 더티 체킹으로 자동 업데이트)
        if (introduce.getId() == null) {
            introduceRepository.save(introduce);
        }

        return IntroduceUpdateResponse.success();
    }
}
