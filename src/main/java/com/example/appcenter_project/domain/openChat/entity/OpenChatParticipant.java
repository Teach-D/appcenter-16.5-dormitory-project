package com.example.appcenter_project.domain.openChat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "open_chat_participant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"}))
public class OpenChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private boolean notificationEnabled = true;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private Long lastReadMessageId;

    @Column(name = "is_host", nullable = false)
    private boolean isHost = false;

    public static OpenChatParticipant create(Long roomId, Long userId, LocalDateTime joinedAt) {
        OpenChatParticipant participant = new OpenChatParticipant();
        participant.roomId = roomId;
        participant.userId = userId;
        participant.joinedAt = joinedAt;
        participant.notificationEnabled = true;
        participant.isHost = false;
        return participant;
    }

    public static OpenChatParticipant create(Long roomId, Long userId, boolean isHost) {
        OpenChatParticipant participant = new OpenChatParticipant();
        participant.roomId = roomId;
        participant.userId = userId;
        participant.joinedAt = LocalDateTime.now();
        participant.notificationEnabled = true;
        participant.isHost = isHost;
        return participant;
    }

    public void grantHost() {
        this.isHost = true;
    }

    public void updateLastReadMessageId(Long messageId) {
        this.lastReadMessageId = messageId;
    }
}
