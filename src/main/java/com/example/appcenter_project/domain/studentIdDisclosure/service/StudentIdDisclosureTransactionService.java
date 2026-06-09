package com.example.appcenter_project.domain.studentIdDisclosure.service;

import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentIdDisclosureTransactionService {

    private final StudentIdDisclosureRequestRepository disclosureRequestRepository;
    private final OpenChatRoomRepository openChatRoomRepository;
    private final OpenChatParticipantRepository openChatParticipantRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long saveRequest(Long requesterId, RequestCreateDisclosureDto dto) {
        Long roomId = dto.getRoomId();
        Long targetId = dto.getTargetId();

        if (requesterId.equals(targetId)) {
            throw new CustomException(ErrorCode.DISCLOSURE_CANNOT_REQUEST_SELF);
        }

        if (!openChatRoomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
        }

        if (!openChatParticipantRepository.existsByRoomIdAndUserId(roomId, requesterId)
                || !openChatParticipantRepository.existsByRoomIdAndUserId(roomId, targetId)) {
            throw new CustomException(ErrorCode.DISCLOSURE_NOT_IN_SAME_ROOM);
        }

        if (disclosureRequestRepository.existsByRequesterIdAndTargetIdAndRoomIdAndStatusIn(
                requesterId, targetId, roomId, List.of(DisclosureRequestStatus.PENDING, DisclosureRequestStatus.ACCEPTED))) {
            throw new CustomException(ErrorCode.DISCLOSURE_REQUEST_ALREADY_EXISTS);
        }

        disclosureRequestRepository.deleteByRequesterIdAndTargetIdAndRoomId(requesterId, targetId, roomId);

        try {
            StudentIdDisclosureRequest saved = disclosureRequestRepository.save(
                    StudentIdDisclosureRequest.create(requesterId, targetId, roomId));
            return saved.getId();
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

        return new AcceptResult(dto, requester.getId());
    }

    public record AcceptResult(ResponseDisclosureAcceptDto dto, Long requesterId) {}

    @Transactional
    public Long rejectRequest(Long targetId, Long requestId) {
        StudentIdDisclosureRequest request = disclosureRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.DISCLOSURE_REQUEST_NOT_FOUND));

        if (!request.getTargetId().equals(targetId)) {
            throw new CustomException(ErrorCode.DISCLOSURE_REQUEST_FORBIDDEN);
        }

        request.reject();

        return request.getRequesterId();
    }

    @Transactional
    public void deleteByRoomAndUser(Long roomId, Long userId) {
        disclosureRequestRepository.deleteByRoomIdAndUserId(roomId, userId);
    }
}
