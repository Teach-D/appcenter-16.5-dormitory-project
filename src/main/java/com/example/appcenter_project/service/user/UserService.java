package com.example.appcenter_project.service.user;

import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.dto.request.user.SignupUser;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.dto.response.like.ResponseLikeDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDetailDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.like.RoommateBoardLike;
import com.example.appcenter_project.entity.like.TipLike;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.enums.user.Role;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.mapper.GroupOrderMapper;
import com.example.appcenter_project.mapper.TipMapper;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.like.GroupOrderLikeRepository;
import com.example.appcenter_project.repository.like.RoommateBoardLikeRepository;
import com.example.appcenter_project.repository.like.TipLikeRepository;
import com.example.appcenter_project.repository.user.SchoolLoginRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
    private final RoommateBoardLikeRepository roommateBoardLikeRepository;
    private final TipLikeRepository tipLikeRepository;

    public ResponseLoginDto saveUser(SignupUser signupUser) {
        boolean existsByStudentNumber = userRepository.existsByStudentNumber(signupUser.getStudentNumber());

        Image defaultImage = imageRepository.findAllByImageTypeAndIsDefault(ImageType.USER, true)
                .orElseThrow(() -> new CustomException(DEFAULT_IMAGE_NOT_FOUND));

        // studentNumber가 "admin"으로 시작하지 않는 경우만 DB 저장 로직 실행
        if (!signupUser.getStudentNumber().startsWith("admin")) {
            // 회원정보가 db에 없는 경우 db에 저장 후 로그인
            if (!existsByStudentNumber) {
                User user = User.builder()
                        .studentNumber(signupUser.getStudentNumber())
                        .password(passwordEncoder.encode(signupUser.getPassword())) // null 방지 + 인코딩 필수
                        .penalty(0) // null 방지
                        .image(defaultImage)
                        .role(Role.ROLE_USER)
                        .penalty(0)
                        .build();
                userRepository.save(user);
            }
        }

        return login(signupUser);
    }

    public ResponseUserDto findUserByUserId(Long userId) {
        log.info("[findUserByUserId] 사용자 정보 조회 요청 - userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (user.getRoommateCheckList() == null) {
            log.info("[findUserByUserId] RoommateCheckList 존재 여부: false");

            return ResponseUserDto.entityToDto(user, false);
        }

        return ResponseUserDto.entityToDto(user, true);
    }

    public ResponseUserDto updateUser(Long userId, RequestUserDto requestUserDto) {
        log.info("[updateUser] 사용자 정보 수정 요청 시작 - userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 기존 사용자의 이름과 다른 경우에만 중복 체크
        if (!user.getName().equals(requestUserDto.getName()) &&
                userRepository.existsByName(requestUserDto.getName())) {
            throw new CustomException(DUPLICATE_USER_NAME);
        }

        user.update(requestUserDto);
        log.info("[updateUser] 사용자 정보 업데이트 완료 - userId={}", userId);

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

        // admin으로 시작하지 않는 경우에만 학교 로그인 체크
        if (!studentNumber.startsWith("admin")) {
            String loginCheck = schoolLoginRepository.loginCheck(studentNumber, signupUser.getPassword());

            if (Objects.equals(loginCheck, "N")) {
                throw new CustomException(USER_NOT_FOUND);
            }
        }

        log.info("[로그인 시도] loginId: {}", studentNumber);

        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
        user.updateRefreshToken(refreshToken);

        return new ResponseLoginDto(accessToken, refreshToken);
    }

    public List<ResponseBoardDto> findLikeByUserId(Long userId) {
        log.info("[findLikeByUserId] userId={}의 좋아요 게시물 조회 시작", userId);

        List<ResponseBoardDto> responseBoardDtoList = new ArrayList<>();

//        List<ResponseGroupOrderDto> likeGroupOrders = groupOrderMapper.findLikeGroupOrders(userId);
//        responseBoardDtoList.addAll(likeGroupOrders);

        List<ResponseRoommatePostDto> responseLikeDtoList = new ArrayList<>();
        List<RoommateBoardLike> likeRoommateBoardLikes = roommateBoardLikeRepository.findByUserId(userId);
        for (RoommateBoardLike likeRoommateBoardLike : likeRoommateBoardLikes) {
            RoommateBoard roommateBoard = likeRoommateBoardLike.getRoommateBoard();
            ResponseRoommatePostDto responseRoommatePostDto = ResponseRoommatePostDto.entityToDto(roommateBoard, false,null);
            responseLikeDtoList.add(responseRoommatePostDto);
        }

        List<ResponseTipDto> responseTipDtos = new ArrayList<>();
        List<TipLike> tipLikes = tipLikeRepository.findByUserId(userId);
        for (TipLike tipLike : tipLikes) {
            Tip tip = tipLike.getTip();
            ResponseTipDto responseTipDto = ResponseTipDto.entityToDto(tip);
            responseTipDtos.add(responseTipDto);
        }

        responseBoardDtoList.addAll(responseLikeDtoList);
        responseBoardDtoList.addAll(responseTipDtos);
        log.info("[findLikeByUserId] 총 좋아요 게시물 수: {}", responseBoardDtoList.size());

        // 로그 추가로 디버깅
        log.debug("[findLikeByUserId] 정렬 전 데이터:");
        for (ResponseBoardDto board : responseBoardDtoList) {
            log.debug("  - Type: {}, CreateDate: {}, Title: {}", board.getType(), board.getCreateDate(), board.getTitle());
        }

        // 최신순 정렬 (createTime이 가장 최근인 것부터)
        responseBoardDtoList.sort(Comparator.comparing(ResponseBoardDto::getCreateDate).reversed());

        // 정렬 후 로그
        log.debug("[findLikeByUserId] 정렬 후 데이터:");
        for (ResponseBoardDto board : responseBoardDtoList) {
            log.debug("  - Type: {}, CreateDate: {}, Title: {}", board.getType(), board.getCreateDate(), board.getTitle());
        }

        log.info("[findLikeByUserId] userId={}의 좋아요 게시물 조회 완료", userId);

        return responseBoardDtoList;
    }

    public List<ResponseBoardDto> findBoardByUserId(Long userId) {
        log.info("[findBoardByUserId] userId={}의 작성한 게시물 조회 시작", userId);

        List<ResponseBoardDto> responseBoardDtoList = new ArrayList<>();

        List<ResponseGroupOrderDto> groupOrdersByUserId = groupOrderMapper.findGroupOrdersByUserId(userId);

        List<ResponseTipDto> tipsByUserId = tipMapper.findTipsByUserId(userId);
        log.info("[findBoardByUserId] Tip 게시물 개수: {}", tipsByUserId.size());

        responseBoardDtoList.addAll(groupOrdersByUserId);
        responseBoardDtoList.addAll(tipsByUserId);

        // 최신순 정렬 (createTime이 가장 최근인 것부터)
        responseBoardDtoList.sort(Comparator.comparing(ResponseBoardDto::getCreateDate).reversed());

        log.info("[findBoardByUserId] userId={}의 게시물 조회 완료", userId);

        return responseBoardDtoList;
    }

    public String reissueAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }
        log.info("refreshToken: {}", refreshToken);
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(REFRESH_TOKEN_USER_NOT_FOUND));

        return jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getStudentNumber(),
                String.valueOf(user.getRole())
        );
    }
}