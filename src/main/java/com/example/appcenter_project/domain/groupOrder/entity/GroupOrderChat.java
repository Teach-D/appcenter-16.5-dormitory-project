package com.example.appcenter_project.domain.groupOrder.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class GroupOrderChat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch =  FetchType.LAZY)
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
