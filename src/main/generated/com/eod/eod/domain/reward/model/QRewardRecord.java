package com.eod.eod.domain.reward.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRewardRecord is a Querydsl query type for RewardRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRewardRecord extends EntityPathBase<RewardRecord> {

    private static final long serialVersionUID = -991355825L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRewardRecord rewardRecord = new QRewardRecord("rewardRecord");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.eod.eod.domain.item.model.QItem item;

    public final com.eod.eod.domain.user.model.QUser student;

    public final com.eod.eod.domain.user.model.QUser teacher;

    public QRewardRecord(String variable) {
        this(RewardRecord.class, forVariable(variable), INITS);
    }

    public QRewardRecord(Path<? extends RewardRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRewardRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRewardRecord(PathMetadata metadata, PathInits inits) {
        this(RewardRecord.class, metadata, inits);
    }

    public QRewardRecord(Class<? extends RewardRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.item = inits.isInitialized("item") ? new com.eod.eod.domain.item.model.QItem(forProperty("item"), inits.get("item")) : null;
        this.student = inits.isInitialized("student") ? new com.eod.eod.domain.user.model.QUser(forProperty("student")) : null;
        this.teacher = inits.isInitialized("teacher") ? new com.eod.eod.domain.user.model.QUser(forProperty("teacher")) : null;
    }

}

