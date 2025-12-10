package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.exception.InvalidParameterException;
import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.presentation.dto.ItemSearchResponse;
import com.eod.eod.domain.item.presentation.dto.ItemSummaryResponse;
import com.eod.eod.domain.place.infrastructure.PlaceRepository;
import com.eod.eod.domain.place.model.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemSearchService {

    private final ItemRepository itemRepository;
    private final PlaceRepository placeRepository;

    public ItemSearchResponse searchItems(Long placeId, String status, int page, int size) {
        // 페이징 설정 (최신순 정렬)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "foundAt"));

        // 상태 변환
        Item.ItemStatus itemStatus = parseStatus(status);

        // 검색 조건에 따라 쿼리 실행
        Page<Item> itemPage = executeSearch(placeId, itemStatus, pageable);

        // Place 정보를 미리 조회하여 Map에 캐싱 (N+1 문제 방지)
        Map<Long, String> placeMap = buildPlaceMap(itemPage.getContent());

        // Item -> ItemSummaryDto 변환
        Page<ItemSummaryResponse> dtoPage = itemPage.map(item -> toSummaryDto(item, placeMap));

        return ItemSearchResponse.from(dtoPage);
    }

    private Item.ItemStatus parseStatus(String status) {
        try {
            return Item.ItemStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException("유효하지 않은 상태 값입니다: " + status);
        }
    }

    private Page<Item> executeSearch(Long placeId, Item.ItemStatus status, Pageable pageable) {
        if (placeId != null) {
            return itemRepository.findByFoundPlaceIdAndStatus(placeId, status, pageable);
        } else {
            return itemRepository.findByStatus(status, pageable);
        }
    }

    private Map<Long, String> buildPlaceMap(List<Item> items) {
        Map<Long, String> placeMap = new HashMap<>();
        items.forEach(item -> {
            Long placeId = item.getFoundPlaceId();
            if (!placeMap.containsKey(placeId)) {
                placeRepository.findById(placeId)
                        .ifPresent(place -> placeMap.put(placeId, getPlaceName(place)));
            }
        });
        return placeMap;
    }

    private String getPlaceName(Place place) {
        return place.getPlace();
    }

    private ItemSummaryResponse toSummaryDto(Item item, Map<Long, String> placeMap) {
        return ItemSummaryResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .foundDate(item.getFoundAt())
                .foundPlace(placeMap.getOrDefault(item.getFoundPlaceId(), ""))
                .placeDetail(item.getFoundPlaceDetail())
                .imageUrl(item.getImage())
                .status(item.getStatus().name())
                .build();
    }
}
