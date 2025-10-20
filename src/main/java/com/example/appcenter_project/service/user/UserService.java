package com.example.appcenter_project.service.user;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.user.*;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.dto.response.user.ResponseUserRole;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.enums.user.Role;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.user.SchoolLoginRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.jwt.JwtTokenProvider;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import com.example.appcenter_project.service.groupOrder.GroupOrderQueryService;
import com.example.appcenter_project.service.image.ImageService;
import com.example.appcenter_project.service.roommate.RoommateQueryService;
import com.example.appcenter_project.service.tip.TipQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SchoolLoginRepository schoolLoginRepository;
    private final GroupOrderQueryService groupOrderQueryService;
    private final TipQueryService tipQueryService;
    private final RoommateQueryService roommateQueryService;
    private final ImageService imageService;
    private final FcmMessageService fcmMessageService;

    // ========== Public Methods ========== //

    public ResponseLoginDto saveUser(SignupUser signupUser) {
        checkINUStudent(signupUser);
        User user = createUser(signupUser);
        return createUserDto(user);
    }

    public String reissueAccessToken(RequestTokenDto request) {
        validateRefreshToken(request.getRefreshToken());
        String refreshToken = extractBearerToken(request);
        return reissueAccessTokenByRefreshToken(refreshToken);
    }

    public void sendPushNotification(RequestUserPushNotification request) {
        String title = request.getTitle();
        String body = request.getBody();

        List<User> userIds = userRepository.findAllById(request.getUserIds());
        sendNotificationToUsers(userIds, title, body);
    }


    public void changeUserRole(RequestUserRoleDto request) {
        Role role = Role.from(request.getRole());
        if (isNotAdminRole(role)) {
            User user = userRepository.findByStudentNumber(request.getStudentNumber()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
            user.changeRole(role);
        }
    }

    public ResponseUserDto findUser(Long userId) {
        User user = findUserById(userId);

        boolean hasUnreadNotifications = user.hasUnreadNotifications();
        boolean hasRoommateCheckList = user.hasRoommateCheckList();

        return ResponseUserDto.entityToDto(user, hasRoommateCheckList, hasUnreadNotifications);
    }

    public List<ResponseUserDto> findAllUsers() {
        return userRepository.findAll().stream().map(ResponseUserDto::entityToBasicDto).toList();
    }

    public List<ResponseUserRole> findUsersDormitoryRoles() {
        List<Role> roles = getDormitoryRoles();
        List<User> users = userRepository.findByRoleIn(roles);
        return users.stream()
                .map(user -> ResponseUserRole.builder().studentNumber(user.getStudentNumber()).role(user.getRole().getDescription())
                        .build()).toList();
    }

    public List<ResponseBoardDto> findUserBoards(Long userId, HttpServletRequest request) {
        List<ResponseTipDto> tips = tipQueryService.findTipsByUser(userId, request);
        List<ResponseGroupOrderDto> groupOrders = groupOrderQueryService.findGroupOrdersByUser(userId, request);

        return Stream.of(tips, groupOrders)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ResponseBoardDto::getCreateDate).reversed())
                .collect(Collectors.toList());
    }

    public List<ResponseBoardDto> findUserLikedBoards(Long userId, HttpServletRequest request) {
        List<ResponseRoommatePostDto> roommateBoards = roommateQueryService.findLikedByUser(userId);
        List<ResponseTipDto> tips = tipQueryService.findLikedByUser(userId, request);
        List<ResponseGroupOrderDto> groupOrders = groupOrderQueryService.findLikedByUser(userId, request);

        return Stream.of(roommateBoards, tips, groupOrders)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ResponseBoardDto::getCreateDate).reversed())
                .collect(Collectors.toList());
    }

    public ImageLinkDto findUserImage(Long userId, HttpServletRequest request) {
        return imageService.findImage(ImageType.USER, userId, request);
    }

    public ImageLinkDto findUserTimeTableImage(Long userId, HttpServletRequest request) {
        return imageService.findImage(ImageType.TIME_TABLE, userId, request);
    }

    public ResponseUserDto updateUser(Long userId, RequestUserDto request) {
        User user = findUserById(userId);
        user.update(request);

        boolean hasUnreadNotifications = user.hasUnreadNotifications();
        boolean hasRoommateCheckList = user.hasRoommateCheckList();

        return ResponseUserDto.entityToDto(user, hasRoommateCheckList, hasUnreadNotifications);
    }

    public void updateUserAgreement(Long userId, boolean isTermsAgreed, boolean isPrivacyAgreed) {
        User user = findUserById(userId);
        user.updateTermsAgreed(isTermsAgreed);
        user.updatePrivacyAgreed(isPrivacyAgreed);
    }

    public void updateUserImage(Long userId, MultipartFile image) {
        imageService.updateImage(ImageType.USER, userId, image);
    }

    public void updateUserTimeTableImage(Long userId, MultipartFile image) {
        imageService.updateImage(ImageType.TIME_TABLE, userId, image);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(USER_NOT_FOUND);
        }

        userRepository.deleteById(userId);
    }

    public void deleteUserTimeTableImage(Long userId) {
        imageService.deleteImage(ImageType.TIME_TABLE, userId);
    }

    // ========== Private Methods ========== //


    private void checkINUStudent(SignupUser signupUser) {
        if (isNotINUStudent(schoolLoginRepository.loginCheck(signupUser.getStudentNumber(), signupUser.getPassword()))) {
            throw new CustomException(USER_NOT_FOUND);
        }
    }

    private static boolean isNotINUStudent(String loginCheck) {
        return Objects.equals(loginCheck, "N");
    }

    private User createUser(SignupUser signupUser) {
        if (existsUser(signupUser)) {
            return null;
        }

        User user = User.builder()
                .studentNumber(signupUser.getStudentNumber()).password(passwordEncoder.encode(signupUser.getPassword()))
                .penalty(0).image(null).role(Role.ROLE_USER).build();
        userRepository.save(user);
        return user;
    }

    private boolean existsUser(SignupUser signupUser) {
        return userRepository.existsByStudentNumber(signupUser.getStudentNumber());
    }

    private ResponseLoginDto createUserDto(User user) {
        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return new ResponseLoginDto(accessToken, refreshToken, user.getRole().toString());
    }

    private String createAccessToken(User user) {
        return jwtTokenProvider.generateAccessToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
    }

    private String createRefreshToken(User user) {
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
        user.updateRefreshToken(refreshToken);
        return refreshToken;
    }

    private void validateRefreshToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Refresh Token이 유효하지 않습니다.");
        }
    }

    private static String extractBearerToken(RequestTokenDto request) {
        return request.getRefreshToken().substring(7);
    }

    private String reissueAccessTokenByRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new CustomException(REFRESH_TOKEN_USER_NOT_FOUND));
        return jwtTokenProvider.generateAccessToken(user.getId(), user.getStudentNumber(), String.valueOf(user.getRole()));
    }

    private void sendNotificationToUsers(List<User> userIds, String title, String body) {
        userIds.forEach(user ->
                fcmMessageService.sendNotification(user, title, body)
        );
    }

    private static boolean isNotAdminRole(Role role) {
        return role == Role.ROLE_USER || role == Role.ROLE_DORM_LIFE_MANAGER
                || role == Role.ROLE_DORM_ROOMMATE_MANAGER || role == Role.ROLE_DORM_MANAGER
                || role == Role.ROLE_DORM_SUPPORTERS || role == Role.ROLE_DORM_EXPEDITED_COMPLAINT_MANAGER;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private static List<Role> getDormitoryRoles() {
        return List.of(
                Role.ROLE_DORM_LIFE_MANAGER,
                Role.ROLE_DORM_ROOMMATE_MANAGER,
                Role.ROLE_DORM_MANAGER,
                Role.ROLE_DORM_EXPEDITED_COMPLAINT_MANAGER
        );
    }
}