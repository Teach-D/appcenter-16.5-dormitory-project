package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateFormDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.service.roommate.RoommateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roommate")
@RequiredArgsConstructor
public class RoommateController {

    private final RoommateService roommateService;

    @PostMapping("/post")
    public ResponseEntity<ResponseRoommatePostDto> createRoommatePost(
            @RequestParam Long userId,
            @RequestBody RequestRoommateFormDto requestDto
    ) {
        ResponseRoommatePostDto responseDto = roommateService.createRoommateCheckListandBoard(requestDto, userId);
        return ResponseEntity.ok(responseDto);
    }

}
