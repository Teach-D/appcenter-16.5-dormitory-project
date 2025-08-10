package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateFormDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateCheckListDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateSimilarityDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.roommate.RoommateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roommates")
@RequiredArgsConstructor
public class RoommateController implements RoommateApiSpecification{

    private final RoommateService roommateService;

    @Override
    @PostMapping
    public ResponseEntity<ResponseRoommatePostDto> createRoommatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestRoommateFormDto requestDto
    ) {
        Long userId = userDetails.getId(); // 인증된 사용자 ID 가져오기
        ResponseRoommatePostDto responseDto = roommateService.createRoommateCheckListandBoard(requestDto, userId);
        return ResponseEntity.status(201).body(responseDto);
    }

    @Override
    @GetMapping("/list")
    public ResponseEntity<List<ResponseRoommatePostDto>> getRoommateBoardList(
            jakarta.servlet.http.HttpServletRequest request
    ) {
        return ResponseEntity.ok(roommateService.getRoommateBoardList(request));
    }

    @Override
    @GetMapping("/{boardId}")
    public ResponseEntity<ResponseRoommatePostDto> getRoommateBoardDetail(
            @PathVariable Long boardId,
            jakarta.servlet.http.HttpServletRequest request
    ){
        return ResponseEntity.ok(roommateService.getRoommateBoardDetail(boardId, request));
    }

    @Override
    @GetMapping("/similar")
    public ResponseEntity<List<ResponseRoommateSimilarityDto>> getSimilarRoommates(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        Long userId = userDetails.getId();
        return ResponseEntity.ok(roommateService.getSimilarRoommateBoards(userId, request));
    }

    @Override
    @PutMapping
    public ResponseEntity<ResponseRoommatePostDto> updateRoommateCheckListAndBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestRoommateFormDto requestDto,
            jakarta.servlet.http.HttpServletRequest request // 추가
    ) {
        Long userId = userDetails.getId();
        ResponseRoommatePostDto updated =
                roommateService.updateRoommateChecklistAndBoard(requestDto, userId, request); // 변경
        return ResponseEntity.ok(updated);
    }

    @Override
    @PostMapping("/{boardId}/like")
    public ResponseEntity<Integer> plusLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long boardId
    ) {
        Integer likeCount = roommateService.likePlusRoommateBoard(userDetails.getId(), boardId);
        return ResponseEntity.ok(likeCount);
    }

    @Override
    @DeleteMapping("/{boardId}/like")
    public ResponseEntity<Integer> minusLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long boardId
    ) {
        Integer likeCount = roommateService.likeMinusRoommateBoard(userDetails.getId(), boardId);
        return ResponseEntity.ok(likeCount);
    }

    @Override
    @GetMapping("/{boardId}/owner-matched")
    public ResponseEntity<Boolean> isBoardOwnerMatched(@PathVariable Long boardId) {
        boolean matched = roommateService.isRoommateBoardOwnerMatched(boardId);
        return ResponseEntity.ok(matched);
    }

    @Override
    @GetMapping("/{boardId}/liked")
    public ResponseEntity<Boolean> isRoommateBoardLiked(
            @PathVariable Long boardId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        boolean isLiked = roommateService.isRoommateBoardLikedByUser(boardId, userId);
        return ResponseEntity.ok(isLiked);
    }

    @Override
    @GetMapping("/my-checklist")
    public ResponseEntity<ResponseRoommateCheckListDto> getMyRoommateCheckList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        ResponseRoommateCheckListDto response = roommateService.getMyRoommateCheckList(userId);
        return ResponseEntity.ok(response);
    }

}
