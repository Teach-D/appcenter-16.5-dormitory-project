package com.example.appcenter_project.domain.studentIdDisclosure.service;

import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.request.RequestCreateDisclosureDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureAcceptDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureSendDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureStatusDto;
import com.example.appcenter_project.domain.studentIdDisclosure.entity.StudentIdDisclosureRequest;
import com.example.appcenter_project.domain.studentIdDisclosure.enums.DisclosureRequestStatus;
import com.example.appcenter_project.domain.studentIdDisclosure.repository.StudentIdDisclosureRequestRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentIdDisclosureRequestService {

    private final StudentIdDisclosureTransactionService transactionService;
    private final StudentIdDisclosureRequestRepository disclosureRequestRepository;
    private final UserRepository userRepository;
    private final FcmMessageService fcmMessageService;

    public ResponseDisclosureSendDto sendRequest(Long requesterId, RequestCreateDisclosureDto dto) {
        Long savedId = transactionService.saveRequest(requesterId, dto);

        userRepository.findById(dto.getTargetId()).ifPresent(targetUser -> {
            if (targetUser.getReceiveNotificationTypes().contains(NotificationType.CHAT)) {
                fcmMessageService.sendNotification(targetUser, "학번 공개 요청", "새로운 학번 공개 요청이 도착했습니다.");
            }
        });

        return ResponseDisclosureSendDto.builder()
                .requestId(savedId)
                .build();
    }

    public void cancel(Long requesterId, Long requestId) {
        transactionService.cancelRequest(requesterId, requestId);
    }

    public ResponseDisclosureAcceptDto accept(Long targetId, Long requestId) {
        StudentIdDisclosureTransactionService.AcceptResult result = transactionService.acceptRequest(targetId, requestId);

        userRepository.findById(result.requesterId()).ifPresent(requester -> {
            if (requester.getReceiveNotificationTypes().contains(NotificationType.CHAT)) {
                fcmMessageService.sendNotification(requester, "학번 공개 수락", "학번 공개 요청이 수락되었습니다.");
            }
        });

        return result.dto();
    }

    public void reject(Long targetId, Long requestId) {
        Long requesterId = transactionService.rejectRequest(targetId, requestId);

        userRepository.findById(requesterId).ifPresent(requester -> {
            if (requester.getReceiveNotificationTypes().contains(NotificationType.CHAT)) {
                fcmMessageService.sendNotification(requester, "학번 공개 거절", "학번 공개 요청이 거절되었습니다.");
            }
        });
    }

    @Transactional(readOnly = true)
    public ResponseDisclosureStatusDto getStatus(Long currentUserId, Long roomId, Long targetId) {
        Optional<StudentIdDisclosureRequest> acceptedSent = disclosureRequestRepository
                .findByRoomIdAndRequesterIdAndTargetIdAndStatus(roomId, currentUserId, targetId, DisclosureRequestStatus.ACCEPTED);
        if (acceptedSent.isPresent()) {
            User target = userRepository.findById(targetId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            return ResponseDisclosureStatusDto.builder()
                    .status("DISCLOSED")
                    .requestId(acceptedSent.get().getId())
                    .targetStudentNumber(target.getStudentNumber())
                    .build();
        }

        Optional<StudentIdDisclosureRequest> acceptedReceived = disclosureRequestRepository
                .findByRoomIdAndRequesterIdAndTargetIdAndStatus(roomId, targetId, currentUserId, DisclosureRequestStatus.ACCEPTED);
        if (acceptedReceived.isPresent()) {
            User target = userRepository.findById(targetId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            return ResponseDisclosureStatusDto.builder()
                    .status("DISCLOSED")
                    .requestId(acceptedReceived.get().getId())
                    .targetStudentNumber(target.getStudentNumber())
                    .build();
        }

        Optional<StudentIdDisclosureRequest> pendingSent = disclosureRequestRepository
                .findByRoomIdAndRequesterIdAndTargetIdAndStatus(roomId, currentUserId, targetId, DisclosureRequestStatus.PENDING);
        if (pendingSent.isPresent()) {
            return ResponseDisclosureStatusDto.builder()
                    .status("PENDING_SENT")
                    .requestId(pendingSent.get().getId())
                    .build();
        }

        Optional<StudentIdDisclosureRequest> pendingReceived = disclosureRequestRepository
                .findByRoomIdAndRequesterIdAndTargetIdAndStatus(roomId, targetId, currentUserId, DisclosureRequestStatus.PENDING);
        if (pendingReceived.isPresent()) {
            return ResponseDisclosureStatusDto.builder()
                    .status("PENDING_RECEIVED")
                    .requestId(pendingReceived.get().getId())
                    .build();
        }

        return ResponseDisclosureStatusDto.builder()
                .status("NONE")
                .build();
    }

    public void deleteByRoomAndUser(Long roomId, Long userId) {
        transactionService.deleteByRoomAndUser(roomId, userId);
    }
}
