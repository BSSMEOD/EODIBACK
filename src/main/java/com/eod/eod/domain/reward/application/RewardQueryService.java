package com.eod.eod.domain.reward.application;

import com.eod.eod.domain.reward.infrastructure.RewardRecordRepository;
import com.eod.eod.domain.reward.model.RewardRecord;
import com.eod.eod.domain.reward.presentation.dto.request.RewardHistoryRequest;
import com.eod.eod.domain.reward.presentation.dto.response.RewardGiveHistoryResponse;
import com.eod.eod.domain.reward.presentation.dto.response.RewardHistoryResponse;
import com.eod.eod.domain.user.infrastructure.UserRepository;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardQueryService {

    private final RewardRecordRepository rewardRecordRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Object getRewardHistory(RewardHistoryRequest request, User currentUser) {
        request.validate();

        if (request.isUserQuery()) {
            return getRewardHistory(request.getUserId(), currentUser);
        }

        return getGiveHistoryByDateAndClass(
                request.getDate(),
                request.getGrade(),
                request.getClassNumber(),
                currentUser
        );
    }

    // 상점 지급 이력 조회
    public RewardHistoryResponse getRewardHistory(Long userId, User currentUser) {
        // 권한 검증 (TEACHER 또는 ADMIN만 조회 가능)
        if (!currentUser.isTeacherOrAdmin()) {
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

    // 날짜, 학년, 반별 지급 내역 조회
    public RewardGiveHistoryResponse getGiveHistoryByDateAndClass(LocalDate date, Integer grade, Integer classNumber, User currentUser) {
        // 권한 검증 (TEACHER 또는 ADMIN만 조회 가능)
        if (!currentUser.isTeacherOrAdmin()) {
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
