package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestMatchingDto;
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

    @PatchMapping("/{matchingId}/accept")
    public ResponseEntity<Void> acceptMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        roommateMatchingService.acceptMatching(matchingId, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}


