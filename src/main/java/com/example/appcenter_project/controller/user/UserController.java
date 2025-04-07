package com.example.appcenter_project.controller.user;

import com.example.appcenter_project.dto.ImageDto;
import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.dto.request.user.SignupUser;
import com.example.appcenter_project.dto.response.like.ResponseLikeDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.jwt.SecurityUser;
import com.example.appcenter_project.service.image.ImageService;
import com.example.appcenter_project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<ResponseLoginDto> saveUser(@RequestBody SignupUser signupUser) {
        return ResponseEntity.status(CREATED).body(userService.saveUser(signupUser));
    }

    @GetMapping
    public ResponseEntity<ResponseUserDto> findUserByUserId(@AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.status(FOUND).body(userService.findUserByUserId(user.getId()));
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> findUserImageByUserId(@AuthenticationPrincipal SecurityUser user) {
        ImageDto imageDto = imageService.findUserImageByUserId(user.getId());

        return ResponseEntity.status(FOUND)
                .contentType(MediaType.parseMediaType(imageDto.getContentType()))
                .body(imageDto.getResource());
    }

    @GetMapping("/like")
    public ResponseEntity<List<ResponseLikeDto>> findLikeByUserId(@AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.status(FOUND).body(userService.findLikeByUserId(user.getId()));
    }

    @PutMapping
    public ResponseEntity<ResponseUserDto> updateUser(@AuthenticationPrincipal SecurityUser user, @RequestBody RequestUserDto requestUserDto) {
        return ResponseEntity.status(FOUND).body(userService.updateUser(user.getId(), requestUserDto));
    }

    @PutMapping("/image")
    public ResponseEntity<Void> updateUserImage(@AuthenticationPrincipal SecurityUser user, @RequestPart MultipartFile image) {
        imageService.updateUserImage(user.getId(), image);
        return ResponseEntity.status(OK).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal SecurityUser user) {
        userService.deleteUser(user.getId());
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
