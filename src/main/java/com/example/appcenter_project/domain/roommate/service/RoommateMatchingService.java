package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.service.NotificationService;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseReceivedRoommateMatchingDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateMatchingDto;
import com.example.appcenter_project.domain.roommate.entity.MyRoommate;
import com.example.appcenter_project.domain.roommate.entity.RoommateChattingRoom;
import com.example.appcenter_project.domain.roommate.entity.RoommateMatching;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.domain.roommate.repository.MyRoommateRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateChattingRoomRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class RoommateMatchingService {

    private final RoommateMatchingRepository roommateMatchingRepository;
    private final UserRepository userRepository;
    private final MyRoommateRepository myRoommateRepository;
    private final RoommateChattingRoomRepository roommateChattingRoomRepository;
    private final FcmMessageService fcmMessageService;
    private final NotificationService notificationService;

    //매칭요청 학번
    @Transactional
    public ResponseRoommateMatchingDto requestMatching(Long senderId, String receiverStudentNumber) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        User receiver = userRepository.findByStudentNumber(receiverStudentNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        // "이미 매칭된 사람" 체크 기준 개선!
        boolean receiverAlreadyMatched =
                roommateMatchingRepository.existsBySenderAndStatus(receiver, MatchingStatus.COMPLETED) ||
                        roommateMatchingRepository.existsByReceiverAndStatus(receiver, MatchingStatus.COMPLETED);

        if (receiverAlreadyMatched) {
            throw new CustomException(ErrorCode.ROOMMATE_ALREADY_MATCHED);
        }

        boolean exists = roommateMatchingRepository.existsBySenderAndReceiver(sender, receiver);
        if (exists) {
            throw new CustomException(ErrorCode.ROOMMATE_MATCHING_ALREADY_REQUESTED);
        }

        RoommateMatching matching = RoommateMatching.builder()
                .check(MatchingStatus.REQUEST)
                .sender(sender)
                .receiver(receiver)
                .build();

        roommateMatchingRepository.save(matching);

        sendRequestNotification(sender, receiver, matching.getId());

        return ResponseRoommateMatchingDto.builder()
                .MatchingId(matching.getId())
                .reciverId(matching.getReceiver().getId())
                .status(matching.getStatus())
                .build();
    }

    private void sendRequestNotification(User sender, User receiver, Long matchingId) {
        Notification requestNotification = notificationService.createRoommateRequestNotification(sender.getName(), matchingId);
        notificationService.createUserNotification(receiver, requestNotification);
        fcmMessageService.sendNotification(receiver, requestNotification.getTitle(), requestNotification.getBody());
    }

    // 매칭 요청 채팅방id
    public ResponseRoommateMatchingDto requestMatchingByChatRoom(Long senderId, Long chatRoomId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        // 채팅방 조회
        RoommateChattingRoom chattingRoom = roommateChattingRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_CHAT_ROOM_NOT_FOUND));

        // 상대방 유저 추출 (본인을 sender로 가정, 상대는 host or guest)
        User receiver;
        if (chattingRoom.getHost().getId().equals(senderId)) {
            receiver = chattingRoom.getGuest();
        } else if (chattingRoom.getGuest().getId().equals(senderId)) {
            receiver = chattingRoom.getHost();
        } else {
            throw new CustomException(ErrorCode.ROOMMATE_FORBIDDEN_ACCESS);
        }

        // receiver가 이미 매칭된 사람인지 체크
        boolean receiverAlreadyMatched =
                roommateMatchingRepository.existsBySenderAndStatus(receiver, MatchingStatus.COMPLETED) ||
                        roommateMatchingRepository.existsByReceiverAndStatus(receiver, MatchingStatus.COMPLETED);

        if (receiverAlreadyMatched) {
            throw new CustomException(ErrorCode.ROOMMATE_ALREADY_MATCHED);
        }

        boolean exists = roommateMatchingRepository.existsBySenderAndReceiver(sender, receiver);
        if (exists) {
            throw new CustomException(ErrorCode.ROOMMATE_MATCHING_ALREADY_REQUESTED);
        }

        RoommateMatching matching = RoommateMatching.builder()
                .check(MatchingStatus.REQUEST)
                .sender(sender)
                .receiver(receiver)
                .build();

        roommateMatchingRepository.save(matching);

        sendRequestNotification(sender, receiver, matching.getId());

        return ResponseRoommateMatchingDto.builder()
                .MatchingId(matching.getId())
                .reciverId(matching.getReceiver().getId())
                .status(matching.getStatus())
                .build();
    }

    // 공통 FCM 발송 메서드
    private void sendFcmToReceiver(User sender, User receiver) {
        // 기존 코드
/*        for (FcmToken fcmToken : receiver.getFcmTokenList()) {
            if (fcmToken != null) {
                try {
                    fcmMessageService.sendNotification(
                            fcmToken.getToken(),
                            "룸메이트 매칭 요청",
                            sender.getName() + "님이 룸메이트 매칭을 요청했습니다."
                    );
                } catch (Exception e) {
                    log.error("FCM 발송 실패: {}", e.getMessage());
                }
            } else {
                log.warn("수신자 {}의 FCM 토큰이 없음", receiver.getId());
            }
        }*/
        fcmMessageService.sendNotification(
                receiver,
                "룸메이트 매칭 요청",
                sender.getName() + "님이 룸메이트 매칭을 요청했습니다."
        );
    }

    // 매칭 수락
    public void acceptMatching(Long matchingId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        RoommateMatching matching = roommateMatchingRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_MATCHING_NOT_FOUND));

        // 매칭 요청의 수신자가 현재 로그인한 사용자인지 확인
        if (!matching.getReceiver().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ROOMMATE_MATCHING_NOT_FOR_USER);
        }

        // 이미 완료된 요청인지 확인
        if (matching.getStatus() != MatchingStatus.REQUEST) {
            throw new CustomException(ErrorCode.ROOMMATE_MATCHING_ALREADY_COMPLETED);
        }

        // "이미 매칭된 사람" 체크 기준 개선!
        boolean userAlreadyMatched =
                roommateMatchingRepository.existsBySenderAndStatus(user, MatchingStatus.COMPLETED) ||
                        roommateMatchingRepository.existsByReceiverAndStatus(user, MatchingStatus.COMPLETED);

        if (userAlreadyMatched) {
            throw new CustomException(ErrorCode.ROOMMATE_ALREADY_MATCHED);
        }

        matching.complete();

        //myRoommate 저장 로직 추가
        User sender = matching.getSender();
        User receiver = matching.getReceiver();

        MyRoommate myRoommate1 = MyRoommate.builder()
                .user(sender)
                .roommate(receiver)
                .build();

        MyRoommate myRoommate2 = MyRoommate.builder()
                .user(receiver)
                .roommate(sender)
                .build();

        // 두 명의 RoommateBoard matched = true로 변경
        sender.getRoommateBoard().changeIsMatched(true);
        receiver.getRoommateBoard().changeIsMatched(true);

        myRoommateRepository.save(myRoommate1);
        myRoommateRepository.save(myRoommate2);

        sendAcceptNotification(sender, receiver, matching.getId());
    }

    private void sendAcceptNotification(User sender, User receiver, Long matchingId) {
        Notification acceptNotification = notificationService.createRoommateAcceptNotification(sender.getName(), matchingId);
        notificationService.createUserNotification(receiver, acceptNotification);
        fcmMessageService.sendNotification(receiver, acceptNotification.getTitle(), acceptNotification.getBody());

    }

    // 매칭 거절
    public void rejectMatching(Long matchingId) {
        RoommateMatching matching = roommateMatchingRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_MATCHING_NOT_FOUND));

        if (matching.getStatus() != MatchingStatus.REQUEST) {
            throw new CustomException(ErrorCode.ROOMMATE_MATCHING_ALREADY_COMPLETED);
        }

        matching.fail();
    }

    // 매칭조회
    @Transactional(readOnly = true)
    public List<ResponseReceivedRoommateMatchingDto> getReceivedMatchings(Long receiverId) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        List<RoommateMatching> matchings = roommateMatchingRepository.findAllByReceiverAndStatus(receiver, MatchingStatus.REQUEST);

        return matchings.stream()
                .map(matching -> ResponseReceivedRoommateMatchingDto.builder()
                        .matchingId(matching.getId())
                        .senderId(matching.getSender().getId())
                        .senderName(matching.getSender().getName())
                        .status(matching.getStatus())
                        .build())
                .toList();
    }

    // 매칭 취소 (완료된 상태에서만 가능)
    @Transactional
    public void cancelMatching(Long matchingId, Long userId) {
        RoommateMatching matching = roommateMatchingRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_MATCHING_NOT_FOUND));

        // 매칭이 COMPLETED 상태인지 확인
        if (matching.getStatus() != MatchingStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ROOMMATE_MATCHING_NOT_COMPLETED);
        }

        // sender나 receiver 중 한 명만 가능
        if (!matching.getSender().getId().equals(userId) && !matching.getReceiver().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ROOMMATE_MATCHING_NOT_FOR_USER);
        }

        // MyRoommate 관계도 해제
        User sender = matching.getSender();
        User receiver = matching.getReceiver();

        sender.getRoommateBoard().changeIsMatched(false);
        receiver.getRoommateBoard().changeIsMatched(false);

        log.info("=== cancelMatching START ===");
        log.info("matchingId: {}, userId: {}", matchingId, userId);
        log.info("senderId: {}, receiverId: {}", sender.getId(), receiver.getId());

        // MyRoommate 직접 삭제 (반환값으로 삭제된 행 수 확인)
        int deletedCount1 = myRoommateRepository.deleteByUserIdAndRoommateId(sender.getId(), receiver.getId());
        log.info("Deleted {} rows for sender {} -> receiver {}", deletedCount1, sender.getId(), receiver.getId());

        int deletedCount2 = myRoommateRepository.deleteByUserIdAndRoommateId(receiver.getId(), sender.getId());
        log.info("Deleted {} rows for receiver {} -> sender {}", deletedCount2, receiver.getId(), sender.getId());

        // 총 삭제된 행 수 확인
        log.info("Total deleted MyRoommate rows: {}", deletedCount1 + deletedCount2);

        // RoommateMatching 레코드 완전 삭제
        roommateMatchingRepository.delete(matching);
        log.info("RoommateMatching record deleted for matchingId: {}", matchingId);
        
        log.info("=== cancelMatching END ===");
    }


}
