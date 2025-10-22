package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.common.image.dto.ImageLinkDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseMyRoommateInfoDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRuleDto;
import com.example.appcenter_project.domain.roommate.entity.MyRoommate;
import com.example.appcenter_project.domain.roommate.entity.RoommateMatching;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.domain.roommate.repository.MyRoommateRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.common.image.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.appcenter_project.global.exception.ErrorCode.MY_ROOMMATE_NOT_REGISTERED;

@Transactional
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
                .imagePath(imageService.getImageUrl(ImageType.USER, roommate.getImage(), request))
                .build();
    }

    //룸메이트 규칙 생성,수정 (양방향 동기화)
    @Transactional
    public void createRule(Long userId, List<String> rules) {
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        // 본인의 룰 업데이트
        myRoommate.updateRules(rules);

        // 룸메이트의 룰도 동일하게 업데이트
        User roommateUser = myRoommate.getRoommate();
        MyRoommate roommateMyRoommate = myRoommateRepository.findByUserId(roommateUser.getId())
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        roommateMyRoommate.updateRules(rules);

        // 로그로 동기화 확인
        System.out.println("Rule synchronized - User " + userId + " and Roommate " + roommateUser.getId() + " both have rules: " + rules);
    }

    //룸메이트 규칙 삭제 (양방향 동기화)
    @Transactional
    public void deleteRule(Long userId) {
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        // 본인의 룰 삭제
        myRoommate.updateRules(null);

        // 룸메이트의 룰도 동일하게 삭제
        User roommateUser = myRoommate.getRoommate();
        MyRoommate roommateMyRoommate = myRoommateRepository.findByUserId(roommateUser.getId())
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        roommateMyRoommate.updateRules(null);

        // 로그로 동기화 확인
        System.out.println("Rules deleted and synchronized - User " + userId + " and Roommate " + roommateUser.getId() + " both have rules cleared");
    }

    @Transactional(readOnly = true)
    public ResponseRuleDto getRules(Long userId) {
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        return new ResponseRuleDto(myRoommate.getRule());
    }

    //규칙 수정 (양방향 동기화)
    @Transactional
    public void updateRules(Long userId, List<String> rules) {
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        if (myRoommate.getRule() == null || myRoommate.getRule().isEmpty()) {
            throw new CustomException(ErrorCode.RULE_NOT_FOUND); // 예외는 정의 필요
        }

        // 본인의 룰 업데이트
        myRoommate.updateRules(rules);

        // 룸메이트의 룰도 동일하게 업데이트
        User roommateUser = myRoommate.getRoommate();
        MyRoommate roommateMyRoommate = myRoommateRepository.findByUserId(roommateUser.getId())
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        roommateMyRoommate.updateRules(rules);

        // 로그로 동기화 확인
        System.out.println("Rule updated and synchronized - User " + userId + " and Roommate " + roommateUser.getId() + " both have rules: " + rules);
    }

    @Transactional
    public ImageLinkDto findMyRoommateImageByUserId(Long userId, HttpServletRequest request) {
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));
        Long myRoommateId = myRoommate.getRoommate().getId();

        ImageLinkDto imageLinkDto = imageService.findImage(ImageType.USER, myRoommateId, request);
        return imageLinkDto;
    }

    public ImageLinkDto getMyRoommateImage(Long userId, HttpServletRequest request) {
        return imageService.findImage(ImageType.USER, userId, request);
    }
}
