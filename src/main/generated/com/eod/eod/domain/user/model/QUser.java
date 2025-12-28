package com.eod.eod.domain.user.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -84340898L;

    public static final QUser user = new QUser("user");

    public final NumberPath<Integer> classNo = createNumber("classNo", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Integer> grade = createNumber("grade", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath introduce = createString("introduce");

    public final BooleanPath isGraduate = createBoolean("isGraduate");

    public final StringPath name = createString("name");

    public final StringPath oauthId = createString("oauthId");

    public final StringPath oauthProvider = createString("oauthProvider");

    public final EnumPath<User.Role> role = createEnum("role", User.Role.class);

    public final NumberPath<Integer> studentNo = createNumber("studentNo", Integer.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

