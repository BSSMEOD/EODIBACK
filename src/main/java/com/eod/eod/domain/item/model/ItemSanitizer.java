package com.eod.eod.domain.item.model;

/**
 * Item 엔티티의 입력값 정제 로직을 담당하는 유틸리티 클래스
 */
public final class ItemSanitizer {

    private ItemSanitizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 물품 이름 정제
     */
    public static String sanitizeName(String name) {
        String trimmed = ItemValidator.requireText(name, "물품 이름은 필수입니다.");
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("물품 이름은 100자를 초과할 수 없습니다.");
        }
        return trimmed;
    }

    /**
     * 신고자 이름 정제
     */
    public static String sanitizeReporterName(String reporterName) {
        if (reporterName == null || reporterName.trim().isEmpty()) {
            return null;
        }
        String trimmed = reporterName.trim();
        if (trimmed.length() > 50) {
            throw new IllegalArgumentException("신고자 이름은 50자를 초과할 수 없습니다.");
        }
        return trimmed;
    }

    /**
     * 장소 설명 정제
     */
    public static String sanitizeDetail(String placeDetail) {
        String trimmed = ItemValidator.requireText(placeDetail, "장소 설명은 필수입니다.");
        if (trimmed.length() > 255) {
            throw new IllegalArgumentException("장소 설명은 255자를 초과할 수 없습니다.");
        }
        return trimmed;
    }
}
