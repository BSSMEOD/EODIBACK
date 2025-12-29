package com.eod.eod.domain.reward.infrastructure;

import com.eod.eod.domain.reward.model.RewardRecord;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RewardRecordSpecification {

    /**
     * 동적 검색 조건을 생성합니다.
     *
     * @param userId 학생 ID (선택)
     * @param itemId 아이템 ID (선택)
     * @param from 시작 날짜 (inclusive, 선택)
     * @param to 종료 날짜 (exclusive, 선택)
     * @param grade 학년 (선택)
     * @param classNumber 반 (선택)
     * @return Specification
     */
    public static Specification<RewardRecord> searchWithFilters(
            Long userId,
            Long itemId,
            LocalDate from,
            LocalDate to,
            Integer grade,
            Integer classNumber
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // userId 필터: student.id = ?
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("student").get("id"), userId));
            }

            // itemId 필터: item.id = ?
            if (itemId != null) {
                predicates.add(criteriaBuilder.equal(root.get("item").get("id"), itemId));
            }

            // from 필터: createdAt >= from (inclusive)
            if (from != null) {
                LocalDateTime fromDateTime = from.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }

            // to 필터: createdAt < to (exclusive)
            if (to != null) {
                LocalDateTime toDateTime = to.atStartOfDay();
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), toDateTime));
            }

            // grade 필터: student.grade = ?
            if (grade != null) {
                predicates.add(criteriaBuilder.equal(root.get("student").get("grade"), grade));
            }

            // classNumber 필터: student.classNo = ?
            if (classNumber != null) {
                predicates.add(criteriaBuilder.equal(root.get("student").get("classNo"), classNumber));
            }

            // 모든 조건을 AND로 결합
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
