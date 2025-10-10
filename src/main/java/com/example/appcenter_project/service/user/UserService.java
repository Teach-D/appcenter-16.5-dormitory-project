package com.example.appcenter_project.service.user;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.dto.request.user.RequestUserPushNotification;
import com.example.appcenter_project.dto.request.user.RequestUserRole;
import com.example.appcenter_project.dto.request.user.SignupUser;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.dto.response.user.ResponseUserRole;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.enums.user.Role;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.mapper.GroupOrderMapper;
import com.example.appcenter_project.mapper.TipMapper;
import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.like.GroupOrderLikeRepository;
import com.example.appcenter_project.repository.like.RoommateBoardLikeRepository;
import com.example.appcenter_project.repository.like.TipLikeRepository;
import com.example.appcenter_project.repository.user.SchoolLoginRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.jwt.JwtTokenProvider;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import com.example.appcenter_project.service.fcm.FcmTokenService;
import com.example.appcenter_project.service.groupOrder.GroupOrderQueryService;
import com.example.appcenter_project.service.image.ImageService;
import com.example.appcenter_project.service.roommate.RoommateQueryService;
import com.example.appcenter_project.service.tip.TipQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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
    private final GroupOrderRepository groupOrderRepository;
    private final GroupOrderQueryService groupOrderQueryService;
    private final TipQueryService tipQueryService;
    private final RoommateQueryService roommateQueryService;
    private final ImageService imageService;
    private final FcmTokenService fcmTokenService;
    private final FcmMessageService fcmMessageService;

    public ResponseLoginDto saveUser(SignupUser signupUser) {
        boolean existsByStudentNumber = userRepository.existsByStudentNumber(signupUser.getStudentNumber());

        // studentNumber가 "admin"으로 시작하지 않는 경우만 DB 저장 로직 실행
        if (!signupUser.getStudentNumber().startsWith("admin")) {
            // 회원정보가 db에 없는 경우 db에 저장 후 로그인
            if (!existsByStudentNumber) {
                User user = User.builder()
                        .studentNumber(signupUser.getStudentNumber())
                        .password(passwordEncoder.encode(signupUser.getPassword())) // null 방지 + 인코딩 필수
                        .penalty(0) // null 방지
                        .image(null)
                        .role(Role.ROLE_USER)
                        .penalty(0)
                        .build();
                userRepository.save(user);
            }
        }

        return login(signupUser);
    }

    public ResponseUserDto findUserByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        boolean hasUnreadNotifications = false;

        for (UserNotification userNotification : user.getUserNotifications()) {
            // 읽지 않은 알림이 하나라도 있을 때
            if (!userNotification.isRead()) {
                hasUnreadNotifications = true;
            }
        }

        if (user.getRoommateCheckList() == null) {
            log.info("RoommateCheckList 존재 여부: false");

            return ResponseUserDto.entityToDto(user, hasUnreadNotifications, false);
        }

        return ResponseUserDto.entityToDto(user, hasUnreadNotifications, true);
    }

    public ResponseUserDto updateUser(Long userId, RequestUserDto requestUserDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

/*        // 기존 사용자의 이름과 다른 경우에만 중복 체크
        // 기존 사용자의 이름이 null이 아니고, 새로운 이름과 다른 경우에만 중복 체크
        if (user.getName() != null &&
                !user.getName().equals(requestUserDto.getName()) &&
                userRepository.existsByName(requestUserDto.getName())) {
            throw new CustomException(DUPLICATE_USER_NAME);
        }*/

        user.update(requestUserDto);

        for (UserNotification userNotification : user.getUserNotifications()) {
            // 읽지 않은 알림이 하나라도 있을 때
            if (!userNotification.isRead()) {
                return ResponseUserDto.entityToDto(user, true);
            }
        }

        // 모든 알림을 읽었을 때
        return ResponseUserDto.entityToDto(user, false);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(USER_NOT_FOUND);
        }
        
        userRepository.deleteById(userId);
    }

    public ResponseLoginDto login(SignupUser signupUser) {
        String studentNumber = signupUser.getStudentNumber();
/*        // admin으로 시작하지 않는 경우에만 학교 로그인 체크
        if (!studentNumber.startsWith("admin")) {
            String loginCheck = schoolLoginRepository.loginCheck(studentNumber, signupUser.getPassword());

            if (Objects.equals(loginCheck, "N")) {
                throw new CustomException(USER_NOT_FOUND);
            }
        }*/

        log.info("[로그인 시도] loginId: {}", studentNumber);

        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
        user.updateRefreshToken(refreshToken);

        return new ResponseLoginDto(accessToken, refreshToken, user.getRole().toString());
    }

    public List<ResponseBoardDto> findLikeByUserId_optimization(Long userId, HttpServletRequest request) {
        List<ResponseBoardDto> responseBoardDtoList = new ArrayList<>();

        List<ResponseRoommatePostDto> responseLikeDtoList = roommateQueryService.findGroupOrderDtosWithImages(userId);
        List<ResponseTipDto> responseTipLikeDtos = tipQueryService.findTipLikeDtosWithImages(userId, request);
        List<ResponseGroupOrderDto> responseGroupOrderDtos = groupOrderQueryService.findGroupOrderLikeDtosWithImages(userId, request);

        responseBoardDtoList.addAll(responseGroupOrderDtos);
        responseBoardDtoList.addAll(responseLikeDtoList);
        responseBoardDtoList.addAll(responseTipLikeDtos);

        // 최신순 정렬 (createTime이 가장 최근인 것부터)
        responseBoardDtoList.sort(Comparator.comparing(ResponseBoardDto::getCreateDate).reversed());

        return responseBoardDtoList;
    }

    public List<ResponseBoardDto> findBoardByUserId_optimization(Long userId, HttpServletRequest request) {
        List<ResponseBoardDto> responseBoardDtoList = new ArrayList<>();

        List<ResponseTipDto> responseTipDtos = tipQueryService.findTipDtosWithImages(userId, request);

        List<ResponseGroupOrderDto> responseGroupOrderDtos = groupOrderQueryService.findGroupOrderDtosWithImages(userId, request);

        responseBoardDtoList.addAll(responseGroupOrderDtos);
        responseBoardDtoList.addAll(responseTipDtos);

        // 최신순 정렬 (createTime이 가장 최근인 것부터)
        responseBoardDtoList.sort(Comparator.comparing(ResponseBoardDto::getCreateDate).reversed());

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

    public void changeUserRole(RequestUserRole requestUserStudentNumber) {
        User user = userRepository.findByStudentNumber(requestUserStudentNumber.getStudentNumber()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        
        // 유저 권한 변경, 관리자/서포터즈로는 변경 불가능
        Role role = Role.from(requestUserStudentNumber.getRole());
        if (role == Role.ROLE_USER || role == Role.ROLE_DORM_LIFE_MANAGER
                || role == Role.ROLE_DORM_ROOMMATE_MANAGER || role == Role.ROLE_DORM_MANAGER) {
            user.changeRole(role);
        }
    }

    public List<ResponseUserRole> findUserDormitoryRole() {
        List<Role> roles = new ArrayList<>();
        roles.add(Role.ROLE_DORM_LIFE_MANAGER);
        roles.add(Role.ROLE_DORM_ROOMMATE_MANAGER);
        roles.add(Role.ROLE_DORM_MANAGER);

        List<User> users = userRepository.findByRoleIn(roles);

        return users.stream()
                .map(user -> ResponseUserRole.builder()
                        .studentNumber(user.getStudentNumber())
                        .role(user.getRole().getDescription())
                        .build())
                .toList();
    }

    public void updateAgreement(Long userId, boolean isTermsAgreed, boolean isPrivacyAgreed) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        user.updateTermsAgreed(isTermsAgreed);
        user.updatePrivacyAgreed(isPrivacyAgreed);
    }

    public ImageLinkDto findUserImage(Long userId, HttpServletRequest request) {
        return imageService.findImage(ImageType.USER, userId, request);
    }

    public void updateUserImage(Long userId, MultipartFile image) {
        imageService.updateImage(ImageType.USER, userId, image);
    }

    public void updateUserTimeTableImage(Long userId, MultipartFile image) {
        imageService.updateImage(ImageType.TIME_TABLE, userId, image);
    }

    public ImageLinkDto findUserTimeTableImage(Long userId, HttpServletRequest request) {
        return imageService.findImage(ImageType.TIME_TABLE, userId, request);
    }

    public void deleteUserTimeTableImage(Long userId) {
        imageService.deleteImage(ImageType.TIME_TABLE, userId);
    }

    public List<ResponseUserDto> findAllUser() {
        return userRepository.findAll().stream()
                .map(ResponseUserDto::entityToDtoNull)
                .toList();
    }

    public void sendPushNotification(RequestUserPushNotification requestUserPushNotification) {
        User user = userRepository.findById(requestUserPushNotification.getUserId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        fcmMessageService.sendNotification(user, requestUserPushNotification.getTitle(), requestUserPushNotification.getBody());
    }
}