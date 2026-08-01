package com.example.appcenter_project.domain.studentIdDisclosure.service;

import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.domain.roommate.entity.RoommateChattingRoom;
import com.example.appcenter_project.domain.roommate.repository.RoommateChattingRoomRepository;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.request.RequestCreateDisclosureDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureAcceptDto;
import com.example.appcenter_project.domain.studentIdDisclosure.entity.StudentIdDisclosureRequest;
import com.example.appcenter_project.domain.studentIdDisclosure.enums.DisclosureRequestStatus;
import com.example.appcenter_project.domain.studentIdDisclosure.repository.StudentIdDisclosureRequestRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentIdDisclosureTransactionService {

    private final StudentIdDisclosureRequestRepository disclosureRequestRepository;
    private final OpenChatRoomRepository openChatRoomRepository;
    private final OpenChatParticipantRepository openChatParticipantRepository;
    private final RoommateChattingRoomRepository roommateChattingRoomRepository;
    private final UserRepository userRepository;

    public record SaveResult(Long savedId, boolean isRoommateRoom) {}

    @Transactional
    public SaveResult saveRequest(Long requesterId, RequestCreateDisclosureDto dto) {
        Long roomId = dto.getRoomId();
        Long targetId = dto.getTargetId();

        if (requesterId.equals(targetId)) {
            throw new CustomException(ErrorCode.DISCLOSURE_CANNOT_REQUEST_SELF);
        }

        boolean isRoommateRoom = resolveIsRoommateRoom(roomId, requesterId, targetId);

        disclosureRequestRepository.deleteByRequesterIdAndTargetIdAndRoomId(requesterId, targetId, roomId);

        try {
            StudentIdDisclosureRequest saved = disclosureRequestRepository.save(
                    StudentIdDisclosureRequest.create(requesterId, targetId, roomId));
            return new SaveResult(saved.getId(), isRoommateRoom);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DISCLOSURE_REQUEST_ALREADY_EXISTS);
        }
    }

    @Transactional
    public void cancelRequest(Long requesterId, Long requestId) {
        StudentIdDisclosureRequest request = disclosureRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.DISCLOSURE_REQUEST_NOT_FOUND));

        if (!request.getRequesterId().equals(requesterId)) {
            throw new CustomException(ErrorCode.DISCLOSURE_REQUEST_FORBIDDEN);
        }

        request.cancel();
    }

    @Transactional
    public AcceptResult acceptRequest(Long targetId, Long requestId) {
        StudentIdDisclosureRequest request = disclosureRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.DISCLOSURE_REQUEST_NOT_FOUND));

        if (!request.getTargetId().equals(targetId)) {
            throw new CustomException(ErrorCode.DISCLOSURE_REQUEST_FORBIDDEN);
        }

        request.accept();

        User requester = userRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ResponseDisclosureAcceptDto dto = ResponseDisclosureAcceptDto.builder()
                .requestId(request.getId())
                .requesterStudentNumber(requester.getStudentNumber())
                .build();

        boolean isRoommateRoom = resolveIsRoommateRoom(
                request.getRoomId(), request.getRequesterId(), request.getTargetId());
        return new AcceptResult(dto, requester.getId(), request.getRoomId(), isRoommateRoom);
    }

    public record AcceptResult(ResponseDisclosureAcceptDto dto, Long requesterId, Long roomId, boolean isRoommateRoom) {}

    @Transactional
    public RejectResult rejectRequest(Long targetId, Long requestId) {
        StudentIdDisclosureRequest request = disclosureRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.DISCLOSURE_REQUEST_NOT_FOUND));

        if (!request.getTargetId().equals(targetId)) {
            throw new CustomException(ErrorCode.DISCLOSURE_REQUEST_FORBIDDEN);
        }

        request.reject();

        boolean isRoommateRoom = resolveIsRoommateRoom(
                request.getRoomId(), request.getRequesterId(), request.getTargetId());
        return new RejectResult(request.getRequesterId(), request.getRoomId(), isRoommateRoom);
    }

    public record RejectResult(Long requesterId, Long roomId, boolean isRoommateRoom) {}

    @Transactional
    public void deleteByRoomAndUser(Long roomId, Long userId) {
        disclosureRequestRepository.deleteByRoomIdAndUserId(roomId, userId);
    }

    //@return true = 룸메이트 채팅방, false = 오픈채팅방
    private boolean resolveIsRoommateRoom(Long roomId, Long userA, Long userB) {
        boolean openChatExists = openChatRoomRepository.existsById(roomId);
        if (openChatExists
                && openChatParticipantRepository.existsByRoomIdAndUserId(roomId, userA)
                && openChatParticipantRepository.existsByRoomIdAndUserId(roomId, userB)) {
            return false;
        }

        RoommateChattingRoom roommateRoom = roommateChattingRoomRepository.findById(roomId).orElse(null);
        if (roommateRoom != null
                && isInRoommateRoom(roommateRoom, userA)
                && isInRoommateRoom(roommateRoom, userB)) {
            return true;
        }

        if (!openChatExists && roommateRoom == null) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
        }
        throw new CustomException(ErrorCode.DISCLOSURE_NOT_IN_SAME_ROOM);
    }

    private boolean isInRoommateRoom(RoommateChattingRoom room, Long userId) {
        return room.getHost().getId().equals(userId)
                || room.getGuest().getId().equals(userId);
    }
}
