package com.example.appcenter_project.service.user;

import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.dto.request.user.SignupUser;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.enums.user.Role;
import com.example.appcenter_project.jwt.JwtTokenProvider;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public ResponseLoginDto saveUser(SignupUser signupUser) {
        Boolean existsByStudentNumber = userRepository.existsByStudentNumber(signupUser.getStudentNumber());
        Image defaultImage = imageRepository.findByImageTypeAndIsDefault(ImageType.USER, true).orElseThrow(RuntimeException::new);

        // 회원정보가 db에 없는 경우 db에 저장 후 로그인
        if (!existsByStudentNumber) {
            User user = User.builder()
                    .studentNumber(signupUser.getStudentNumber())
//                    .password(signupUser.getPassword())
                    .image(defaultImage)
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user);
        }

        return login(signupUser);
    }

    public ResponseUserDto findUserByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return ResponseUserDto.entityToDto(user);
    }

    public ResponseUserDto updateUser(Long userId, RequestUserDto requestUserDto) {
        User user = userRepository.findById(userId).orElseThrow();
        user.update(requestUserDto);

        return ResponseUserDto.entityToDto(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    private ResponseLoginDto login(SignupUser signupUser) {
        User user = userRepository.findByStudentNumber(signupUser.getStudentNumber()).orElseThrow();

        String token = jwtTokenProvider.createToken(user.getStudentNumber(), String.valueOf(user.getRole()));

        return new ResponseLoginDto(token);
    }
}
