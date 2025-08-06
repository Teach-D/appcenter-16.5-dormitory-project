package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestMatchingByChatRoomDto;
import com.example.appcenter_project.dto.request.roommate.RequestMatchingDto;
import com.example.appcenter_project.dto.response.roommate.ResponseReceivedRoommateMatchingDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateMatchingDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.roommate.RoommateMatchingService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roommate-matching")
@RequiredArgsConstructor
public class RoommateMatchingController implements RoommateMatchingApiSpecification {

    private final RoommateMatchingService roommateMatchingService;

    @PostMapping("/request")
    public ResponseEntity<ResponseRoommateMatchingDto> requestMatching(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestMatchingDto requestDto
    ) {
        Long senderId = userDetails.getId(); // 인증된 사용자 ID
        ResponseRoommateMatchingDto responseDto = roommateMatchingService.requestMatching(senderId, requestDto.getReceiverStudentNumber());
        return ResponseEntity.status(201).body(responseDto);
    }

    @PostMapping("/request-by-chatroom")
    public ResponseEntity<ResponseRoommateMatchingDto> requestMatchingByChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestMatchingByChatRoomDto requestDto
    ) {
        Long senderId = userDetails.getId(); // 인증된 사용자 ID
        ResponseRoommateMatchingDto responseDto = roommateMatchingService.requestMatchingByChatRoom(senderId, requestDto.getChatRoomId());
        return ResponseEntity.status(201).body(responseDto);
    }


    @PatchMapping("/{matchingId}/accept")
    public ResponseEntity<Void> acceptMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        roommateMatchingService.acceptMatching(matchingId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{matchingId}/reject")
    public ResponseEntity<Void> rejectMatching(@PathVariable Long matchingId) {
        roommateMatchingService.rejectMatching(matchingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/received")
    public ResponseEntity<List<ResponseReceivedRoommateMatchingDto>> getReceivedMatchings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long receiverId = userDetails.getId();
        List<ResponseReceivedRoommateMatchingDto> responseDtos = roommateMatchingService.getReceivedMatchings(receiverId);
        return ResponseEntity.ok(responseDtos);
    }

    @PatchMapping("/{matchingId}/cancel")
    public ResponseEntity<Void> cancelMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        roommateMatchingService.cancelMatching(matchingId, userDetails.getId());
        return ResponseEntity.ok().build();
    }


}


