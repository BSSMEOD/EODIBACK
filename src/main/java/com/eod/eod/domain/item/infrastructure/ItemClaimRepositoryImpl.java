package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.item.model.QItem;
import com.eod.eod.domain.item.model.QItemClaim;
import com.eod.eod.domain.item.presentation.dto.response.ClaimItemResponse;
import com.eod.eod.domain.place.model.QPlace;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ItemClaimRepositoryImpl implements ItemClaimRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<ClaimItemResponse> findItemsWithPendingClaims() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QItem item = QItem.item;
        QItemClaim claim = QItemClaim.itemClaim;
        QPlace place = QPlace.place1;

        return queryFactory
                .select(Projections.constructor(
                        ClaimItemResponse.class,
                        item.id,
                        item.name,
                        item.foundAt,
                        place.place,
                        item.image,
                        claim.id.count()
                ))
                .from(item)
                .join(claim).on(claim.item.id.eq(item.id))
                .join(place).on(place.id.eq(item.foundPlaceId))
                .where(
                        claim.status.eq(ItemClaim.ClaimStatus.PENDING),
                        item.status.notIn(Item.ItemStatus.DISCARDED, Item.ItemStatus.GIVEN)
                )
                .groupBy(item.id, item.name, item.foundAt, place.place, item.image)
                .having(claim.id.count().gt(0))
                .fetch();
    }
}
