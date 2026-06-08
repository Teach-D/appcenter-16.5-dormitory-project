package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.OpenChatInvitation;
import com.example.appcenter_project.domain.openChat.enums.OpenChatInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OpenChatInvitationRepository extends JpaRepository<OpenChatInvitation, Long> {

    Optional<OpenChatInvitation> findByIdAndInviteeUserId(Long id, Long inviteeUserId);

    boolean existsByRoomIdAndInviteeUserIdAndStatus(Long roomId, Long inviteeUserId, OpenChatInvitationStatus status);

    List<OpenChatInvitation> findByRoomId(Long roomId);
}
