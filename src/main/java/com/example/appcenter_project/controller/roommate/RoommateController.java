package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateFormDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateSimilarityDto;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.roommate.RoommateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roommate")
@RequiredArgsConstructor
public class RoommateController {

    private final RoommateService roommateService;

    @PostMapping("/post")
    public ResponseEntity<ResponseRoommatePostDto> createRoommatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestRoommateFormDto requestDto
    ) {
        Long userId = userDetails.getId(); // 인증된 사용자 ID 가져오기
        ResponseRoommatePostDto responseDto = roommateService.createRoommateCheckListandBoard(requestDto, userId);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ResponseRoommatePostDto>> getRoommateBoardList() {
        return ResponseEntity.ok(roommateService.getRoommateBoardList());
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ResponseRoommatePostDto> getRoommateBoardDetail(@PathVariable Long boardId){
        return ResponseEntity.ok(roommateService.getRoommateBoardDetail(boardId));
    }

    @GetMapping("/similar")
    public ResponseEntity<List<ResponseRoommateSimilarityDto>> getSimilarRoommates(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId(); // 로그인된 사용자의 ID 가져오기
        List<ResponseRoommateSimilarityDto> similarList = roommateService.getSimilarRoommateBoards(userId);
        return ResponseEntity.ok(similarList);
    }
}

