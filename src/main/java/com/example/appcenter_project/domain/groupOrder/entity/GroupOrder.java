package com.example.appcenter_project.domain.groupOrder.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.groupOrder.dto.request.RequestGroupOrderDto;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class GroupOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private GroupOrderType groupOrderType;

    private Integer price;

    private String link;

    private String openChatLink;

    private LocalDateTime deadline;

    private int groupOrderLike = 0;

    private int groupOrderViewCount = 0;

    @Lob
    private String description;

    private boolean recruitmentComplete = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_order_chat_room_id")
    private GroupOrderChatRoom groupOrderChatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "groupOrder", cascade = CascadeType.REMOVE)
    private List<GroupOrderLike> groupOrderLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "groupOrder", cascade = CascadeType.REMOVE)
    private List<GroupOrderComment> groupOrderCommentList = new ArrayList<>();

    @Builder
    public GroupOrder(String title, GroupOrderType groupOrderType, Integer price, String link, String openChatLink, int currentPeople, int maxPeople, LocalDateTime deadline, int groupOrderLike, String description, User user, GroupOrderChatRoom groupOrderChatRoom, int groupOrderViewCount) {
        this.title = title;
        this.groupOrderType = groupOrderType;
        this.price = price;
        this.link = link;
        this.openChatLink = openChatLink;
        this.deadline = deadline;
        this.groupOrderLike = groupOrderLike;
        this.groupOrderViewCount = groupOrderViewCount;
        this.description = description;
        this.user = user;
        this.groupOrderChatRoom = groupOrderChatRoom;
    }

    public void update(RequestGroupOrderDto requestGroupOrderDto) {
        this.title = requestGroupOrderDto.getTitle();
        this.groupOrderType = requestGroupOrderDto.getGroupOrderType();
        this.price = requestGroupOrderDto.getPrice();
        this.link = requestGroupOrderDto.getLink();
        this.openChatLink = requestGroupOrderDto.getOpenChatLink();
        this.deadline = requestGroupOrderDto.getDeadline();
        this.description = requestGroupOrderDto.getDescription();
    }

    public Integer plusLike() {
        this.groupOrderLike += 1;
        return this.groupOrderLike;}

    public void updateGroupOrderChatRoom(GroupOrderChatRoom groupOrderChatRoom) {
        this.groupOrderChatRoom = groupOrderChatRoom;
    }

    public Integer minusLike() {
        return this.groupOrderLike -= 1;
    }

    public void plusViewCount() {
        this.groupOrderViewCount += 1;
    }

    public void updateRecruitmentComplete(boolean b) {
        this.recruitmentComplete = b;
    }
}
