package com.example.appcenter_project.service.user;

import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.dto.request.user.SignupUser;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.dto.response.like.ResponseLikeDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDetailDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.like.TipLike;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.enums.user.Role;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.mapper.GroupOrderMapper;
import com.example.appcenter_project.mapper.TipMapper;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.like.GroupOrderLikeRepository;
import com.example.appcenter_project.repository.user.SchoolLoginRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final GroupOrderLikeRepository groupOrderLikeRepository;
    private final AuthenticationManagerBuilder authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SchoolLoginRepository schoolLoginRepository;
    private final GroupOrderMapper groupOrderMapper;
    private final TipMapper tipMapper;

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
                    .penalty(0)
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
        //schoolLoginRepository.loginCheck(signupUser.getStudentNumber(), signupUser.getPassword());
        String studentNumber = signupUser.getStudentNumber();
        log.info("[로그인 시도] loginId: {}", studentNumber);

        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
        user.updateRefreshToken(refreshToken);

        return new ResponseLoginDto(accessToken, refreshToken);
    }

    public List<ResponseBoardDto> findLikeByUserId(Long userId) {
        List<ResponseBoardDto> responseBoardDtoList = new ArrayList<>();

        List<ResponseGroupOrderDto> likeGroupOrders = groupOrderMapper.findLikeGroupOrders(userId);
        List<ResponseTipDto> likeTips = tipMapper.findLikeTips(userId);

        responseBoardDtoList.addAll(likeGroupOrders);
        responseBoardDtoList.addAll(likeTips);

        // 최신순 정렬 (createTime이 가장 최근인 것부터)
        responseBoardDtoList.sort(Comparator.comparing(ResponseBoardDto::getCreateDate).reversed());

        return responseBoardDtoList;
    }

    public List<ResponseBoardDto> findBoardByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<ResponseBoardDto> responseBoardDtoList = new ArrayList<>();

        for (Tip tip : user.getTipList()) {
            ResponseTipDto responseTipDto = ResponseTipDto.entityToDto(tip);
            responseBoardDtoList.add(responseTipDto);
        }

        for (GroupOrder groupOrder : user.getGroupOrderList()) {
            ResponseGroupOrderDto responseTipDto = ResponseGroupOrderDto.entityToDto(groupOrder);
            responseBoardDtoList.add(responseTipDto);
        }

        // 최신순 정렬 (createTime이 가장 최근인 것부터)
        responseBoardDtoList.sort(Comparator.comparing(ResponseBoardDto::getCreateDate).reversed());

        return responseBoardDtoList;
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