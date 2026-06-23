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

    private LocalDateTime lastMessageAt;

    @Column(length = 500)
    private String lastMessage;

    @Column(nullable = false)
    private boolean isOfficial;

    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpenChatRoomType roomType;

    @Column(length = 50)
    private String password;

    @Column(nullable = false)
    private boolean isPublic = true;

    public boolean matchesPassword(String input) {
        return this.password == null || this.password.equals(input);
    }

    public static OpenChatRoom create(String name, String description, OpenChatRoomScope scope,
                                       int maxParticipants, Long createdBy,
                                       String creatorDormitory, boolean isOfficial) {
        OpenChatRoom room = new OpenChatRoom();
        room.name = name;
        room.description = description;
        room.scope = scope;
        room.maxParticipants = maxParticipants;
        room.creatorDormitory = creatorDormitory;
        room.isOfficial = isOfficial;
        room.createdBy = createdBy;
        room.roomType = OpenChatRoomType.OPEN;
        return room;
    }

    public static OpenChatRoom createOfficial(String name, String description, OpenChatRoomScope scope,
                                               int maxParticipants, Long createdBy,
                                               String creatorDormitory) {
        OpenChatRoom room = new OpenChatRoom();
        room.name = name;
        room.description = description;
        room.scope = scope != null ? scope : OpenChatRoomScope.ALL;
        room.maxParticipants = maxParticipants;
        room.creatorDormitory = creatorDormitory;
        room.isOfficial = true;
        room.createdBy = createdBy;
        room.roomType = OpenChatRoomType.OPEN;
        return room;
    }

    public static OpenChatRoom createDerived(String name, String description, int maxParticipants,
                                              Long createdBy, String password, boolean isPublic) {
        OpenChatRoom room = new OpenChatRoom();
        room.name = name;
        room.description = description;
        room.scope = OpenChatRoomScope.ALL;
        room.maxParticipants = maxParticipants;
        room.creatorDormitory = null;
        room.isOfficial = false;
        room.createdBy = createdBy;
        room.roomType = OpenChatRoomType.DERIVED;
        room.password = password;
        room.isPublic = isPublic;
        return room;
    }

    public void updateLastMessage(String content, LocalDateTime at) {
        this.lastMessage = content != null && content.length() > 500 ? content.substring(0, 500) : content;
        this.lastMessageAt = at;
    }
}
