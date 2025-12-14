package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.model.QItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Page<Item> searchItems(List<Long> placeIds, Item.ItemStatus status, Pageable pageable) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QItem item = QItem.item;

        // 동적 쿼리 조건 생성
        BooleanBuilder builder = new BooleanBuilder();

        if (placeIds != null) {
            List<Long> filteredPlaceIds = placeIds.stream()
                    .filter(Objects::nonNull)
                    .toList();
            if (!filteredPlaceIds.isEmpty()) {
                builder.and(item.foundPlaceId.in(filteredPlaceIds));
            }
        }

        if (status != null) {
            builder.and(item.status.eq(status));
        }

        // 쿼리 실행
        List<Item> content = queryFactory
                .selectFrom(item)
                .where(builder)
                .orderBy(item.foundAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 쿼리 (최적화를 위해 별도 실행)
        JPAQuery<Long> countQuery = queryFactory
                .select(item.count())
                .from(item)
                .where(builder);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
