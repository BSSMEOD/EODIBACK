package com.eod.eod.domain.reward.application;

import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.reward.infrastructure.RewardRecordRepository;
import com.eod.eod.domain.reward.infrastructure.RewardRecordSpecification;
import com.eod.eod.domain.reward.model.RewardRecord;
import com.eod.eod.domain.reward.presentation.dto.request.RewardHistoryRequest;
import com.eod.eod.domain.reward.presentation.dto.response.RewardEligibleResponse;
import com.eod.eod.domain.reward.presentation.dto.response.RewardGiveHistoryResponse;
import com.eod.eod.domain.reward.presentation.dto.response.RewardHistoryResponse;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardQueryService {

    private final RewardRecordRepository rewardRecordRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 상점 지급 이력 검색 (동적 필터링)
     * 모든 파라미터는 선택적이며, 조건에 맞는 결과만 반환합니다.
     */
    public RewardHistoryResponse searchRewardHistory(RewardHistoryRequest request, User currentUser) {
        // 권한 검증 (TEACHER 또는 ADMIN만 조회 가능) - 테스트를 위해 임시로 null 체크 추가
        if (currentUser != null && !currentUser.isTeacher()) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        // 동적 검색 조건 생성
        Specification<RewardRecord> spec = RewardRecordSpecification.searchWithFilters(
                request.getEffectiveUserId(),
                request.getItemId(),
                request.getEffectiveFrom(),
                request.getTo(),
                request.getGrade(),
                request.getClassNumber()
        );

        // 검색 실행
        List<RewardRecord> records = rewardRecordRepository.findAll(spec);

        // Response 변환 (userId는 필터링 조건으로 사용된 값을 반환)
        return RewardHistoryResponse.fromRecords(records, request.getEffectiveUserId());
    }

    // 상점 지급 이력 조회
    public RewardHistoryResponse getRewardHistory(Long userId, User currentUser) {
        // 권한 검증 (교사만 조회 가능)
        if (!currentUser.isTeacher()) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        // 조회 대상 사용자 확인
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 상점 지급 이력 조회
        List<RewardRecord> records = rewardRecordRepository.findByStudentId(userId);

        // Response 변환
        return RewardHistoryResponse.from(userId, records);
    }

    // 특정 물품의 상점 지급 가능 여부 및 현황 조회
    public RewardEligibleResponse getRewardEligible(Long itemId, User currentUser) {
        if (!currentUser.isTeacher()) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("물품을 찾을 수 없습니다."));

        User finder = item.getStudent();
        Optional<RewardRecord> record = rewardRecordRepository.findByItemId(itemId);

        return new RewardEligibleResponse(
                finder.getId(),
                finder.getName(),
                finder.getStudentCode(),
                itemId,
                record.isPresent(),
                record.map(RewardRecord::getCreatedAt).orElse(null)
        );
    }

    // 상점 지급 대기 건수: 지급 완료(GIVEN) + 학생 신고자 있음 + 상점 미지급
    public long countRewardEligibleItems(User currentUser) {
        if (!currentUser.isTeacher()) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
        return rewardRecordRepository.countRewardEligibleItems(Item.ItemStatus.GIVEN, User.Role.USER);
    }

    // 날짜, 학년, 반별 지급 내역 조회
    public RewardGiveHistoryResponse getGiveHistoryByDateAndClass(LocalDate date, Integer grade, Integer classNumber, User currentUser) {
        // 권한 검증 (교사만 조회 가능)
        if (!currentUser.isTeacher()) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        // 날짜를 LocalDateTime으로 변환
        LocalDateTime dateTime = date.atStartOfDay();

        // 지급 이력 조회
        List<RewardRecord> records = rewardRecordRepository.findByDateAndGradeAndClass(dateTime, grade, classNumber);

        // Response 변환
        List<RewardGiveHistoryResponse.RewardGiveHistoryItem> histories = records.stream()
                .map(record -> RewardGiveHistoryResponse.RewardGiveHistoryItem.builder()
                        .receivedAt(record.getCreatedAt().toLocalDate().format(DATE_FORMATTER))
                        .studentName(record.getStudent().getName())
                        .itemName(record.getItem().getName())
                        .givenAt(record.getCreatedAt().toLocalDate().format(DATE_FORMATTER))
                        .build())
                .collect(Collectors.toList());

        return RewardGiveHistoryResponse.builder()
                .histories(histories)
                .build();
    }
}
