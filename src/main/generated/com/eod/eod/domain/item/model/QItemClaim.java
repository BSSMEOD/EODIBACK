package com.eod.eod.domain.item.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QItemClaim is a Querydsl query type for ItemClaim
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QItemClaim extends EntityPathBase<ItemClaim> {

    private static final long serialVersionUID = 8999038L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QItemClaim itemClaim = new QItemClaim("itemClaim");

    public final com.eod.eod.domain.user.model.QUser claimant;

    public final DateTimePath<java.time.LocalDateTime> claimedAt = createDateTime("claimedAt", java.time.LocalDateTime.class);

    public final StringPath claimReason = createString("claimReason");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QItem item;

    public final EnumPath<ItemClaim.ClaimStatus> status = createEnum("status", ItemClaim.ClaimStatus.class);

    public QItemClaim(String variable) {
        this(ItemClaim.class, forVariable(variable), INITS);
    }

    public QItemClaim(Path<? extends ItemClaim> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QItemClaim(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QItemClaim(PathMetadata metadata, PathInits inits) {
        this(ItemClaim.class, metadata, inits);
    }

    public QItemClaim(Class<? extends ItemClaim> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.claimant = inits.isInitialized("claimant") ? new com.eod.eod.domain.user.model.QUser(forProperty("claimant")) : null;
        this.item = inits.isInitialized("item") ? new QItem(forProperty("item"), inits.get("item")) : null;
    }

}

