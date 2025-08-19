package com.example.appcenter_project.entity.groupOrder;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class UserGroupOrderChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "group_order_chat_room_id")
    private GroupOrderChatRoom groupOrderChatRoom;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 30)
    private String chatRoomTitle;

    @Column(nullable = false)
    private int unreadCount = 0;

    // 채팅방의 가장 최근 채팅
    @Column(length = 100)
    private String recentChatContent;

    @Builder
    public UserGroupOrderChatRoom(GroupOrderChatRoom groupOrderChatRoom, User user) {
        this.groupOrderChatRoom = groupOrderChatRoom;
        this.chatRoomTitle = groupOrderChatRoom.getTitle();
        this.user = user;
    }

    public void update(String title, String content, int unreadCount) {
        this.chatRoomTitle = title;
        this.recentChatContent = content;
        this.unreadCount = unreadCount;
    }
}
