package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.response.roommate.ResponseReceivedRoommateMatchingDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateMatchingDto;
import com.example.appcenter_project.entity.roommate.MyRoommate;
import com.example.appcenter_project.entity.roommate.RoommateChattingRoom;
import com.example.appcenter_project.entity.roommate.RoommateMatching;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.roommate.MatchingStatus;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.roommate.MyRoommateRepository;
import com.example.appcenter_project.repository.roommate.RoommateChattingRoomRepository;
import com.example.appcenter_project.repository.roommate.RoommateMatchingRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class RoommateMatchingService {

    private final RoommateMatchingRepository roommateMatchingRepository;
    private final UserRepository userRepository;
    private final MyRoommateRepository myRoommateRepository;
    private final RoommateChattingRoomRepository roommateChattingRoomRepository;

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

        return ResponseRoommateMatchingDto.builder()
                .MatchingId(matching.getId())
                .reciverId(matching.getReceiver().getId())
                .status(matching.getStatus())
                .build();
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

        return ResponseRoommateMatchingDto.builder()
                .MatchingId(matching.getId())
                .reciverId(matching.getReceiver().getId())
                .status(matching.getStatus())
                .build();
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

        myRoommateRepository.save(myRoommate1);
        myRoommateRepository.save(myRoommate2);
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

        log.info("cancelMatching senderId : {}, receiverId:{}", sender.getId(), receiver.getId());

        // MyRoommate 레코드들 조회 후 삭제 (더 안전한 방법)
        Optional<MyRoommate> senderToReceiver = myRoommateRepository.findByUserAndRoommate(sender, receiver);
        Optional<MyRoommate> receiverToSender = myRoommateRepository.findByUserAndRoommate(receiver, sender);

        if (senderToReceiver.isPresent()) {
            myRoommateRepository.delete(senderToReceiver.get());
            log.info("Deleted MyRoommate: user {} -> roommate {}", sender.getId(), receiver.getId());
        }

        if (receiverToSender.isPresent()) {
            myRoommateRepository.delete(receiverToSender.get());
            log.info("Deleted MyRoommate: user {} -> roommate {}", receiver.getId(), sender.getId());
        }

        // RoommateMatching 레코드 완전 삭제
        roommateMatchingRepository.delete(matching);
        log.info("RoommateMatching record deleted for matchingId: {}", matchingId);
    }


}
