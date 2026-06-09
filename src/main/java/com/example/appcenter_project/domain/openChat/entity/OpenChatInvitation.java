package com.example.appcenter_project.domain.openChat.entity;

import com.example.appcenter_project.domain.openChat.enums.OpenChatInvitationStatus;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "open_chat_invitation")
public class OpenChatInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long inviterUserId;

    @Column(nullable = false)
    private Long inviteeUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpenChatInvitationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static OpenChatInvitation create(Long roomId, Long inviterUserId, Long inviteeUserId) {
        OpenChatInvitation invitation = new OpenChatInvitation();
        invitation.roomId = roomId;
        invitation.inviterUserId = inviterUserId;
        invitation.inviteeUserId = inviteeUserId;
        invitation.status = OpenChatInvitationStatus.PENDING;
        invitation.createdAt = LocalDateTime.now();
        return invitation;
    }

    public void accept() {
        if (this.status != OpenChatInvitationStatus.PENDING) {
            throw new CustomException(ErrorCode.OPEN_CHAT_INVITATION_ALREADY_PROCESSED);
        }
        this.status = OpenChatInvitationStatus.ACCEPTED;
    }

    public void reject() {
        if (this.status != OpenChatInvitationStatus.PENDING) {
            throw new CustomException(ErrorCode.OPEN_CHAT_INVITATION_ALREADY_PROCESSED);
        }
        this.status = OpenChatInvitationStatus.REJECTED;
    }
}
