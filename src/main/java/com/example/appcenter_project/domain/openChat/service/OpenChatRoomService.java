package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseLeaveOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatParticipantDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatParticipantListDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatRoomDetailDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomTab;
import com.example.appcenter_project.domain.openChat.repository.OpenChatMessageRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OpenChatRoomService {

    private final OpenChatRoomRepository openChatRoomRepository;
    private final OpenChatParticipantRepository openChatParticipantRepository;
    private final OpenChatMessageRepository openChatMessageRepository;
    private final UserRepository userRepository;
    private final OpenChatMessageService openChatMessageService;

    @Autowired
    public OpenChatRoomService(
            OpenChatRoomRepository openChatRoomRepository,
            OpenChatParticipantRepository openChatParticipantRepository,
            OpenChatMessageRepository openChatMessageRepository,
            UserRepository userRepository,
            @Lazy OpenChatMessageService openChatMessageService) {
        this.openChatRoomRepository = openChatRoomRepository;
        this.openChatParticipantRepository = openChatParticipantRepository;
        this.openChatMessageRepository = openChatMessageRepository;
        this.userRepository = userRepository;
        this.openChatMessageService = openChatMessageService;
    }

    @Transactional
    public Long createRoom(RequestCreateOpenChatRoomDto request, Long userId) {
        String creatorDormitory = null;
        if (request.getScope() == OpenChatRoomScope.DORMITORY) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            creatorDormitory = user.getDormType() != null ? user.getDormType().name() : null;
        }

        OpenChatRoom room = OpenChatRoom.create(
                request.getName(),
                request.getDescription(),
                request.getScope(),
                request.getMaxParticipants(),
                userId,
                creatorDormitory,
                false
        );
        OpenChatRoom savedRoom = openChatRoomRepository.save(room);

        OpenChatParticipant participant = OpenChatParticipant.create(savedRoom.getId(), userId, true);
        openChatParticipantRepository.save(participant);

        return savedRoom.getId();
    }

    @Transactional(readOnly = true)
    public Page<ResponseOpenChatRoomDto> getRooms(Long userId, OpenChatRoomTab tab, Pageable pageable) {
        if (tab == OpenChatRoomTab.MY) {
            List<OpenChatRoom> rooms = openChatRoomRepository.findMyRooms(userId);
            return toPageDtoWithUnread(rooms, userId, pageable);
        } else if (tab == OpenChatRoomTab.ALL) {
            List<OpenChatRoom> rooms = openChatRoomRepository.findAllPublicRooms();
            return toPageDto(rooms, userId, pageable);
        }
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    @Transactional(readOnly = true)
    public Page<ResponseOpenChatRoomDto> getRoomsForDormitory(Long userId, String dormType, Pageable pageable) {
        if ("NONE".equals(dormType)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<OpenChatRoom> rooms = openChatRoomRepository.findByDormitory(dormType);
        return toPageDto(rooms, userId, pageable);
    }

    @Transactional
    public ResponseOpenChatRoomDetailDto joinRoom(Long userId, Long roomId) {
        OpenChatRoom room = openChatRoomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        if (openChatParticipantRepository.existsByRoomIdAndUserId(roomId, userId)) {
            return toDetailDto(room, roomId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (room.getScope() == OpenChatRoomScope.DORMITORY) {
            String userDorm = user.getDormType() != null ? user.getDormType().name() : null;
            String roomDorm = room.getCreatorDormitory();
            if (roomDorm == null || !roomDorm.equals(userDorm)) {
                throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
            }
        }

        long currentCount = openChatParticipantRepository.countByRoomId(roomId);
        if (currentCount >= room.getMaxParticipants()) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FULL);
        }

        openChatParticipantRepository.save(OpenChatParticipant.create(roomId, userId, LocalDateTime.now()));
        openChatMessageService.sendSystemMessage(roomId, user.getName() + "님이 입장했습니다.");

        return toDetailDto(room, roomId);
    }

    @Transactional
    public ResponseLeaveOpenChatRoomDto leaveRoom(Long roomId, Long userId, Long newHostUserId) {
        List<OpenChatParticipant> lockedParticipants =
                openChatParticipantRepository.findAllByRoomIdWithLock(roomId);

        OpenChatParticipant self = lockedParticipants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND));

        if (newHostUserId != null) {
            if (!self.isHost()) {
                throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
            }

            if (newHostUserId.equals(userId)) {
                throw new CustomException(ErrorCode.OPEN_CHAT_ALREADY_HOST);
            }

            OpenChatParticipant newHostParticipant = lockedParticipants.stream()
                    .filter(p -> p.getUserId().equals(newHostUserId))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND));

            newHostParticipant.grantHost();
            openChatParticipantRepository.delete(self);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            openChatMessageService.sendSystemMessage(roomId, user.getName() + "님이 퇴장했습니다.");

            return ResponseLeaveOpenChatRoomDto.builder().roomDeleted(false).build();
        }

        if (self.isHost()) {
            long hostCount = lockedParticipants.stream().filter(OpenChatParticipant::isHost).count();
            if (hostCount == 1) {
                throw new CustomException(ErrorCode.OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE);
            }
        }

        openChatParticipantRepository.delete(self);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        openChatMessageService.sendSystemMessage(roomId, user.getName() + "님이 퇴장했습니다.");

        return ResponseLeaveOpenChatRoomDto.builder().roomDeleted(false).build();
    }

    @Transactional
    public void updateNotification(Long userId, Long roomId, boolean enabled) {
        OpenChatParticipant participant = openChatParticipantRepository
                .findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND));
        participant.updateNotificationEnabled(enabled);
    }

    @Transactional
    public void kickParticipant(Long actorId, Long roomId, Long targetUserId) {
        openChatRoomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        if (actorId.equals(targetUserId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_KICK_FORBIDDEN);
        }

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        boolean isAdmin = actor.getRole() == Role.ROLE_ADMIN;

        if (!isAdmin) {
            boolean actorIsHost = openChatParticipantRepository
                    .existsByRoomIdAndUserIdAndIsHost(roomId, actorId, true);
            if (!actorIsHost) {
                throw new CustomException(ErrorCode.OPEN_CHAT_KICK_FORBIDDEN);
            }

            OpenChatParticipant targetParticipant = openChatParticipantRepository
                    .findByRoomIdAndUserId(roomId, targetUserId)
                    .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND));

            if (targetParticipant.isHost()) {
                throw new CustomException(ErrorCode.OPEN_CHAT_KICK_FORBIDDEN);
            }
        }

        OpenChatParticipant targetParticipant = openChatParticipantRepository
                .findByRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        openChatParticipantRepository.delete(targetParticipant);
        openChatMessageService.sendSystemMessage(roomId, targetUser.getName() + "님이 강제퇴장되었습니다.");
    }

    @Transactional
    public void deleteRoom(Long roomId, Long userId) {
        OpenChatRoom room = openChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        boolean isHost = openChatParticipantRepository.existsByRoomIdAndUserIdAndIsHost(roomId, userId, true);

        if (!isHost) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        if (room.isOfficial()) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        List<OpenChatParticipant> participants = openChatParticipantRepository.findAllByRoomId(roomId);
        openChatParticipantRepository.deleteAll(participants);
        openChatRoomRepository.delete(room);
    }

    @Transactional
    public void grantHost(Long roomId, Long requesterId, Long targetUserId) {
        openChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        boolean requesterIsHost = openChatParticipantRepository.existsByRoomIdAndUserIdAndIsHost(roomId, requesterId, true);
        if (!requesterIsHost) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        OpenChatParticipant target = openChatParticipantRepository
                .findByRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND));

        if (target.isHost()) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ALREADY_HOST);
        }

        target.grantHost();
    }

    @Transactional(readOnly = true)
    public ResponseOpenChatParticipantListDto getParticipants(Long roomId, Long requesterId) {
        openChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        if (!openChatParticipantRepository.existsByRoomIdAndUserId(roomId, requesterId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        }

        List<OpenChatParticipant> participants = openChatParticipantRepository.findAllByRoomId(roomId);

        List<Long> userIds = participants.stream().map(OpenChatParticipant::getUserId).toList();
        List<User> users = userRepository.findAllById(userIds);
        Map<Long, String> nicknameMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        List<ResponseOpenChatParticipantDto> dtos = participants.stream()
                .sorted((a, b) -> a.getJoinedAt().compareTo(b.getJoinedAt()))
                .map(p -> ResponseOpenChatParticipantDto.of(
                        p,
                        nicknameMap.getOrDefault(p.getUserId(), ""),
                        p.isHost()))
                .toList();

        int hostCount = (int) participants.stream().filter(OpenChatParticipant::isHost).count();
        return ResponseOpenChatParticipantListDto.of(roomId, dtos, hostCount);
    }

    private Page<ResponseOpenChatRoomDto> toPageDto(List<OpenChatRoom> rooms, Long userId, Pageable pageable) {
        if (rooms.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<Long> roomIds = rooms.stream().map(OpenChatRoom::getId).toList();
        Map<Long, Long> countMap = openChatParticipantRepository.countByRoomIds(roomIds);
        Set<Long> joinedRoomIds = openChatParticipantRepository.findJoinedRoomIds(userId, roomIds);
        List<ResponseOpenChatRoomDto> dtos = rooms.stream()
                .map(room -> ResponseOpenChatRoomDto.from(
                        room,
                        countMap.getOrDefault(room.getId(), 0L).intValue(),
                        joinedRoomIds.contains(room.getId())))
                .toList();
        return new PageImpl<>(dtos, pageable, dtos.size());
    }

    private Page<ResponseOpenChatRoomDto> toPageDtoWithUnread(List<OpenChatRoom> rooms, Long userId, Pageable pageable) {
        if (rooms.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<Long> roomIds = rooms.stream().map(OpenChatRoom::getId).toList();
        Map<Long, Long> countMap = openChatParticipantRepository.countByRoomIds(roomIds);
        Set<Long> joinedRoomIds = openChatParticipantRepository.findJoinedRoomIds(userId, roomIds);
        Map<Long, Long> lastReadMap = openChatParticipantRepository.findLastReadMessageIdsByUserId(userId, roomIds);
        List<ResponseOpenChatRoomDto> dtos = rooms.stream()
                .map(room -> {
                    Long lastReadMessageId = lastReadMap.get(room.getId());
                    int unreadCount = (int) openChatMessageRepository.countByRoomIdAndIdGreaterThan(room.getId(), lastReadMessageId);
                    return ResponseOpenChatRoomDto.from(
                            room,
                            countMap.getOrDefault(room.getId(), 0L).intValue(),
                            joinedRoomIds.contains(room.getId()),
                            unreadCount);
                })
                .toList();
        return new PageImpl<>(dtos, pageable, dtos.size());
    }

    private ResponseOpenChatRoomDetailDto toDetailDto(OpenChatRoom room, Long roomId) {
        long count = openChatParticipantRepository.countByRoomId(roomId);
        return ResponseOpenChatRoomDetailDto.builder()
                .roomId(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .scope(room.getScope())
                .currentParticipants((int) count)
                .maxParticipants(room.getMaxParticipants())
                .isOfficial(room.isOfficial())
                .createdAt(room.getCreatedDate())
                .build();
    }
}
