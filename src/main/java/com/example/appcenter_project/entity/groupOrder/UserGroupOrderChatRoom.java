package com.example.appcenter_project.entity.groupOrder;

import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class UserGroupOrderChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_order_chat_room_id")
    private GroupOrderChatRoom groupOrderChatRoom;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public UserGroupOrderChatRoom(GroupOrderChatRoom groupOrderChatRoom, User user) {
        this.groupOrderChatRoom = groupOrderChatRoom;
        this.user = user;
    }
}
