package com.eod.eod.domain.item.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGiveRecord is a Querydsl query type for GiveRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGiveRecord extends EntityPathBase<GiveRecord> {

    private static final long serialVersionUID = 555107789L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGiveRecord giveRecord = new QGiveRecord("giveRecord");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.eod.eod.domain.user.model.QUser giver;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QItem item;

    public final com.eod.eod.domain.user.model.QUser receiver;

    public final BooleanPath status = createBoolean("status");

    public QGiveRecord(String variable) {
        this(GiveRecord.class, forVariable(variable), INITS);
    }

    public QGiveRecord(Path<? extends GiveRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGiveRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGiveRecord(PathMetadata metadata, PathInits inits) {
        this(GiveRecord.class, metadata, inits);
    }

    public QGiveRecord(Class<? extends GiveRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.giver = inits.isInitialized("giver") ? new com.eod.eod.domain.user.model.QUser(forProperty("giver")) : null;
        this.item = inits.isInitialized("item") ? new QItem(forProperty("item"), inits.get("item")) : null;
        this.receiver = inits.isInitialized("receiver") ? new com.eod.eod.domain.user.model.QUser(forProperty("receiver")) : null;
    }

}

