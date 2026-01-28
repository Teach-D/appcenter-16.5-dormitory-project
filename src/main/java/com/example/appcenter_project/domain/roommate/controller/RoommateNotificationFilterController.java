package com.example.appcenter_project.domain.roommate.controller;

import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateNotificationFilterDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateNotificationFilterDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.service.RoommateNotificationFilterService;
import com.example.appcenter_project.global.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/roommates/notification-filter")
@RequiredArgsConstructor
public class RoommateNotificationFilterController implements RoommateNotificationFilterApiSpecification {

    private final RoommateNotificationFilterService filterService;

    @Override
    @PostMapping
    public ResponseEntity<Void> saveOrUpdateFilter(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestRoommateNotificationFilterDto requestDto
    ) {
        Long userId = userDetails.getId();
        filterService.saveOrUpdateFilter(userId, requestDto);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping
    public ResponseEntity<ResponseRoommateNotificationFilterDto> getFilter(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        ResponseRoommateNotificationFilterDto response = filterService.getFilter(userId);
        return ResponseEntity.status(OK).body(response);
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteFilter(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        filterService.deleteFilter(userId);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/matching-posts")
    public ResponseEntity<List<ResponseRoommatePostDto>> getFilteredBoards(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Long userId = userDetails.getId();
        List<ResponseRoommatePostDto> filteredBoards = filterService.getFilteredBoards(userId, request);
        return ResponseEntity.status(OK).body(filteredBoards);
    }
}

