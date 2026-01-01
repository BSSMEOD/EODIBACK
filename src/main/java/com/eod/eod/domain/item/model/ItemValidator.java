package com.eod.eod.domain.item.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Item 엔티티의 검증 로직을 담당하는 유틸리티 클래스
 */
public final class ItemValidator {

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private ItemValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 습득 일자 검증
     */
    public static void validateFoundAt(LocalDateTime foundAt) {
        if (foundAt == null) {
            throw new IllegalArgumentException("습득 일자는 필수입니다.");
        }
        if (foundAt.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("과거 날짜만 등록할 수 있습니다.");
        }
    }

    /**
     * 카테고리 검증
     */
    public static void validateCategory(Item.ItemCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
    }

    /**
     * 장소 ID 필수 검증
     */
    public static Long requirePlaceId(Long placeId) {
        if (placeId == null) {
            throw new IllegalArgumentException("장소 ID는 필수입니다.");
        }
        return placeId;
    }

    /**
     * 텍스트 필수 검증
     */
    public static String requireText(String value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    /**
     * 이미지 파일 검증
     */
    public static void validateImage(long size, String contentType) {
        if (size > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalStateException("이미지는 최대 5MB까지 업로드할 수 있습니다.");
        }
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalStateException("이미지는 JPEG 또는 PNG 형식만 허용됩니다.");
        }
    }
}
