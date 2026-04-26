package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItemRepositoryImplTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void searchItems는_상태_필터가_없어도_GIVEN_물품을_반환하지_않는다() {
        Item givenItem = persistItem(Item.ItemStatus.GIVEN);

        Page<Item> result = itemRepository.searchItems(
                null,
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "foundAt"))
        );

        assertThat(result.getContent()).doesNotContain(givenItem);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchItems는_GIVEN_상태를_명시해도_GIVEN_물품을_반환하지_않는다() {
        Item givenItem = persistItem(Item.ItemStatus.GIVEN);

        Page<Item> result = itemRepository.searchItems(
                null,
                null,
                List.of(Item.ItemStatus.GIVEN),
                null,
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "foundAt"))
        );

        assertThat(result.getContent()).doesNotContain(givenItem);
        assertThat(result.getContent()).isEmpty();
    }

    private Item persistItem(Item.ItemStatus status) {
        User admin = entityManager.persist(User.builder()
                .oauthProvider("local")
                .oauthId("admin-" + status.name())
                .name("Admin")
                .email("admin-" + status.name() + "@test.com")
                .role(User.Role.ADMIN)
                .build());

        Item item = Item.builder()
                .admin(admin)
                .foundPlaceId(1L)
                .foundPlaceDetail("상세 위치")
                .name("테스트 물품")
                .image("image.jpg")
                .status(status)
                .category(Item.ItemCategory.ETC)
                .foundAt(LocalDateTime.now().minusDays(1))
                .build();

        Item persisted = entityManager.persist(item);
        entityManager.flush();
        entityManager.clear();
        return persisted;
    }
}
