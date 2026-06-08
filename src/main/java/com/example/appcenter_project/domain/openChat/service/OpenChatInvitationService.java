package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateDerivedRoomDto;
import com.example.appcenter_project.domain.openChat.dto.request.RequestSendInvitationDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseDerivedRoomCreatedDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseInvitationAcceptDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseInvitationCreatedDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatParticipantDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatParticipantListDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatInvitation;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatInvitationStatus;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomType;
import com.example.appcenter_project.domain.openChat.repository.OpenChatInvitationRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenChatInvitationService {

    private final OpenChatRoomRepository openChatRoomRepository;
    private final OpenChatParticipantRepository openChatParticipantRepository;
    private final OpenChatInvitationRepository openChatInvitationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResponseDerivedRoomCreatedDto createDerivedRoom(Long requesterId, RequestCreateDerivedRoomDto dto) {
        OpenChatRoom parentRoom = openChatRoomRepository.findById(dto.getParentRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        if (!openChatParticipantRepository.existsByRoomIdAndUserId(dto.getParentRoomId(), requesterId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        if (parentRoom.getRoomType() == OpenChatRoomType.DERIVED) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        OpenChatRoom derivedRoom = OpenChatRoom.createDerived(
                dto.getName(),
                dto.getDescription(),
                dto.getMaxParticipants(),
                requesterId,
                dto.getParentRoomId()
        );
        OpenChatRoom savedRoom = openChatRoomRepository.save(derivedRoom);

        OpenChatParticipant participant = OpenChatParticipant.create(savedRoom.getId(), requesterId, LocalDateTime.now());
        openChatParticipantRepository.save(participant);

        return ResponseDerivedRoomCreatedDto.of(savedRoom.getId());
    }

    @Transactional
    public ResponseInvitationCreatedDto sendInvitation(Long inviterUserId, Long roomId, RequestSendInvitationDto dto) {
        Long inviteeUserId = dto.getInviteeUserId();

        if (inviterUserId.equals(inviteeUserId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_INVITATION_SELF_INVITE);
        }

        userRepository.findById(inviteeUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        OpenChatRoom room = openChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        if (!openChatParticipantRepository.existsByRoomIdAndUserId(roomId, inviterUserId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        Long parentRoomId = room.getParentRoomId();

        if (parentRoomId == null) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
        }

        if (!openChatParticipantRepository.existsByRoomIdAndUserId(parentRoomId, inviteeUserId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_INVITATION_INVALID_TARGET);
        }

        if (openChatParticipantRepository.existsByRoomIdAndUserId(roomId, inviteeUserId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS);
        }

        if (openChatInvitationRepository.existsByRoomIdAndInviteeUserIdAndStatus(roomId, inviteeUserId, OpenChatInvitationStatus.PENDING)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_INVITATION_ALREADY_EXISTS);
        }

        OpenChatInvitation invitation = OpenChatInvitation.create(roomId, inviterUserId, inviteeUserId);
        OpenChatInvitation saved = openChatInvitationRepository.save(invitation);

        return ResponseInvitationCreatedDto.of(saved.getId());
    }

    @Transactional
    public ResponseInvitationAcceptDto acceptInvitation(Long requesterId, Long roomId, Long invitationId) {
        OpenChatInvitation invitation = openChatInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_INVITATION_NOT_FOUND));

        if (!invitation.getInviteeUserId().equals(requesterId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        if (!invitation.getRoomId().equals(roomId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        OpenChatRoom room = openChatRoomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        long currentCount = openChatParticipantRepository.countByRoomId(roomId);
        if (currentCount >= room.getMaxParticipants()) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FULL);
        }

        if (openChatParticipantRepository.existsByRoomIdAndUserId(roomId, requesterId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS);
        }

        invitation.accept();

        openChatParticipantRepository.save(OpenChatParticipant.create(roomId, requesterId, LocalDateTime.now()));

        return ResponseInvitationAcceptDto.of(room.getId(), room.getName(), (int) (currentCount + 1), room.getMaxParticipants());
    }

    @Transactional
    public void rejectInvitation(Long requesterId, Long roomId, Long invitationId) {
        OpenChatInvitation invitation = openChatInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_INVITATION_NOT_FOUND));

        if (!invitation.getInviteeUserId().equals(requesterId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        if (!invitation.getRoomId().equals(roomId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        invitation.reject();
    }

    @Transactional(readOnly = true)
    public ResponseOpenChatParticipantListDto getParticipants(Long requesterId, Long roomId) {
        OpenChatRoom room = openChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        if (!openChatParticipantRepository.existsByRoomIdAndUserId(roomId, requesterId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        List<OpenChatParticipant> participants = openChatParticipantRepository.findByRoomId(roomId);

        List<Long> userIds = participants.stream().map(OpenChatParticipant::getUserId).toList();
        List<User> users = userRepository.findAllById(userIds);
        Map<Long, String> nicknameMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        Long hostUserId = room.getHostUserId();
        List<ResponseOpenChatParticipantDto> dtos = participants.stream()
                .sorted((a, b) -> a.getJoinedAt().compareTo(b.getJoinedAt()))
                .map(p -> ResponseOpenChatParticipantDto.of(
                        p,
                        nicknameMap.getOrDefault(p.getUserId(), ""),
                        p.getUserId().equals(hostUserId)
                ))
                .toList();

        return ResponseOpenChatParticipantListDto.of(roomId, dtos);
    }
}
