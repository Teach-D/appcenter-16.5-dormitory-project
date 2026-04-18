package com.example.appcenter_project.domain.user.service;

import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.domain.groupOrder.service.GroupOrderQueryService;
import com.example.appcenter_project.domain.roommate.service.RoommateQueryService;
import com.example.appcenter_project.domain.tip.service.TipQueryService;
import com.example.appcenter_project.domain.user.dto.request.RequestTokenDto;
import com.example.appcenter_project.domain.user.dto.request.RequestUserDto;
import com.example.appcenter_project.domain.user.dto.request.RequestUserPushNotification;
import com.example.appcenter_project.domain.user.dto.request.RequestUserRoleDto;
import com.example.appcenter_project.domain.user.dto.request.SignupUser;
import com.example.appcenter_project.domain.user.dto.response.ResponseBoardDto;
import com.example.appcenter_project.domain.user.dto.response.ResponseLoginDto;
import com.example.appcenter_project.domain.user.dto.response.ResponseUserDto;
import com.example.appcenter_project.domain.user.entity.RefreshToken;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.user.repository.RefreshTokenRepository;
import com.example.appcenter_project.domain.user.repository.SchoolLoginRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.security.jwt.JwtTokenProvider;
import com.example.appcenter_project.common.image.enums.ImageType;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.springframework.web.multipart.MultipartFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock PasswordEncoder passwordEncoder;
    @Mock SchoolLoginRepository schoolLoginRepository;
    @Mock GroupOrderQueryService groupOrderQueryService;
    @Mock TipQueryService tipQueryService;
    @Mock RoommateQueryService roommateQueryService;
    @Mock ImageService imageService;
    @Mock FcmMessageService fcmMessageService;

    @InjectMocks UserService userService;

    private User buildMockUser() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getStudentNumber()).thenReturn("202312345");
        when(user.getRole()).thenReturn(Role.ROLE_USER);
        when(user.getPenalty()).thenReturn(0);
        when(user.getTimeTableImage()).thenReturn(null);
        when(user.hasUnreadNotifications()).thenReturn(false);
        when(user.hasRoommateCheckList()).thenReturn(false);
        return user;
    }

    private void stubTokenGeneration() {
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(), any(), any())).thenReturn("refresh-token");
    }

    // ===== saveUser =====

    @Test
    @DisplayName("saveUser - INU 포털 인증 성공 시 회원가입 및 토큰 발급")
    void saveUser_success() {
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(signupUser.getPassword()).thenReturn("password");
        when(schoolLoginRepository.loginCheck("202312345", "password")).thenReturn("Y");
        when(userRepository.existsByStudentNumber("202312345")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        stubTokenGeneration();

        ResponseLoginDto result = userService.saveUser(signupUser);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("saveUser - 이미 가입된 학번이면 기존 유저 반환")
    void saveUser_alreadyRegistered_returnsExistingUser() {
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(signupUser.getPassword()).thenReturn("password");
        when(schoolLoginRepository.loginCheck("202312345", "password")).thenReturn("Y");
        when(userRepository.existsByStudentNumber("202312345")).thenReturn(true);

        User existingUser = buildMockUser();
        when(userRepository.findByStudentNumber("202312345")).thenReturn(Optional.of(existingUser));
        stubTokenGeneration();

        ResponseLoginDto result = userService.saveUser(signupUser);

        assertThat(result).isNotNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveUser - INU 포털 인증 실패 시 예외 발생")
    void saveUser_notINUStudent_throwsException() {
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(signupUser.getPassword()).thenReturn("wrong");
        when(schoolLoginRepository.loginCheck("202312345", "wrong")).thenReturn("N");

        assertThatThrownBy(() -> userService.saveUser(signupUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_NOT_FOUND);
    }

    // ===== saveFreshman =====

    @Test
    @DisplayName("saveFreshman - 신입생 회원가입 성공")
    void saveFreshman_success() {
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(signupUser.getPassword()).thenReturn("password");
        when(userRepository.existsByStudentNumber("202312345")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        stubTokenGeneration();

        ResponseLoginDto result = userService.saveFreshman(signupUser);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("saveFreshman - 이미 가입된 학번이면 예외 발생")
    void saveFreshman_duplicate_throwsException() {
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(userRepository.existsByStudentNumber("202312345")).thenReturn(true);

        assertThatThrownBy(() -> userService.saveFreshman(signupUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ALREADY_REGISTERED_USER);
    }

    // ===== loginFreshman =====

    @Test
    @DisplayName("loginFreshman - 신입생 로그인 성공")
    void loginFreshman_success() {
        User freshmanUser = User.createFreshman("202312345", "encoded");
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(signupUser.getPassword()).thenReturn("password");
        when(userRepository.findByStudentNumber("202312345")).thenReturn(Optional.of(freshmanUser));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        stubTokenGeneration();

        ResponseLoginDto result = userService.loginFreshman(signupUser);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("loginFreshman - 존재하지 않는 학번이면 예외 발생")
    void loginFreshman_userNotFound_throwsException() {
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("999999999");
        when(userRepository.findByStudentNumber("999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loginFreshman(signupUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_NOT_FOUND);
    }

    @Test
    @DisplayName("loginFreshman - 신입생 계정이 아니면 예외 발생")
    void loginFreshman_notFreshman_throwsException() {
        User regularUser = User.createNewUser("202312345", "encoded");
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(userRepository.findByStudentNumber("202312345")).thenReturn(Optional.of(regularUser));

        assertThatThrownBy(() -> userService.loginFreshman(signupUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_NOT_FRESHMAN);
    }

    @Test
    @DisplayName("loginFreshman - 비밀번호 불일치 시 예외 발생")
    void loginFreshman_wrongPassword_throwsException() {
        User freshmanUser = User.createFreshman("202312345", "encoded");
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(signupUser.getPassword()).thenReturn("wrong");
        when(userRepository.findByStudentNumber("202312345")).thenReturn(Optional.of(freshmanUser));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.loginFreshman(signupUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(INVALID_PASSWORD);
    }

    // ===== reissueAccessToken =====

    @Test
    @DisplayName("reissueAccessToken - 유효한 리프레시 토큰으로 재발급 성공")
    void reissueAccessToken_success() {
        RequestTokenDto request = mock(RequestTokenDto.class);
        when(request.getRefreshToken()).thenReturn("Bearer valid-refresh-token");

        User mockUser = buildMockUser();
        RefreshToken refreshTokenEntity = mock(RefreshToken.class);
        when(refreshTokenEntity.getUser()).thenReturn(mockUser);

        when(jwtTokenProvider.validateRefreshToken("valid-refresh-token")).thenReturn(true);
        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshTokenEntity));
        stubTokenGeneration();

        ResponseLoginDto result = userService.reissueAccessToken(request);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).delete(refreshTokenEntity);
    }

    @Test
    @DisplayName("reissueAccessToken - Bearer 접두어 없으면 예외 발생")
    void reissueAccessToken_missingBearer_throwsException() {
        RequestTokenDto request = mock(RequestTokenDto.class);
        when(request.getRefreshToken()).thenReturn("invalid-token-without-bearer");

        assertThatThrownBy(() -> userService.reissueAccessToken(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("reissueAccessToken - null 토큰이면 예외 발생")
    void reissueAccessToken_nullToken_throwsException() {
        RequestTokenDto request = mock(RequestTokenDto.class);
        when(request.getRefreshToken()).thenReturn(null);

        assertThatThrownBy(() -> userService.reissueAccessToken(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("reissueAccessToken - JWT 검증 실패 시 예외 발생")
    void reissueAccessToken_invalidJwt_throwsException() {
        RequestTokenDto request = mock(RequestTokenDto.class);
        when(request.getRefreshToken()).thenReturn("Bearer expired-token");
        when(jwtTokenProvider.validateRefreshToken("expired-token")).thenReturn(false);

        assertThatThrownBy(() -> userService.reissueAccessToken(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("reissueAccessToken - DB에 없는 토큰이면 예외 발생")
    void reissueAccessToken_tokenNotFoundInDb_throwsException() {
        RequestTokenDto request = mock(RequestTokenDto.class);
        when(request.getRefreshToken()).thenReturn("Bearer orphan-token");
        when(jwtTokenProvider.validateRefreshToken("orphan-token")).thenReturn(true);
        when(refreshTokenRepository.findByToken("orphan-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.reissueAccessToken(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(REFRESH_TOKEN_USER_NOT_FOUND);
    }

    // ===== findUser =====

    @Test
    @DisplayName("findUser - 존재하는 사용자 조회 성공")
    void findUser_success() {
        User mockUser = buildMockUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        ResponseUserDto result = userService.findUser(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStudentNumber()).isEqualTo("202312345");
    }

    @Test
    @DisplayName("findUser - 존재하지 않는 사용자 예외 발생")
    void findUser_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUser(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_NOT_FOUND);
    }

    // ===== findAllUsers =====

    @Test
    @DisplayName("findAllUsers - 전체 사용자 목록 조회")
    void findAllUsers_success() {
        User u1 = mock(User.class);
        when(u1.getId()).thenReturn(1L);
        when(u1.getStudentNumber()).thenReturn("202312345");
        User u2 = mock(User.class);
        when(u2.getId()).thenReturn(2L);
        when(u2.getStudentNumber()).thenReturn("202312346");
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<ResponseUserDto> result = userService.findAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ResponseUserDto::getStudentNumber)
                .containsExactly("202312345", "202312346");
    }

    // ===== findUsersDormitoryRoles =====

    @Test
    @DisplayName("findUsersDormitoryRoles - 기숙사 관련 역할 사용자 목록 조회")
    void findUsersDormitoryRoles_success() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getStudentNumber()).thenReturn("202312345");
        when(mockUser.getRole()).thenReturn(Role.ROLE_DORM_MANAGER);
        when(userRepository.findByRoleIn(any())).thenReturn(List.of(mockUser));

        var result = userService.findUsersDormitoryRoles();

        assertThat(result).hasSize(1);
    }

    // ===== findUserBoards =====

    @Test
    @DisplayName("findUserBoards - 사용자 작성 게시글 목록 조회")
    void findUserBoards_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(tipQueryService.findTipsByUser(eq(1L), any())).thenReturn(List.of());
        when(groupOrderQueryService.findGroupOrdersByUser(eq(1L), any())).thenReturn(List.of());

        List<ResponseBoardDto> result = userService.findUserBoards(1L, request);

        assertThat(result).isEmpty();
        verify(tipQueryService).findTipsByUser(eq(1L), any());
        verify(groupOrderQueryService).findGroupOrdersByUser(eq(1L), any());
    }

    // ===== findUserLikedBoards =====

    @Test
    @DisplayName("findUserLikedBoards - 사용자 좋아요 게시글 목록 조회")
    void findUserLikedBoards_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(roommateQueryService.findLikedByUser(1L)).thenReturn(List.of());
        when(tipQueryService.findLikedByUser(eq(1L), any())).thenReturn(List.of());
        when(groupOrderQueryService.findLikedByUser(eq(1L), any())).thenReturn(List.of());

        List<ResponseBoardDto> result = userService.findUserLikedBoards(1L, request);

        assertThat(result).isEmpty();
        verify(roommateQueryService).findLikedByUser(1L);
        verify(tipQueryService).findLikedByUser(eq(1L), any());
        verify(groupOrderQueryService).findLikedByUser(eq(1L), any());
    }

    // ===== sendPushNotification =====

    @Test
    @DisplayName("sendPushNotification - 대상 사용자들에게 푸시 알림 전송")
    void sendPushNotification_success() {
        User mockUser = buildMockUser();
        RequestUserPushNotification request = mock(RequestUserPushNotification.class);
        when(request.getTitle()).thenReturn("공지");
        when(request.getBody()).thenReturn("내용");
        when(request.getUserIds()).thenReturn(List.of(1L));
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(mockUser));

        userService.sendPushNotification(request);

        verify(fcmMessageService).sendNotification(mockUser, "공지", "내용");
    }

    // ===== findUserImage / findUserTimeTableImage =====

    @Test
    @DisplayName("findUserImage - imageService에 위임")
    void findUserImage_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        userService.findUserImage(1L, request);

        verify(imageService).findImage(ImageType.USER, 1L, request);
    }

    @Test
    @DisplayName("findUserTimeTableImage - imageService에 위임")
    void findUserTimeTableImage_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        userService.findUserTimeTableImage(1L, request);

        verify(imageService).findImage(ImageType.TIME_TABLE, 1L, request);
    }

    // ===== updateUserImage / updateUserTimeTableImage =====

    @Test
    @DisplayName("updateUserImage - imageService에 위임")
    void updateUserImage_success() {
        MultipartFile image = mock(MultipartFile.class);

        userService.updateUserImage(1L, image);

        verify(imageService).updateImage(ImageType.USER, 1L, image);
    }

    @Test
    @DisplayName("updateUserTimeTableImage - imageService에 위임")
    void updateUserTimeTableImage_success() {
        MultipartFile image = mock(MultipartFile.class);

        userService.updateUserTimeTableImage(1L, image);

        verify(imageService).updateImage(ImageType.TIME_TABLE, 1L, image);
    }

    // ===== changeUserRole =====

    @Test
    @DisplayName("changeUserRole - 역할 변경 성공")
    void changeUserRole_success() {
        RequestUserRoleDto request = mock(RequestUserRoleDto.class);
        when(request.getRole()).thenReturn("관리자");
        when(request.getStudentNumber()).thenReturn("202312345");

        User mockUser = buildMockUser();
        when(userRepository.findByStudentNumber("202312345")).thenReturn(Optional.of(mockUser));

        userService.changeUserRole(request);

        verify(mockUser).changeRole(Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("changeUserRole - 학번 없으면 예외 발생")
    void changeUserRole_userNotFound_throwsException() {
        RequestUserRoleDto request = mock(RequestUserRoleDto.class);
        when(request.getRole()).thenReturn("관리자");
        when(request.getStudentNumber()).thenReturn("999999999");
        when(userRepository.findByStudentNumber("999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeUserRole(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_NOT_FOUND);
    }

    // ===== convertToPermanent =====

    @Test
    @DisplayName("convertToPermanent - 신입생을 정규 사용자로 전환 성공 (충돌 없음)")
    void convertToPermanent_success_noConflict() {
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(signupUser.getPassword()).thenReturn("password");

        User mockUser = buildMockUser();
        when(mockUser.convertINUUser(any(), any())).thenReturn(mockUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(schoolLoginRepository.loginCheck("202312345", "password")).thenReturn("Y");
        when(userRepository.findByStudentNumber("202312345")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        stubTokenGeneration();

        ResponseLoginDto result = userService.convertToPermanent(1L, signupUser);

        assertThat(result).isNotNull();
        verify(refreshTokenRepository).deleteByUser(mockUser);
        verify(mockUser).convertINUUser("202312345", "encoded");
    }

    @Test
    @DisplayName("convertToPermanent - 학번 충돌 유저(다른 ID)가 있으면 삭제 후 전환")
    void convertToPermanent_conflictingUserDeleted() {
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(signupUser.getPassword()).thenReturn("password");

        User mockUser = buildMockUser();
        when(mockUser.convertINUUser(any(), any())).thenReturn(mockUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(schoolLoginRepository.loginCheck("202312345", "password")).thenReturn("Y");

        User conflictingUser = mock(User.class);
        when(conflictingUser.getId()).thenReturn(99L);
        when(userRepository.findByStudentNumber("202312345")).thenReturn(Optional.of(conflictingUser));

        when(passwordEncoder.encode("password")).thenReturn("encoded");
        stubTokenGeneration();

        userService.convertToPermanent(1L, signupUser);

        verify(refreshTokenRepository).deleteByUser(conflictingUser);
        verify(userRepository).delete(conflictingUser);
    }

    @Test
    @DisplayName("convertToPermanent - 동일 유저가 이미 해당 학번이면 삭제 없이 전환")
    void convertToPermanent_sameUserConflict_noDelete() {
        SignupUser signupUser = mock(SignupUser.class);
        when(signupUser.getStudentNumber()).thenReturn("202312345");
        when(signupUser.getPassword()).thenReturn("password");

        User mockUser = buildMockUser();
        when(mockUser.convertINUUser(any(), any())).thenReturn(mockUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(schoolLoginRepository.loginCheck("202312345", "password")).thenReturn("Y");
        when(userRepository.findByStudentNumber("202312345")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        stubTokenGeneration();

        userService.convertToPermanent(1L, signupUser);

        verify(userRepository, never()).delete(mockUser);
    }

    // ===== updateUser =====

    @Test
    @DisplayName("updateUser - 사용자 정보 수정 성공")
    void updateUser_success() {
        User mockUser = buildMockUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RequestUserDto request = mock(RequestUserDto.class);
        when(request.getName()).thenReturn("홍길동");
        when(request.getDormType()).thenReturn("2기숙사");
        when(request.getCollege()).thenReturn("공과대학");

        ResponseUserDto result = userService.updateUser(1L, request);

        assertThat(result).isNotNull();
        verify(mockUser).update(request);
    }

    // ===== updateUserAgreement =====

    @Test
    @DisplayName("updateUserAgreement - 약관 동의 여부 수정 성공")
    void updateUserAgreement_success() {
        User mockUser = buildMockUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        userService.updateUserAgreement(1L, true, true);

        verify(mockUser).updateTermsAgreed(true);
        verify(mockUser).updatePrivacyAgreed(true);
    }

    // ===== deleteUser =====

    @Test
    @DisplayName("deleteUser - 사용자 삭제 성공")
    void deleteUser_success() {
        User mockUser = buildMockUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        userService.deleteUser(1L);

        verify(refreshTokenRepository).deleteByUser(mockUser);
        verify(userRepository).deleteById(1L);
    }

    // ===== deleteUserTimeTableImage =====

    @Test
    @DisplayName("deleteUserTimeTableImage - 시간표 이미지 삭제 성공")
    void deleteUserTimeTableImage_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUserTimeTableImage(1L);

        verify(imageService).deleteImage(any(), eq(1L));
    }

    @Test
    @DisplayName("deleteUserTimeTableImage - 존재하지 않는 사용자 예외 발생")
    void deleteUserTimeTableImage_userNotFound_throwsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUserTimeTableImage(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_NOT_FOUND);
    }
}
