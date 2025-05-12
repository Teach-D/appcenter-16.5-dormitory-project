package com.example.appcenter_project.entity.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.entity.Image;
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
public class GroupOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private GroupOrderType groupOrderType;

    private int price;

    private String link;

    private int currentPeople;

    private int maxPeople;

    private LocalDateTime deadline;

    private int groupOrderLike;

    private String description;

    @OneToOne
    @JoinColumn(name = "group_order_chat_room_id")
    private GroupOrderChatRoom groupOrderChatRoom;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany
    private List<Image> imageList = new ArrayList<>();

    @Builder
    public GroupOrder(String title, String groupOrderType, int price, String link, int currentPeople, int maxPeople, LocalDateTime deadline, int groupOrderLike, String description, User user, GroupOrderChatRoom groupOrderChatRoom) {
        this.title = title;
        this.groupOrderType = GroupOrderType.valueOf(groupOrderType);
        this.price = price;
        this.link = link;
        this.currentPeople = currentPeople;
        this.maxPeople = maxPeople;
        this.deadline = deadline;
        this.groupOrderLike = groupOrderLike;
        this.description = description;
        this.user = user;
        this.groupOrderChatRoom = groupOrderChatRoom;
    }

    public void update(RequestGroupOrderDto requestGroupOrderDto) {
        this.title = requestGroupOrderDto.getTitle();
        this.groupOrderType = GroupOrderType.valueOf(requestGroupOrderDto.getGroupOrderType());
        this.price = requestGroupOrderDto.getPrice();
        this.link = requestGroupOrderDto.getLink();
        this.maxPeople = requestGroupOrderDto.getMaxPeople();
        this.deadline = requestGroupOrderDto.getDeadline();
        this.description = requestGroupOrderDto.getDescription();
    }

    public Integer plusLike() {
        this.groupOrderLike += 1;
        return this.groupOrderType.ordinal();
    }

    public void updateGroupOrderChatRoom(GroupOrderChatRoom groupOrderChatRoom) {
        this.groupOrderChatRoom = groupOrderChatRoom;
    }

    public void plusCurrentPeople() {
        this.currentPeople += 1;
    }
}
