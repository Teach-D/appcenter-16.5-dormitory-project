package com.example.appcenter_project.entity.user;

import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.like.TipLike;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
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

    private String studentNumber;

    private String name;
//    private String password;

    private DormType dormType;

    private College college;

    private int penalty;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToMany(mappedBy = "user")
    private List<GroupOrderLike> groupOrderLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<TipLike> tipLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<GroupOrder> groupOrderList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserGroupOrderChatRoom> userGroupOrderChatRoomList = new ArrayList<>();

    @OneToOne(mappedBy = "member")
    private RoommateBoard roommateBoard;


    @Builder
    public User(String studentNumber, String name, String password, DormType dormType, int penalty, Role role, Image image) {
        this.name = name;
        this.studentNumber = studentNumber;
//        this.password = password;
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

    public void addLike(GroupOrderLike groupOrderLike) {
        this.groupOrderLikeList.add(groupOrderLike);
    }

    public void addLike(TipLike tipLike) {
        this.tipLikeList.add(tipLike);
    }
}
