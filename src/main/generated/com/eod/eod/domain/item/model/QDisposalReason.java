package com.eod.eod.domain.item.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDisposalReason is a Querydsl query type for DisposalReason
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDisposalReason extends EntityPathBase<DisposalReason> {

    private static final long serialVersionUID = 1242933472L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDisposalReason disposalReason = new QDisposalReason("disposalReason");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> extensionDays = createNumber("extensionDays", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QItem item;

    public final StringPath reason = createString("reason");

    public final com.eod.eod.domain.user.model.QUser teacher;

    public QDisposalReason(String variable) {
        this(DisposalReason.class, forVariable(variable), INITS);
    }

    public QDisposalReason(Path<? extends DisposalReason> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDisposalReason(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDisposalReason(PathMetadata metadata, PathInits inits) {
        this(DisposalReason.class, metadata, inits);
    }

    public QDisposalReason(Class<? extends DisposalReason> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.item = inits.isInitialized("item") ? new QItem(forProperty("item"), inits.get("item")) : null;
        this.teacher = inits.isInitialized("teacher") ? new com.eod.eod.domain.user.model.QUser(forProperty("teacher")) : null;
    }

}

