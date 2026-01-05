package com.eod.eod.domain.item.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QItem is a Querydsl query type for Item
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QItem extends EntityPathBase<Item> {

    private static final long serialVersionUID = -2146422178L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QItem item = new QItem("item");

    public final com.eod.eod.domain.user.model.QUser admin;

    public final EnumPath<Item.ApprovalStatus> approvalStatus = createEnum("approvalStatus", Item.ApprovalStatus.class);

    public final DateTimePath<java.time.LocalDateTime> approvedAt = createDateTime("approvedAt", java.time.LocalDateTime.class);

    public final com.eod.eod.domain.user.model.QUser approvedBy;

    public final EnumPath<Item.ItemCategory> category = createEnum("category", Item.ItemCategory.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> discardedAt = createDateTime("discardedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> foundAt = createDateTime("foundAt", java.time.LocalDateTime.class);

    public final StringPath foundPlaceDetail = createString("foundPlaceDetail");

    public final NumberPath<Long> foundPlaceId = createNumber("foundPlaceId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final StringPath name = createString("name");

    public final EnumPath<Item.ItemStatus> status = createEnum("status", Item.ItemStatus.class);

    public final com.eod.eod.domain.user.model.QUser student;

    public QItem(String variable) {
        this(Item.class, forVariable(variable), INITS);
    }

    public QItem(Path<? extends Item> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QItem(PathMetadata metadata, PathInits inits) {
        this(Item.class, metadata, inits);
    }

    public QItem(Class<? extends Item> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.admin = inits.isInitialized("admin") ? new com.eod.eod.domain.user.model.QUser(forProperty("admin")) : null;
        this.approvedBy = inits.isInitialized("approvedBy") ? new com.eod.eod.domain.user.model.QUser(forProperty("approvedBy")) : null;
        this.student = inits.isInitialized("student") ? new com.eod.eod.domain.user.model.QUser(forProperty("student")) : null;
    }

}

