package com.example.appcenter_project.controller.user;

import com.example.appcenter_project.dto.ImageDto;
import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.dto.request.user.SignupUser;
import com.example.appcenter_project.dto.response.like.ResponseLikeDto;
import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.image.ImageService;
import com.example.appcenter_project.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<ResponseLoginDto> saveUser(@Valid @RequestBody SignupUser signupUser) {
        return ResponseEntity.status(CREATED).body(userService.saveUser(signupUser));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> reissueAccessToken(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String refreshToken = authorizationHeader.substring(7);
            String newAccessToken = userService.reissueAccessToken(refreshToken);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token이 유효하지 않습니다.");
    }

    @GetMapping
    public ResponseEntity<ResponseUserDto> findUserByUserId(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(userService.findUserByUserId(user.getId()));
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> findUserImageByUserId(@AuthenticationPrincipal CustomUserDetails user) {
        ImageDto imageDto = imageService.findUserImageByUserId(user.getId());

        return ResponseEntity.status(OK)
                .contentType(MediaType.parseMediaType(imageDto.getContentType()))
                .body(imageDto.getResource());
    }

    @GetMapping("/board")
    public ResponseEntity<List<ResponseBoardDto>> findBoardByUserId(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(userService.findBoardByUserId(user.getId()));
    }

    @GetMapping("/like")
    public ResponseEntity<List<ResponseLikeDto>> findLikeByUserId(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(userService.findLikeByUserId(user.getId()));
    }

    @PutMapping
    public ResponseEntity<ResponseUserDto> updateUser(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody RequestUserDto requestUserDto) {
        return ResponseEntity.status(OK).body(userService.updateUser(user.getId(), requestUserDto));
    }

    @PutMapping("/image")
    public ResponseEntity<Void> updateUserImage(@AuthenticationPrincipal CustomUserDetails user, @RequestPart MultipartFile image) {
        imageService.updateUserImage(user.getId(), image);
        return ResponseEntity.status(OK).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal CustomUserDetails user) {
        userService.deleteUser(user.getId());
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
