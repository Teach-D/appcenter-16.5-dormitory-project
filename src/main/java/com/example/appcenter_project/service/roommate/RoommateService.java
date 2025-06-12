package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateFormDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.roommate.RoommateBoardRepository;
import com.example.appcenter_project.repository.roommate.RoommateCheckListRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoommateService {

    private final UserRepository userRepository;
    private final RoommateCheckListRepository roommateCheckListRepository;
    private final RoommateBoardRepository roommateBoardRepository;

    public ResponseRoommatePostDto createRoommateCheckListandBoard(RequestRoommateFormDto requestDto, Long userId) {
        // 1. 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        // 체크리스트 생성 및 저장
        RoommateCheckList roommateCheckList = RoommateCheckList.builder()
                .title(requestDto.getTitle())
                .dormPeriod(requestDto.getDormPeriod())
                .dormType(requestDto.getDormType())
                .college(requestDto.getCollege())
                .mbti(requestDto.getMbti())
                .smoking(requestDto.getSmoking())
                .snoring(requestDto.getSnoring())
                .toothGrind(requestDto.getToothGrind())
                .sleeper(requestDto.getSleeper())
                .showerHour(requestDto.getShowerHour())
                .showerTime(requestDto.getShowerTime())
                .bedTime(requestDto.getBedTime())
                .arrangement(requestDto.getArrangement())
                .comment(requestDto.getComment())
                .title(requestDto.getTitle())
                .build();

        roommateCheckListRepository.save(roommateCheckList);

        // 게시글 생성 및 저장
        RoommateBoard roommateBoard = RoommateBoard.builder()
                .title(requestDto.getTitle())
                .user(user)
                .roommateBoardLike(0)
                .roommateCheckList(roommateCheckList)
                .build();

        roommateBoardRepository.save(roommateBoard);

        // 응답 DTO 반환
        return ResponseRoommatePostDto.builder()
                .boardId(roommateBoard.getId())
                .title(roommateCheckList.getTitle())
                .dormPeriod(roommateCheckList.getDormPeriod())
                .dormType(roommateCheckList.getDormType())
                .college(roommateCheckList.getCollege())
                .mbti(roommateCheckList.getMbti())
                .smoking(roommateCheckList.getSmoking())
                .snoring(roommateCheckList.getSnoring())
                .toothGrind(roommateCheckList.getToothGrind())
                .sleeper(roommateCheckList.getSleeper())
                .showerHour(roommateCheckList.getShowerHour())
                .showerTime(roommateCheckList.getShowerTime())
                .bedTime(roommateCheckList.getBedTime())
                .arrangement(roommateCheckList.getArrangement())
                .comment(roommateCheckList.getComment())
                .build();
    }
}
