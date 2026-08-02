package com.example.appcenter_project.domain.studentIdDisclosure.service;

import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.domain.openChat.service.OpenChatMessageService;
import com.example.appcenter_project.domain.roommate.service.RoommateChattingChatService;
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
    private final OpenChatMessageService openChatMessageService;
    private final RoommateChattingChatService roommateChattingChatService;

    public ResponseDisclosureSendDto sendRequest(Long requesterId, RequestCreateDisclosureDto dto) {
        StudentIdDisclosureTransactionService.SaveResult saved = transactionService.saveRequest(requesterId, dto);

        userRepository.findById(dto.getTargetId()).ifPresent(targetUser -> {
            if (targetUser.getReceiveNotificationTypes().contains(NotificationType.CHAT)) {
                fcmMessageService.sendNotification(targetUser, "학번 공개 요청", "새로운 학번 공개 요청이 도착했습니다.");
            }
        });

        if (saved.isRoommateRoom()) {
            roommateChattingChatService.sendStudentIdRequestMessage(dto.getRoomId(), requesterId, saved.savedId());
        } else {
            openChatMessageService.sendStudentIdRequestMessage(dto.getRoomId(), requesterId, saved.savedId());
        }

        return ResponseDisclosureSendDto.builder()
                .requestId(saved.savedId())
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

        if (result.isRoommateRoom()) {
            roommateChattingChatService.sendSystemMessageById(result.roomId(), "학번 공유가 수락되었습니다.");
        } else {
            openChatMessageService.sendSystemMessage(result.roomId(), "학번 공유가 수락되었습니다.");
        }

        return result.dto();
    }

    public void reject(Long targetId, Long requestId) {
        StudentIdDisclosureTransactionService.RejectResult result = transactionService.rejectRequest(targetId, requestId);

        userRepository.findById(result.requesterId()).ifPresent(requester -> {
            if (requester.getReceiveNotificationTypes().contains(NotificationType.CHAT)) {
                fcmMessageService.sendNotification(requester, "학번 공개 거절", "학번 공개 요청이 거절되었습니다.");
            }
        });

        if (result.isRoommateRoom()) {
            roommateChattingChatService.sendSystemMessageById(result.roomId(), "학번 공유가 거절되었습니다.");
        } else {
            openChatMessageService.sendSystemMessage(result.roomId(), "학번 공유가 거절되었습니다.");
        }
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
                .findFirstByRoomIdAndRequesterIdAndTargetIdAndStatus(roomId, currentUserId, targetId, DisclosureRequestStatus.PENDING);
        if (pendingSent.isPresent()) {
            return ResponseDisclosureStatusDto.builder()
                    .status("PENDING_SENT")
                    .requestId(pendingSent.get().getId())
                    .build();
        }

        Optional<StudentIdDisclosureRequest> pendingReceived = disclosureRequestRepository
                .findFirstByRoomIdAndRequesterIdAndTargetIdAndStatus(roomId, targetId, currentUserId, DisclosureRequestStatus.PENDING);
        if (pendingReceived.isPresent()) {
            return ResponseDisclosureStatusDto.builder()
                    .status("PENDING_RECEIVED")
                    .requestId(pendingReceived.get().getId())
                    .build();
        }

        Optional<StudentIdDisclosureRequest> rejectedSent = disclosureRequestRepository
                .findFirstByRoomIdAndRequesterIdAndTargetIdAndStatus(roomId, currentUserId, targetId, DisclosureRequestStatus.REJECTED);
        if (rejectedSent.isPresent()) {
            return ResponseDisclosureStatusDto.builder()
                    .status("REJECTED")
                    .requestId(rejectedSent.get().getId())
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
