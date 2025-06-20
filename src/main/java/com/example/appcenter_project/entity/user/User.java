package com.example.appcenter_project.entity.user;

import com.example.appcenter_project.converter.StringListConverter;
import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.like.TipLike;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.enums.user.College;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.enums.user.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String studentNumber;

    @Column(length = 10)
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

    @Convert(converter = StringListConverter.class)
    private List<String> searchLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToMany(mappedBy = "user")
    private List<Tip> tipList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<GroupOrderLike> groupOrderLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<TipLike> tipLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<GroupOrder> groupOrderList = new ArrayList<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<UserGroupOrderChatRoom> userGroupOrderChatRoomList = new ArrayList<>();

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
        this.dormType = DormType.valueOf(requestUserDto.getDormType());
        this.college = College.valueOf(requestUserDto.getCollege());
        this.penalty = requestUserDto.getPenalty();
    }

    public void updateImage(Image image) {
        this.image =image;
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

    public void addSearchKeyword(String keyword) {
        if (searchLog == null) {
            searchLog = new ArrayList<>();
        }

        // 기존에 있으면 삭제
        searchLog.remove(keyword);
        searchLog.add(keyword);

        // 5개 초과 시, 가장 오래된 항목 제거
        if (searchLog.size() > 5) {
            searchLog.remove(0); // 맨 앞 요소 제거
        }
    }

}
