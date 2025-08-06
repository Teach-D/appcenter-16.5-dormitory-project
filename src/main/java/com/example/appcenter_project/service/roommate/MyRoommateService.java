package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.response.roommate.ResponseMyRoommateInfoDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRuleDto;
import com.example.appcenter_project.entity.roommate.MyRoommate;
import com.example.appcenter_project.entity.roommate.RoommateMatching;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.roommate.MatchingStatus;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.roommate.MyRoommateRepository;
import com.example.appcenter_project.repository.roommate.RoommateMatchingRepository;
import com.example.appcenter_project.service.image.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.MY_ROOMMATE_NOT_REGISTERED;

@Service
@RequiredArgsConstructor
public class MyRoommateService {

    private final MyRoommateRepository myRoommateRepository;
    private final ImageService imageService;
    private final RoommateMatchingRepository roommateMatchingRepository;

    @Transactional(readOnly = true)
    public ResponseMyRoommateInfoDto getMyRoommateInfo(Long userId, HttpServletRequest request){
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        User roommate = myRoommate.getRoommate();

        // COMPLETED 매칭 정보 찾기 (양방향)
        RoommateMatching matching = roommateMatchingRepository.findBySenderAndReceiverAndStatus(
                myRoommate.getUser(), roommate, MatchingStatus.COMPLETED
        ).orElseGet(() -> roommateMatchingRepository.findBySenderAndReceiverAndStatus(
                roommate, myRoommate.getUser(), MatchingStatus.COMPLETED
        ).orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_MATCHING_NOT_FOUND)));

        return ResponseMyRoommateInfoDto.builder()
                .matchingId(matching.getId())
                .name(roommate.getName())
                .dormType(roommate.getDormType() != null ? roommate.getDormType().name() : null)
                .college(roommate.getCollege() != null ? roommate.getCollege().toValue() : null)
                .imagePath(imageService.findUserImageUrlByUserId(roommate.getId(), request).getFileName())
                .build();
    }

    //룸메이트 규칙 생성,수정
    @Transactional
    public void createRule(Long userId, List<String> rules) {
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        myRoommate.updateRules(rules);
    }

    //룸메이트 규칙 삭제
    @Transactional
    public void deleteRule(Long userId) {
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        myRoommate.updateRules(null);
    }

    @Transactional(readOnly = true)
    public ResponseRuleDto getRules(Long userId) {
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        return new ResponseRuleDto(myRoommate.getRule());
    }

    //규칙 수정
    @Transactional
    public void updateRules(Long userId, List<String> rules) {
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        if (myRoommate.getRule() == null || myRoommate.getRule().isEmpty()) {
            throw new CustomException(ErrorCode.RULE_NOT_FOUND); // 예외는 정의 필요
        }

        myRoommate.updateRules(rules);
    }

    @Transactional
    public ImageLinkDto findMyRoommateImageByUserId(Long userId, HttpServletRequest request) {
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));
        Long myRoommateId = myRoommate.getRoommate().getId();

        ImageLinkDto imageLinkDto = imageService.findUserTimeTableImageUrlByUserId(myRoommateId, request);
        return imageLinkDto;
    }

    public ImageLinkDto getMyRoommateImage(Long userId, HttpServletRequest request) {
        return imageService.findUserImageUrlByUserId(userId, request);
    }
}
