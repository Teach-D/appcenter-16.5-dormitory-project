package com.example.appcenter_project.domain.user.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.common.image.entity.Image;
import com.example.appcenter_project.domain.fcm.entity.FcmToken;
import com.example.appcenter_project.domain.user.dto.request.RequestUserDto;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.entity.UserGroupOrderChatRoom;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderLike;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoardLike;
import com.example.appcenter_project.domain.tip.entity.TipLike;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.tip.entity.Tip;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.user.enums.College;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

import static com.example.appcenter_project.domain.user.enums.NotificationType.*;

@Entity
@NoArgsConstructor
@Getter
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentNumber;

    @Column(unique = true)
    private String name;

    private String password;

    private String refreshToken;

    @Enumerated(EnumType.STRING)
    private DormType dormType;

    @Enumerated(EnumType.STRING)
    private College college;

    private Integer penalty;

    // 이용약관 동의 여부, 초기값 false
    private boolean isTermsAgreed = false;

    // 개인정보처리방침 동의 여부, 초기값 false
    private boolean isPrivacyAgreed = false;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ElementCollection
    @CollectionTable(name = "user_search_logs", joinColumns =
    @JoinColumn(name = "user_id")
    )
    @Column(name = "log")
    private List<String> searchLogs = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_ratings", joinColumns =
    @JoinColumn(name = "user_id")
    )
    @Column(name = "rating")
    private List<Float> ratings = new ArrayList<>();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "group_order_type", joinColumns =
    @JoinColumn(name = "user_id")
    )
    @Column(name = "group_order_type")
    private List<GroupOrderType> groupOrderTypes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "group_order_keyword", joinColumns =
    @JoinColumn(name = "user_id")
    )
    @Column(name = "group_order_keyword")
    private List<String> groupOrderKeywords = new ArrayList<>();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_receive_notification_type", joinColumns =
    @JoinColumn(name = "user_id")
    )
    @Column(name = "notification_type")
    // 모든 유저가 초기에는 모든 알림을 받도록
    private List<NotificationType> receiveNotificationTypes = new ArrayList<>(List.of(ROOMMATE, GROUP_ORDER, DORMITORY, UNI_DORM, SUPPORTERS));

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_table_image_id")
    private Image timeTableImage;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Tip> tipList = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<GroupOrderLike> groupOrderLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TipLike> tipLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<GroupOrder> groupOrderList = new ArrayList<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserGroupOrderChatRoom> userGroupOrderChatRoomList = new ArrayList<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RoommateCheckList roommateCheckList;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RoommateBoard roommateBoard;

    @OneToMany(mappedBy = "user")
    private List<RoommateBoardLike> roommateBoardLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade =  CascadeType.REMOVE)
    private List<UserNotification> userNotifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade =  CascadeType.REMOVE)
    private List<FcmToken>  fcmTokenList = new ArrayList<>();

    @Builder
    public User(String studentNumber, String name, String password, DormType dormType, Integer penalty, Role role, Image image) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.password = password;
        this.dormType = dormType;
        this.penalty = penalty;
        this.role = role;
        this.image = image;
    }

    public void updateTermsAgreed(boolean isTermsAgreed) {
        this.isTermsAgreed = isTermsAgreed;
    }

    public void updatePrivacyAgreed(boolean isPrivacyAgreed) {
        this.isPrivacyAgreed = isPrivacyAgreed;
    }

    public void addReceiveNotificationType(NotificationType notificationType) {
        this.receiveNotificationTypes.add(notificationType);
    }

    public void deleteReceiveNotificationType(NotificationType notificationType) {
        this.receiveNotificationTypes.remove(notificationType);
    }

    public void update(RequestUserDto requestUserDto) {
        this.name = requestUserDto.getName();
        this.dormType = DormType.from(requestUserDto.getDormType());
        this.college = College.from(requestUserDto.getCollege());
        this.penalty = requestUserDto.getPenalty();

        if (DormType.from(requestUserDto.getDormType()) == DormType.DORM_1 || DormType.from(requestUserDto.getDormType()) == DormType.DORM_2
        || DormType.from(requestUserDto.getDormType()) == DormType.DORM_3) {
            receiveNotificationTypes.add(DORMITORY);
        }

    }

    public void addFcmToken(FcmToken fcmToken) {
        this.fcmTokenList.add(fcmToken);
    }

    public void addRoommateBoardLike(RoommateBoardLike roommateBoardLike) {
        this.roommateBoardLikeList.add(roommateBoardLike);
    }

    public void removeRoommateBoardLike(RoommateBoardLike roommateBoardLike) {
        this.roommateBoardLikeList.remove(roommateBoardLike);
    }

    public void addNotification(UserNotification userNotification) {
        this.userNotifications.add(userNotification);
    }

    public void updateImage(Image image) {
        this.image =image;
    }

    public void updateTimeTableImage(Image timeTableImage) {
        this.timeTableImage = timeTableImage;
    }

    public void removeTimeTableImage() {
        this.timeTableImage = null;
    }

    public void addTip(Tip tip) {
        this.tipList.add(tip);
    }

    public void removeTip(Tip tip) {
        this.tipList.remove(tip);
    }

    public void addGroupOrderLike(GroupOrderLike groupOrderLike) {
        this.groupOrderLikeList.add(groupOrderLike);
    }

    public void addLike(TipLike tipLike) {
        this.tipLikeList.add(tipLike);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void removeLike(TipLike tipLike) {
        this.tipLikeList.remove(tipLike);
    }

    public void removeGroupOrderLike(GroupOrderLike groupOrderLike) {
        this.groupOrderLikeList.remove(groupOrderLike);
    }

    public void addSearchLog(String searchLog) {
        searchLogs.remove(searchLog); // 중복 제거
        if (searchLogs.size() >= 3) {
            searchLogs.remove(0); // 맨 앞(가장 오래된 것) 제거
        }
        searchLogs.add(searchLog); // 최신 검색어 추가
    }

    public void addRating(Float rating) {
        ratings.add(rating);
    }

    public Float getAverageRating() {
        Float sum = 0.0f;
        for (Float rating : ratings) {
            sum += rating;
        }
        Float average = sum / ratings.size();
        return Math.round(average * 10.0f) / 10.0f;
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}
