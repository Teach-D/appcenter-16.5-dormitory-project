package com.example.appcenter_project.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 2093430862L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final EnumPath<com.example.appcenter_project.domain.user.enums.College> college = createEnum("college", com.example.appcenter_project.domain.user.enums.College.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final EnumPath<com.example.appcenter_project.domain.user.enums.DormType> dormType = createEnum("dormType", com.example.appcenter_project.domain.user.enums.DormType.class);

    public final ListPath<com.example.appcenter_project.domain.fcm.entity.FcmToken, com.example.appcenter_project.domain.fcm.entity.QFcmToken> fcmTokenList = this.<com.example.appcenter_project.domain.fcm.entity.FcmToken, com.example.appcenter_project.domain.fcm.entity.QFcmToken>createList("fcmTokenList", com.example.appcenter_project.domain.fcm.entity.FcmToken.class, com.example.appcenter_project.domain.fcm.entity.QFcmToken.class, PathInits.DIRECT2);

    public final ListPath<String, StringPath> groupOrderKeywords = this.<String, StringPath>createList("groupOrderKeywords", String.class, StringPath.class, PathInits.DIRECT2);

    public final ListPath<com.example.appcenter_project.domain.groupOrder.entity.GroupOrderLike, com.example.appcenter_project.domain.groupOrder.entity.QGroupOrderLike> groupOrderLikeList = this.<com.example.appcenter_project.domain.groupOrder.entity.GroupOrderLike, com.example.appcenter_project.domain.groupOrder.entity.QGroupOrderLike>createList("groupOrderLikeList", com.example.appcenter_project.domain.groupOrder.entity.GroupOrderLike.class, com.example.appcenter_project.domain.groupOrder.entity.QGroupOrderLike.class, PathInits.DIRECT2);

    public final ListPath<com.example.appcenter_project.domain.groupOrder.entity.GroupOrder, com.example.appcenter_project.domain.groupOrder.entity.QGroupOrder> groupOrderList = this.<com.example.appcenter_project.domain.groupOrder.entity.GroupOrder, com.example.appcenter_project.domain.groupOrder.entity.QGroupOrder>createList("groupOrderList", com.example.appcenter_project.domain.groupOrder.entity.GroupOrder.class, com.example.appcenter_project.domain.groupOrder.entity.QGroupOrder.class, PathInits.DIRECT2);

    public final ListPath<com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType, EnumPath<com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType>> groupOrderTypes = this.<com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType, EnumPath<com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType>>createList("groupOrderTypes", com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType.class, EnumPath.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.appcenter_project.common.image.entity.QImage image;

    public final BooleanPath isPrivacyAgreed = createBoolean("isPrivacyAgreed");

    public final BooleanPath isTermsAgreed = createBoolean("isTermsAgreed");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final NumberPath<Integer> penalty = createNumber("penalty", Integer.class);

    public final ListPath<Float, NumberPath<Float>> ratings = this.<Float, NumberPath<Float>>createList("ratings", Float.class, NumberPath.class, PathInits.DIRECT2);

    public final ListPath<com.example.appcenter_project.domain.user.enums.NotificationType, EnumPath<com.example.appcenter_project.domain.user.enums.NotificationType>> receiveNotificationTypes = this.<com.example.appcenter_project.domain.user.enums.NotificationType, EnumPath<com.example.appcenter_project.domain.user.enums.NotificationType>>createList("receiveNotificationTypes", com.example.appcenter_project.domain.user.enums.NotificationType.class, EnumPath.class, PathInits.DIRECT2);

    public final StringPath refreshToken = createString("refreshToken");

    public final EnumPath<com.example.appcenter_project.domain.user.enums.Role> role = createEnum("role", com.example.appcenter_project.domain.user.enums.Role.class);

    public final com.example.appcenter_project.domain.roommate.entity.QRoommateBoard roommateBoard;

    public final ListPath<com.example.appcenter_project.domain.roommate.entity.RoommateBoardLike, com.example.appcenter_project.domain.roommate.entity.QRoommateBoardLike> roommateBoardLikeList = this.<com.example.appcenter_project.domain.roommate.entity.RoommateBoardLike, com.example.appcenter_project.domain.roommate.entity.QRoommateBoardLike>createList("roommateBoardLikeList", com.example.appcenter_project.domain.roommate.entity.RoommateBoardLike.class, com.example.appcenter_project.domain.roommate.entity.QRoommateBoardLike.class, PathInits.DIRECT2);

    public final com.example.appcenter_project.domain.roommate.entity.QRoommateCheckList roommateCheckList;

    public final ListPath<String, StringPath> searchLogs = this.<String, StringPath>createList("searchLogs", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath studentNumber = createString("studentNumber");

    public final com.example.appcenter_project.common.image.entity.QImage timeTableImage;

    public final ListPath<com.example.appcenter_project.domain.tip.entity.TipLike, com.example.appcenter_project.domain.tip.entity.QTipLike> tipLikeList = this.<com.example.appcenter_project.domain.tip.entity.TipLike, com.example.appcenter_project.domain.tip.entity.QTipLike>createList("tipLikeList", com.example.appcenter_project.domain.tip.entity.TipLike.class, com.example.appcenter_project.domain.tip.entity.QTipLike.class, PathInits.DIRECT2);

    public final ListPath<com.example.appcenter_project.domain.tip.entity.Tip, com.example.appcenter_project.domain.tip.entity.QTip> tipList = this.<com.example.appcenter_project.domain.tip.entity.Tip, com.example.appcenter_project.domain.tip.entity.QTip>createList("tipList", com.example.appcenter_project.domain.tip.entity.Tip.class, com.example.appcenter_project.domain.tip.entity.QTip.class, PathInits.DIRECT2);

    public final ListPath<com.example.appcenter_project.domain.groupOrder.entity.UserGroupOrderChatRoom, com.example.appcenter_project.domain.groupOrder.entity.QUserGroupOrderChatRoom> userGroupOrderChatRoomList = this.<com.example.appcenter_project.domain.groupOrder.entity.UserGroupOrderChatRoom, com.example.appcenter_project.domain.groupOrder.entity.QUserGroupOrderChatRoom>createList("userGroupOrderChatRoomList", com.example.appcenter_project.domain.groupOrder.entity.UserGroupOrderChatRoom.class, com.example.appcenter_project.domain.groupOrder.entity.QUserGroupOrderChatRoom.class, PathInits.DIRECT2);

    public final ListPath<com.example.appcenter_project.domain.notification.entity.UserNotification, com.example.appcenter_project.domain.notification.entity.QUserNotification> userNotifications = this.<com.example.appcenter_project.domain.notification.entity.UserNotification, com.example.appcenter_project.domain.notification.entity.QUserNotification>createList("userNotifications", com.example.appcenter_project.domain.notification.entity.UserNotification.class, com.example.appcenter_project.domain.notification.entity.QUserNotification.class, PathInits.DIRECT2);

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.image = inits.isInitialized("image") ? new com.example.appcenter_project.common.image.entity.QImage(forProperty("image")) : null;
        this.roommateBoard = inits.isInitialized("roommateBoard") ? new com.example.appcenter_project.domain.roommate.entity.QRoommateBoard(forProperty("roommateBoard"), inits.get("roommateBoard")) : null;
        this.roommateCheckList = inits.isInitialized("roommateCheckList") ? new com.example.appcenter_project.domain.roommate.entity.QRoommateCheckList(forProperty("roommateCheckList"), inits.get("roommateCheckList")) : null;
        this.timeTableImage = inits.isInitialized("timeTableImage") ? new com.example.appcenter_project.common.image.entity.QImage(forProperty("timeTableImage")) : null;
    }

}

