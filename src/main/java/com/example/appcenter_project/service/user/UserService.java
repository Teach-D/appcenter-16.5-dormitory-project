package com.example.appcenter_project.service.user;

import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.dto.request.user.SignupUser;
import com.example.appcenter_project.dto.response.like.ResponseLikeDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.enums.user.Role;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.like.LikeRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;
    private final AuthenticationManagerBuilder authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public ResponseLoginDto saveUser(SignupUser signupUser) {
        boolean existsByStudentNumber = userRepository.existsByStudentNumber(signupUser.getStudentNumber());

        Image defaultImage = imageRepository.findByImageTypeAndIsDefault(ImageType.USER, true)
                .orElseThrow(() -> new CustomException(DEFAULT_IMAGE_NOT_FOUND));

        // 회원정보가 db에 없는 경우 db에 저장 후 로그인
        if (!existsByStudentNumber) {
            User user = User.builder()
                    .studentNumber(signupUser.getStudentNumber())
                    .image(defaultImage)
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user);
        }

        return login(signupUser);
    }

    public ResponseUserDto findUserByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return ResponseUserDto.entityToDto(user);
    }

    public ResponseUserDto updateUser(Long userId, RequestUserDto requestUserDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        user.update(requestUserDto);
        return ResponseUserDto.entityToDto(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(USER_NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }

    public ResponseLoginDto login(SignupUser signupUser) {
        String studentNumber = signupUser.getStudentNumber();
        log.info("[로그인 시도] loginId: {}", studentNumber);

        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
        user.updateRefreshToken(refreshToken);

        return new ResponseLoginDto(accessToken, refreshToken);
    }

    public List<ResponseLikeDto> findLikeByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(USER_NOT_FOUND);
        }

        List<GroupOrderLike> groupOrderLikeList = likeRepository.findByUser_Id(userId);
        List<ResponseLikeDto> responseLikeDtoList = new ArrayList<>();

        for (GroupOrderLike groupOrderLike : groupOrderLikeList) {
            GroupOrder groupOrder = groupOrderLike.getGroupOrder();

            ResponseLikeDto responseLikeDto = ResponseLikeDto.builder()
                    .title(groupOrder.getTitle())
                    .price(groupOrder.getPrice())
                    .currentPeople(groupOrder.getCurrentPeople())
                    .maxPeople(groupOrder.getMaxPeople())
                    .boardId(groupOrder.getId())
                    .deadline(groupOrder.getDeadline())
                    .build();

            responseLikeDtoList.add(responseLikeDto);
        }

        return responseLikeDtoList;
    }

    public String reissueAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(REFRESH_TOKEN_USER_NOT_FOUND));

        return jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getStudentNumber(),
                String.valueOf(user.getRole())
        );
    }
}