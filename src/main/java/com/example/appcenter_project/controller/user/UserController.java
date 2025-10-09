package com.example.appcenter_project.controller.user;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.user.RequestTokenDto;
import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.dto.request.user.RequestUserRole;
import com.example.appcenter_project.dto.request.user.SignupUser;
import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.dto.response.user.ResponseUserRole;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.user.UserService;
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

    @PostMapping
    public ResponseEntity<ResponseLoginDto> saveUser(@Valid @RequestBody SignupUser signupUser) {
        return ResponseEntity.status(CREATED).body(userService.saveUser(signupUser));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> reissueAccessToken(@RequestBody RequestTokenDto requestTokenDto) {
        if (requestTokenDto.getRefreshToken() != null && requestTokenDto.getRefreshToken().startsWith("Bearer ")) {
            String refreshToken = requestTokenDto.getRefreshToken().substring(7);
            String newAccessToken = userService.reissueAccessToken(refreshToken);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token이 유효하지 않습니다.");
    }

    @PostMapping("/role")
    public ResponseEntity<Void> changeUserRole(@RequestBody RequestUserRole requestUserRole) {
        userService.changeUserRole(requestUserRole);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping
    public ResponseEntity<ResponseUserDto> findUserByUserId(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(userService.findUserByUserId(user.getId()));
    }

    @GetMapping("/role")
    public ResponseEntity<List<ResponseUserRole>> findUserDormitoryRole() {
        return ResponseEntity.status(OK).body(userService.findUserDormitoryRole());
    }

/*
    @GetMapping("/image")
    public ResponseEntity<Resource> findUserImageByUserId(@AuthenticationPrincipal CustomUserDetails user) {
        ImageDto imageDto = imageService.findUserImageByUserId(user.getId());

        return ResponseEntity.status(OK)
                .contentType(MediaType.parseMediaType(imageDto.getContentType()))
                .body(imageDto.getResource());
    }
*/

    @GetMapping(value = "/image")
    public ResponseEntity<ImageLinkDto> findUserImageByUserId(
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

    @GetMapping("/board")
    public ResponseEntity<List<ResponseBoardDto>> findBoardByUserId(@AuthenticationPrincipal CustomUserDetails user, HttpServletRequest request) {
        return ResponseEntity.status(OK).body(userService.findBoardByUserId_optimization(user.getId(), request));
    }

    @GetMapping("/like")
    public ResponseEntity<List<ResponseBoardDto>> findLikeByUserId(@AuthenticationPrincipal CustomUserDetails user, HttpServletRequest request) {
        return ResponseEntity.status(OK).body(userService.findLikeByUserId_optimization(user.getId(), request));
    }

    @PutMapping
    public ResponseEntity<ResponseUserDto> updateUser(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody RequestUserDto requestUserDto) {
        return ResponseEntity.status(OK).body(userService.updateUser(user.getId(), requestUserDto));
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

    @PutMapping("/agreement")
    public ResponseEntity<Void> updateAgreement(@AuthenticationPrincipal CustomUserDetails user, @Parameter boolean isTermsAgreed, @Parameter boolean isPrivacyAgreed) {
        userService.updateAgreement(user.getId(), isTermsAgreed, isPrivacyAgreed);
        return ResponseEntity.status(OK).build();
    }

    @GetMapping("/time-table-image")
    public ResponseEntity<ImageLinkDto> findUserTimeTableImageByUserId(
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

    @DeleteMapping("/time-table-image")
    public ResponseEntity<Void> deleteUserTimeTableImage(@AuthenticationPrincipal CustomUserDetails user) {
        userService.deleteUserTimeTableImage(user.getId());
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal CustomUserDetails user) {
        userService.deleteUser(user.getId());
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
