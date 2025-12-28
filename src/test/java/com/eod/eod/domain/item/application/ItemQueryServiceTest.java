package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemRepositoryCustom;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.presentation.dto.response.ItemSearchResponse;
import com.eod.eod.domain.place.infrastructure.PlaceRepository;
import com.eod.eod.domain.place.model.Place;
import com.eod.eod.domain.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemQueryServiceTest {

    @Mock
    private ItemFacade itemFacade;

    @Mock
    private ItemRepositoryCustom itemRepository;

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private ItemQueryService itemQueryService;

    @Test
    void placeIds가_null_제외_후_IN_검색된다() {
        // given
        List<Long> placeIds = Arrays.asList(1L, null, 2L);
        Item item = createItem(1L, Item.ItemStatus.LOST);
        ReflectionTestUtils.setField(item, "id", 10L);

        Place place = new Place();
        ReflectionTestUtils.setField(place, "id", 1L);
        ReflectionTestUtils.setField(place, "place", "도서관");
        when(placeRepository.findAllById(any())).thenReturn(List.of(place));

        when(itemRepository.searchItems(isNull(),anyList(), eq(Item.ItemStatus.LOST), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "foundAt")), 1));

        // when
        ItemSearchResponse response = itemQueryService.searchItems(null, placeIds, "LOST", null, null, null, 1, 5);

        // then
        ArgumentCaptor<List<Long>> placeIdsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(itemRepository).searchItems(isNull(), placeIdsCaptor.capture(), eq(Item.ItemStatus.LOST), isNull(), isNull(), isNull(), pageableCaptor.capture());

        assertThat(placeIdsCaptor.getValue()).containsExactly(1L, 2L);

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("foundAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("foundAt").getDirection()).isEqualTo(Sort.Direction.DESC);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getFoundPlace()).isEqualTo("도서관");
    }

    @Test
    void placeIds_null이고_status_공백이면_필터_없이_검색된다() {
        // given
        when(itemRepository.searchItems(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        // when
        ItemSearchResponse response = itemQueryService.searchItems(null, null, "   ", null, null, null, 1, 10);

        // then
        verify(itemRepository).searchItems(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
        verifyNoInteractions(placeRepository);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(10);
    }

    @Test
    void status_소문자도_ENUM으로_파싱된다() {
        // given
        Item item = createItem(1L, Item.ItemStatus.LOST);
        when(itemRepository.searchItems(isNull(), isNull(), eq(Item.ItemStatus.LOST), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1));
        Place place = new Place();
        ReflectionTestUtils.setField(place, "id", 1L);
        ReflectionTestUtils.setField(place, "place", "기숙사");
        when(placeRepository.findAllById(any())).thenReturn(List.of(place));

        // when
        itemQueryService.searchItems(null, null, "lost", null, null, null, 1, 10);

        // then
        verify(itemRepository).searchItems(isNull(), isNull(), eq(Item.ItemStatus.LOST), isNull(), isNull(), isNull(), any(Pageable.class));
        verify(placeRepository).findAllById(any());
    }

    @Test
    void 동일_장소_ID는_한번만_조회한다() {
        // given
        Item first = createItem(1L, Item.ItemStatus.LOST);
        Item second = createItem(1L, Item.ItemStatus.LOST);
        when(itemRepository.searchItems(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second), PageRequest.of(0, 10), 2));

        Place place = new Place();
        ReflectionTestUtils.setField(place, "id", 1L);
        ReflectionTestUtils.setField(place, "place", "본관");
        when(placeRepository.findAllById(any())).thenReturn(List.of(place));

        // when
        itemQueryService.searchItems(null, null, null, null, null, null, 1, 10);

        // then
        verify(placeRepository, times(1)).findAllById(any());
    }

    @Test
    void 장소_없으면_빈_문자열로_반환한다() {
        // given
        Item item = createItem(99L, Item.ItemStatus.LOST);
        when(itemRepository.searchItems(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1));
        when(placeRepository.findAllById(any())).thenReturn(List.of());

        // when
        ItemSearchResponse response = itemQueryService.searchItems(null, null, null, null, null, null, 1, 10);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getFoundPlace()).isEmpty();
    }

    private Item createItem(Long placeId, Item.ItemStatus status) {
        User admin = User.builder()
                .oauthProvider("local")
                .oauthId("admin")
                .name("Admin")
                .email("admin@test.com")
                .role(User.Role.ADMIN)
                .build();

        User student = User.builder()
                .oauthProvider("local")
                .oauthId("student")
                .name("Student")
                .email("student@test.com")
                .role(User.Role.USER)
                .build();

        return Item.builder()
                .student(student)
                .admin(admin)
                .foundPlaceId(placeId)
                .foundPlaceDetail("상세 위치")
                .name("테스트 물품")
                .reporterName("테스트 신고자")
                .image("image.jpg")
                .status(status)
                .category(Item.ItemCategory.ETC)
                .foundAt(LocalDateTime.now().minusDays(1))
                .build();
    }
}
