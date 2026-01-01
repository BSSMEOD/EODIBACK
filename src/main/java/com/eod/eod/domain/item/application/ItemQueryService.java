package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemRepositoryCustom;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.presentation.dto.response.ItemDetailResponse;
import com.eod.eod.domain.item.presentation.dto.response.ItemSearchResponse;
import com.eod.eod.domain.item.presentation.dto.response.ItemSummaryResponse;
import com.eod.eod.domain.place.infrastructure.PlaceRepository;
import com.eod.eod.domain.place.model.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemQueryService {

    private final ItemFacade itemFacade;
    private final ItemRepositoryCustom itemRepository;
    private final PlaceRepository placeRepository;

    public ItemDetailResponse getItemDetail(Long itemId) {
        Item item = itemFacade.getItemById(itemId);

        Place place = placeRepository.findById(item.getFoundPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 장소입니다."));

        return ItemDetailResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .reporterName(item.getReporterName())
                .imageUrl(item.getImage())
                .foundAt(item.getFoundAt())
                .foundPlace(place.getPlace())
                .foundPlaceDetail(item.getFoundPlaceDetail())
                .category(item.getCategory())
                .build();
    }

    public ItemSearchResponse searchItems(String query, List<Long> placeIds, String status,
                                          LocalDate foundAtFrom, LocalDate foundAtTo,
                                          List<String> categories, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "foundAt"));

        Item.ItemStatus itemStatus = parseStatus(status);
        List<Item.ItemCategory> itemCategories = parseCategories(categories);
        String trimmedQuery = parseQuery(query);
        List<Long> filteredPlaceIds = filterNullPlaceIds(placeIds);

        Page<Item> itemPage = itemRepository.searchItems(trimmedQuery, filteredPlaceIds, itemStatus,
                                                          foundAtFrom, foundAtTo,
                                                          itemCategories, pageable);

        Map<Long, String> placeMap = buildPlaceMap(itemPage.getContent());

        Page<ItemSummaryResponse> dtoPage = itemPage.map(item -> toSummaryDto(item, placeMap));

        return ItemSearchResponse.from(dtoPage);
    }

    private Item.ItemStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return Item.ItemStatus.valueOf(status.toUpperCase());
    }

    private List<Item.ItemCategory> parseCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return categories.stream()
                .filter(category -> category != null && !category.isBlank())
                .map(Item.ItemCategory::from)
                .toList();
    }

    private String parseQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmedQuery = query.trim();
        return trimmedQuery.isEmpty() ? null : trimmedQuery;
    }
    
    private List<Long> filterNullPlaceIds(List<Long> placeIds) {
        if (placeIds == null) {
            return null;
        }
        return placeIds.stream()
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private Map<Long, String> buildPlaceMap(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        
        Set<Long> placeIds = items.stream()
                .map(Item::getFoundPlaceId)
                .collect(Collectors.toSet());

        return placeRepository.findAllById(placeIds).stream()
                .collect(Collectors.toMap(Place::getId, this::getPlaceName));
    }

    private String getPlaceName(Place place) {
        return place.getPlace();
    }

    private ItemSummaryResponse toSummaryDto(Item item, Map<Long, String> placeMap) {
        return ItemSummaryResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .reporterName(item.getReporterName())
                .foundAt(item.getFoundAt())
                .foundPlace(placeMap.getOrDefault(item.getFoundPlaceId(), ""))
                .placeDetail(item.getFoundPlaceDetail())
                .imageUrl(item.getImage())
                .status(item.getStatus().name())
                .category(item.getCategory())
                .build();
    }
}
