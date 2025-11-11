package com.example.appcenter_project.domain.roommate.controller;

import com.example.appcenter_project.common.image.dto.ImageLinkDto;
import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateRuleDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseMyRoommateInfoDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRuleDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.roommate.service.MyRoommateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/my-roommate")
@RequiredArgsConstructor
public class MyRoommateController implements MyRoommateApiSpecification {

    private final MyRoommateService myRoommateService;

    @GetMapping("/informations")
    public ResponseEntity<ResponseMyRoommateInfoDto> getMyRoommate(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request) {
        Long userId = userDetails.getId();
        ResponseMyRoommateInfoDto response = myRoommateService.getMyRoommateInfo(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/image")
    public ResponseEntity<ImageLinkDto> findUserImageByUserId(
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        try {

            ImageLinkDto imageLinkDto = myRoommateService.getMyRoommateImage(user.getId(), request);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(imageLinkDto);
        } catch (Exception e) {
            log.error("Error retrieving user image: ", e);
            throw e;
        }
    }

    @GetMapping("/time-table-image")
    public ResponseEntity<ImageLinkDto> findMyRoommateImageByUserId(
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        try {
            ImageLinkDto imageLinkDto = myRoommateService.findMyRoommateImageByUserId(user.getId(), request);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(imageLinkDto);
        } catch (Exception e) {
            log.error("Error retrieving user timetable image: ", e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<Void> createRule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestRoommateRuleDto dto) {
        myRoommateService.createRule(userDetails.getId(), dto.getRules());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteRule(@AuthenticationPrincipal CustomUserDetails userDetails) {
        myRoommateService.deleteRule(userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rules")
    public ResponseEntity<ResponseRuleDto> getRules(@AuthenticationPrincipal CustomUserDetails userDetails) {
        ResponseRuleDto response = myRoommateService.getRules(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rules")
    public ResponseEntity<Void> updateRules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestRoommateRuleDto dto) {
        myRoommateService.updateRules(userDetails.getId(), dto.getRules());
        return ResponseEntity.ok().build();
    }

}
