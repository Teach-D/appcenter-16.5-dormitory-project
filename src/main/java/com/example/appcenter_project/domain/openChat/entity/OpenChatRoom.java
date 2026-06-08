package com.example.appcenter_project.domain.openChat.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "open_chat_room")
public class OpenChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 100)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpenChatRoomScope scope;

    @Column(nullable = false)
    private int maxParticipants;

    private String creatorDormitory;

    private Long hostUserId;

    private LocalDateTime lastMessageAt;

    @Column(length = 500)
    private String lastMessage;

    @Column(nullable = false)
    private boolean isOfficial;

    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpenChatRoomType roomType;

    private Long parentRoomId;

    public static OpenChatRoom create(String name, String description, OpenChatRoomScope scope,
                                       int maxParticipants, Long hostUserId,
                                       String creatorDormitory, boolean isOfficial) {
        OpenChatRoom room = new OpenChatRoom();
        room.name = name;
        room.description = description;
        room.scope = scope;
        room.maxParticipants = maxParticipants;
        room.hostUserId = hostUserId;
        room.creatorDormitory = creatorDormitory;
        room.isOfficial = isOfficial;
        room.createdBy = hostUserId;
        room.roomType = OpenChatRoomType.OPEN;
        return room;
    }

    public static OpenChatRoom createDerived(String name, String description, int maxParticipants,
                                              Long hostUserId, Long parentRoomId) {
        OpenChatRoom room = new OpenChatRoom();
        room.name = name;
        room.description = description;
        room.scope = OpenChatRoomScope.ALL;
        room.maxParticipants = maxParticipants;
        room.hostUserId = hostUserId;
        room.creatorDormitory = null;
        room.isOfficial = false;
        room.createdBy = hostUserId;
        room.roomType = OpenChatRoomType.DERIVED;
        room.parentRoomId = parentRoomId;
        return room;
    }

    public void updateHost(Long userId) {
        this.hostUserId = userId;
    }

    public void updateLastMessage(String content, LocalDateTime at) {
        this.lastMessage = content != null && content.length() > 500 ? content.substring(0, 500) : content;
        this.lastMessageAt = at;
    }
}
