package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateDormOfficialRoomDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OpenChatDormOfficialRoomService {

    private final OpenChatRoomRepository openChatRoomRepository;
    private final OpenChatParticipantRepository openChatParticipantRepository;
    private final UserRepository userRepository;

    public Long createDormOfficialRoom(Long adminId, RequestCreateDormOfficialRoomDto request) {
        DormType dormType = request.getDormType();
        if (dormType == null || dormType == DormType.NONE) {
            throw new CustomException(ErrorCode.INVALID_DORM_TYPE);
        }
        openChatRoomRepository.findByTargetDorm(dormType).ifPresent(r -> {
            throw new CustomException(ErrorCode.OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS);
        });

        OpenChatRoom room = OpenChatRoom.createDormOfficial(request.getName(), request.getDescription(), adminId, dormType);
        OpenChatRoom savedRoom = openChatRoomRepository.save(room);

        List<User> users = userRepository.findAllByDormType(dormType);
        List<OpenChatParticipant> participants = users.stream()
                .map(u -> OpenChatParticipant.create(savedRoom.getId(), u.getId(), LocalDateTime.now()))
                .toList();
        openChatParticipantRepository.saveAll(participants);

        return savedRoom.getId();
    }

    public void reassignDormRoom(Long userId, DormType oldDorm, DormType newDorm) {
        if (oldDorm != null && oldDorm != DormType.NONE) {
            openChatRoomRepository.findByTargetDorm(oldDorm).ifPresent(room ->
                    openChatParticipantRepository.findByRoomIdAndUserId(room.getId(), userId)
                            .ifPresent(openChatParticipantRepository::delete)
            );
        }
        if (newDorm != null && newDorm != DormType.NONE) {
            openChatRoomRepository.findByTargetDorm(newDorm).ifPresent(room -> {
                if (!openChatParticipantRepository.existsByRoomIdAndUserId(room.getId(), userId)) {
                    openChatParticipantRepository.save(
                            OpenChatParticipant.create(room.getId(), userId, LocalDateTime.now())
                    );
                }
            });
        }
    }
}
