package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.response.roommate.ResponseRoommateMatchingDto;
import com.example.appcenter_project.entity.roommate.RoommateMatching;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.roommate.MatchingStatus;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.roommate.RoommateMatchingRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoommateMatchingService {

    private final RoommateMatchingRepository roommateMatchingRepository;
    private final UserRepository userRepository;

    // 매칭 요청
    @Transactional
    public ResponseRoommateMatchingDto requestMatching(Long senderId, String receiverStudentNumber) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        User receiver = userRepository.findByStudentNumber(receiverStudentNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        if (receiver.getRoommateBoard() != null) {
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

        // 이미 매칭 완료된 사람인지 확인
        if (user.getRoommateBoard() != null) {
            throw new CustomException(ErrorCode.ROOMMATE_ALREADY_MATCHED);
        }

        matching.complete();
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

}
