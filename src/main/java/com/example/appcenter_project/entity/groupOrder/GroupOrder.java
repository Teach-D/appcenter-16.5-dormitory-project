package com.example.appcenter_project.entity.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.Order;

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

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupOrderType groupOrderType;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String openChatLink;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false)
    private int groupOrderLike = 0;

    @Column(nullable = false)
    private int groupOrderViewCount = 0;

    @Lob
    private String description;

    @Column(nullable = false)
    private boolean recruitmentComplete = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_order_chat_room_id")
    private GroupOrderChatRoom groupOrderChatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(orphanRemoval = true)
    private List<Image> imageList = new ArrayList<>();

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
