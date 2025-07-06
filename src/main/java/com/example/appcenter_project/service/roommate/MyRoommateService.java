package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.response.roommate.ResponseMyRoommateInfoDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRuleDto;
import com.example.appcenter_project.entity.roommate.MyRoommate;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.roommate.MyRoommateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.MY_ROOMMATE_NOT_REGISTERED;

@Service
@RequiredArgsConstructor
public class MyRoommateService {

    private final MyRoommateRepository myRoommateRepository;

    @Transactional(readOnly = true)
    public ResponseMyRoommateInfoDto getMyRoommateInfo(Long userId){
        MyRoommate myRoommate = myRoommateRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        User roommate = myRoommate.getRoommate();

        return ResponseMyRoommateInfoDto.builder()
                .name(roommate.getName())
                .dormType(roommate.getDormType() != null ? roommate.getDormType().name() : null)
                .college(roommate.getCollege() != null ? roommate.getCollege().name() : null)
                .imagePath(roommate.getImage() != null ? roommate.getImage().getFilePath() : null)
                .build();
    }

    //룸메이트 규칙 생성
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




}
