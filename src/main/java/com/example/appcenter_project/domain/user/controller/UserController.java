package com.example.appcenter_project.domain.user.controller;

import com.example.appcenter_project.common.metrics.annotation.TrackApi;
import com.example.appcenter_project.domain.user.dto.request.*;
import com.example.appcenter_project.common.image.dto.ImageLinkDto;
import com.example.appcenter_project.domain.user.dto.response.ResponseBoardDto;
import com.example.appcenter_project.domain.user.dto.response.ResponseLoginDto;
import com.example.appcenter_project.domain.user.dto.response.ResponseUserDto;
import com.example.appcenter_project.domain.user.dto.response.ResponseUserRole;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController implements UserApiSpecification {

    private final UserService userService;

    @TrackApi
    @PostMapping
    public ResponseEntity<ResponseLoginDto> saveUser(@Valid @RequestBody SignupUser signupUser) {
        return ResponseEntity.status(CREATED).body(userService.saveUser(signupUser));
    }

    @TrackApi
    @PostMapping("/freshman")
    public ResponseEntity<ResponseLoginDto> saveFreshman(@Valid @RequestBody SignupUser signupUser) {
        return ResponseEntity.status(CREATED).body(userService.saveFreshman(signupUser));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> reissueAccessToken(@RequestBody RequestTokenDto request) {
        try {
            String newAccessToken = userService.reissueAccessToken(request);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (IllegalArgumentException | CustomException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/push-notification")
    public ResponseEntity<Void> sendPushNotification(@RequestBody RequestUserPushNotification request) {
        userService.sendPushNotification(request);
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/role")
    public ResponseEntity<Void> changeUserRole(@RequestBody RequestUserRoleDto request) {
        userService.changeUserRole(request);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping
    public ResponseEntity<ResponseUserDto> findUser(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(userService.findUser(user.getId()));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<ResponseUserDto>> findAllUsers() {
        return ResponseEntity.status(OK).body(userService.findAllUsers());
    }

    @GetMapping("/role")
    public ResponseEntity<List<ResponseUserRole>> findUsersDormitoryRoles() {
        return ResponseEntity.status(OK).body(userService.findUsersDormitoryRoles());
    }

    @GetMapping("/board")
    public ResponseEntity<List<ResponseBoardDto>> findUserBoards(@AuthenticationPrincipal CustomUserDetails user, HttpServletRequest request) {
        return ResponseEntity.status(OK).body(userService.findUserBoards(user.getId(), request));
    }

    @GetMapping("/like")
    public ResponseEntity<List<ResponseBoardDto>> findUserLikedBoards(@AuthenticationPrincipal CustomUserDetails user, HttpServletRequest request) {
        return ResponseEntity.status(OK).body(userService.findUserLikedBoards(user.getId(), request));
    }

    @GetMapping(value = "/image")
    public ResponseEntity<ImageLinkDto> findUserImage(
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        try {
            ImageLinkDto imageLinkDto = userService.findUserImage(user.getId(), request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(imageLinkDto);
        } catch (Exception e) {
            log.error("Error retrieving user image: ", e);
            throw e;
        }
    }

    @GetMapping("/time-table-image")
    public ResponseEntity<ImageLinkDto> findUserTimeTableImage(
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {
        try {
            ImageLinkDto imageLinkDto = userService.findUserTimeTableImage(user.getId(), request);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(imageLinkDto);
        } catch (Exception e) {
            log.error("Error retrieving user timetable image: ", e);
            throw e;
        }
    }

    @PutMapping
    public ResponseEntity<ResponseUserDto> updateUser(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody RequestUserDto request) {
        return ResponseEntity.status(OK).body(userService.updateUser(user.getId(), request));
    }

    @PutMapping("/agreement")
    public ResponseEntity<Void> updateUserAgreement(@AuthenticationPrincipal CustomUserDetails user, @Parameter boolean isTermsAgreed, @Parameter boolean isPrivacyAgreed) {
        userService.updateUserAgreement(user.getId(), isTermsAgreed, isPrivacyAgreed);
        return ResponseEntity.status(OK).build();
    }

    @PutMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUserImage(@AuthenticationPrincipal CustomUserDetails user, @RequestPart MultipartFile image) {
        userService.updateUserImage(user.getId(), image);
        return ResponseEntity.status(OK).build();
    }

    @PutMapping(value = "/time-table-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUserTimeTableImage(@AuthenticationPrincipal CustomUserDetails user, @RequestPart MultipartFile image) {
        userService.updateUserTimeTableImage(user.getId(), image);
        return ResponseEntity.status(OK).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal CustomUserDetails user) {
        userService.deleteUser(user.getId());
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping("/time-table-image")
    public ResponseEntity<Void> deleteUserTimeTableImage(@AuthenticationPrincipal CustomUserDetails user) {
        userService.deleteUserTimeTableImage(user.getId());
        return ResponseEntity.status(NO_CONTENT).build();
    }
}