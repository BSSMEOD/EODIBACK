package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.presentation.dto.ItemDetailResponse;
import com.eod.eod.domain.place.infrastructure.PlaceRepository;
import com.eod.eod.domain.place.model.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemDetailService {

    private final ItemRepository itemRepository;
    private final PlaceRepository placeRepository;

    public ItemDetailResponse getItemDetail(Long itemId) {
        // 아이템 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 물품을 찾을 수 없습니다."));

        // 장소 조회
        Place place = placeRepository.findById(item.getFoundPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 장소입니다."));

        // DTO 변환
        return ItemDetailResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .imageUrl(item.getImage())
                .foundAt(item.getFoundAt())
                .foundPlace(place.getPlace())
                .foundPlaceDetail(item.getFoundPlaceDetail())
                .build();
    }
}
