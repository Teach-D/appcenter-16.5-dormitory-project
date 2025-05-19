package com.example.appcenter_project.entity.groupOrder;

import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class GroupOrderChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "group_order_chat_room_id")
    private GroupOrderChatRoom groupOrderChatRoom;

    @Column(nullable = false, length = 100)
    private String content;

    private List<Long> unreadUser = new ArrayList<>();

    @Builder
    public GroupOrderChat(User user, GroupOrderChatRoom groupOrderChatRoom, String content, List<Long> unreadUser) {
        this.user = user;
        this.groupOrderChatRoom = groupOrderChatRoom;
        this.content = content;
        this.unreadUser = unreadUser;
    }
}
