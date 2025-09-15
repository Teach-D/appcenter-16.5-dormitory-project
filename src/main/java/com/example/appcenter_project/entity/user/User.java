package com.example.appcenter_project.entity.user;

import com.example.appcenter_project.converter.StringListConverter;
import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.like.RoommateBoardLike;
import com.example.appcenter_project.entity.like.TipLike;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.entity.roommate.MyRoommate;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.enums.user.College;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.enums.user.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import java.util.*;

@Entity
@NoArgsConstructor
@Getter
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String studentNumber;

    @Column(length = 10, unique = true)
    private String name;

    private String password;

    private String refreshToken;

    @Enumerated(EnumType.STRING)
    private DormType dormType;

    @Enumerated(EnumType.STRING)
    private College college;

    private Integer penalty;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @ManyToOne(fetch = FetchType.LAZY)
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

    public void addRoommateBoardLike(RoommateBoardLike roommateBoardLike) {
        this.roommateBoardLikeList.add(roommateBoardLike);
    }

    public void removeRoommateBoardLike(RoommateBoardLike roommateBoardLike) {
        this.roommateBoardLikeList.remove(roommateBoardLike);
    }


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

    public void update(RequestUserDto requestUserDto) {
        this.name = requestUserDto.getName();
        this.dormType = DormType.from(requestUserDto.getDormType());
        this.college = College.from(requestUserDto.getCollege());
        this.penalty = requestUserDto.getPenalty();
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
}
