package com.example.appcenter_project.domain.roommate.controller;

import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateFormDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateCheckListDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateSimilarityDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.roommate.service.RoommateService;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping("/latest10/random")
    public ResponseEntity<ResponseRoommatePostDto> getRandomFromLatest10(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        Long userId = userDetails.getId();
        return ResponseEntity.ok(roommateService.getRandomFromLatest10(userId, request));
    }

    @GetMapping("/list/scroll")
    public ResponseEntity<List<ResponseRoommatePostDto>> getRoommateBoardListScroll(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(roommateService.getRoommateBoardListScroll(request, lastId, size));
    }

    @GetMapping("/list/similar/scroll/me")
    public ResponseEntity<List<ResponseRoommateSimilarityDto>> getSimilarRoommateBoardListScrollForMe(
            @RequestParam(required = false) Integer lastPct,    // 마지막 유사도 퍼센트
            @RequestParam(required = false) Long lastBoardId,   // 마지막 boardId
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(
                roommateService.getSimilarRoommateBoardListScrollForMe(
                        request,
                        userDetails.getId(),
                        lastPct,
                        lastBoardId,
                        size
                )
        );
    }

}
