package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.common.image.dto.ImageLinkDto;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseMyRoommateInfoDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRuleDto;
import com.example.appcenter_project.domain.roommate.entity.MyRoommate;
import com.example.appcenter_project.domain.roommate.entity.RoommateMatching;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import com.example.appcenter_project.domain.roommate.repository.MyRoommateRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.College;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MyRoommateServiceTest {

    @Mock
    MyRoommateRepository myRoommateRepository;

    @Mock
    ImageService imageService;

    @Mock
    RoommateMatchingRepository roommateMatchingRepository;

    @InjectMocks
    MyRoommateService myRoommateService;

    private User buildMockUser(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn("사용자" + id);
        when(user.getDormType()).thenReturn(DormType.DORM_1);
        when(user.getCollege()).thenReturn(College.ENGINEERING);
        return user;
    }

    private MyRoommate buildMockMyRoommate(Long userId, Long roommateId) {
        MyRoommate myRoommate = mock(MyRoommate.class);
        User user = buildMockUser(userId);
        User roommateUser = buildMockUser(roommateId);
        when(myRoommate.getUser()).thenReturn(user);
        when(myRoommate.getRoommate()).thenReturn(roommateUser);
        return myRoommate;
    }

    @Test
    @DisplayName("내 룸메이트 정보 조회 - 정상 반환")
    void getMyRoommateInfo_정상_반환() {
        MyRoommate myRoommate = buildMockMyRoommate(1L, 2L);
        when(myRoommateRepository.findByUserId(1L)).thenReturn(Optional.of(myRoommate));

        User roommateUser = myRoommate.getRoommate();
        User myUser = myRoommate.getUser();

        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getId()).thenReturn(10L);
        when(roommateMatchingRepository.findBySenderAndReceiverAndStatus(myUser, roommateUser, MatchingStatus.COMPLETED))
                .thenReturn(Optional.of(matching));

        ImageLinkDto mockImageLink = mock(ImageLinkDto.class);
        when(mockImageLink.getImageUrl()).thenReturn("http://example.com/image.jpg");
        when(imageService.findImage(eq(ImageType.USER), anyLong(), any(HttpServletRequest.class)))
                .thenReturn(mockImageLink);

        ResponseMyRoommateInfoDto result = myRoommateService.getMyRoommateInfo(1L, mock(HttpServletRequest.class));

        assertThat(result).isNotNull();
        assertThat(result.getMatchingId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("내 룸메이트 정보 조회 - 룸메이트 없으면 예외")
    void getMyRoommateInfo_룸메이트없으면_예외() {
        when(myRoommateRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> myRoommateService.getMyRoommateInfo(99L, mock(HttpServletRequest.class)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", MY_ROOMMATE_NOT_REGISTERED);
    }

    @Test
    @DisplayName("룸메이트 규칙 생성 - 정상 생성")
    void createRule_정상_생성() {
        MyRoommate myRoommate = buildMockMyRoommate(1L, 2L);
        when(myRoommateRepository.findByUserId(1L)).thenReturn(Optional.of(myRoommate));

        MyRoommate roommateMyRoommate = buildMockMyRoommate(2L, 1L);
        when(myRoommateRepository.findByUserId(2L)).thenReturn(Optional.of(roommateMyRoommate));

        List<String> rules = List.of("소등 23시", "청결 유지");

        myRoommateService.createRule(1L, rules);

        verify(myRoommate).initRule();
        verify(roommateMyRoommate).initRule();
        verify(myRoommate).updateRules(rules);
        verify(roommateMyRoommate).updateRules(rules);
    }

    @Test
    @DisplayName("룸메이트 규칙 생성 - 룸메이트 없으면 예외")
    void createRule_룸메이트없으면_예외() {
        when(myRoommateRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> myRoommateService.createRule(99L, List.of("소등 23시")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", MY_ROOMMATE_NOT_REGISTERED);
    }

    @Test
    @DisplayName("룸메이트 규칙 조회 - 정상 반환")
    void getRules_정상_반환() {
        MyRoommate myRoommate = buildMockMyRoommate(1L, 2L);
        when(myRoommate.getRule()).thenReturn(List.of("소등 23시", "청결 유지"));
        when(myRoommateRepository.findByUserId(1L)).thenReturn(Optional.of(myRoommate));

        ResponseRuleDto result = myRoommateService.getRules(1L);

        assertThat(result).isNotNull();
        assertThat(result.getRules()).containsExactly("소등 23시", "청결 유지");
    }

    @Test
    @DisplayName("룸메이트 규칙 조회 - 룸메이트 없으면 예외")
    void getRules_룸메이트없으면_예외() {
        when(myRoommateRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> myRoommateService.getRules(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", MY_ROOMMATE_NOT_REGISTERED);
    }

    @Test
    @DisplayName("룸메이트 규칙 수정 - 정상 수정")
    void updateRules_정상_수정() {
        MyRoommate myRoommate = buildMockMyRoommate(1L, 2L);
        when(myRoommate.getRule()).thenReturn(List.of("기존 규칙")); // 기존 규칙 존재
        when(myRoommateRepository.findByUserId(1L)).thenReturn(Optional.of(myRoommate));

        MyRoommate roommateMyRoommate = buildMockMyRoommate(2L, 1L);
        when(myRoommateRepository.findByUserId(2L)).thenReturn(Optional.of(roommateMyRoommate));

        List<String> newRules = List.of("소등 22시", "방 청소 주 2회");

        myRoommateService.updateRules(1L, newRules);

        verify(myRoommate).updateRules(newRules);
        verify(roommateMyRoommate).updateRules(newRules);
    }

    @Test
    @DisplayName("룸메이트 규칙 수정 - 기존 규칙 없으면 예외")
    void updateRules_규칙없으면_예외() {
        MyRoommate myRoommate = buildMockMyRoommate(1L, 2L);
        when(myRoommate.getRule()).thenReturn(null); // 규칙 없음
        when(myRoommateRepository.findByUserId(1L)).thenReturn(Optional.of(myRoommate));

        assertThatThrownBy(() -> myRoommateService.updateRules(1L, List.of("새 규칙")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", RULE_NOT_FOUND);
    }

    @Test
    @DisplayName("룸메이트 규칙 삭제 - 정상 삭제")
    void deleteRule_정상_삭제() {
        MyRoommate myRoommate = buildMockMyRoommate(1L, 2L);
        when(myRoommateRepository.findByUserId(1L)).thenReturn(Optional.of(myRoommate));

        MyRoommate roommateMyRoommate = buildMockMyRoommate(2L, 1L);
        when(myRoommateRepository.findByUserId(2L)).thenReturn(Optional.of(roommateMyRoommate));

        myRoommateService.deleteRule(1L);

        verify(myRoommate).updateRules(null);
        verify(roommateMyRoommate).updateRules(null);
    }

    @Test
    @DisplayName("룸메이트 규칙 삭제 - 룸메이트 없으면 예외")
    void deleteRule_룸메이트없으면_예외() {
        when(myRoommateRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> myRoommateService.deleteRule(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", MY_ROOMMATE_NOT_REGISTERED);
    }

    @Test
    @DisplayName("룸메이트 이미지 조회 - 정상 반환")
    void findMyRoommateImageByUserId_정상_반환() {
        MyRoommate myRoommate = buildMockMyRoommate(1L, 2L);
        when(myRoommateRepository.findByUserId(1L)).thenReturn(Optional.of(myRoommate));

        ImageLinkDto mockImageLink = mock(ImageLinkDto.class);
        when(imageService.findImage(eq(ImageType.TIME_TABLE), anyLong(), any(HttpServletRequest.class)))
                .thenReturn(mockImageLink);

        ImageLinkDto result = myRoommateService.findMyRoommateImageByUserId(1L, mock(HttpServletRequest.class));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("룸메이트 이미지 조회 - 룸메이트 없으면 예외")
    void findMyRoommateImageByUserId_룸메이트없으면_예외() {
        when(myRoommateRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                myRoommateService.findMyRoommateImageByUserId(99L, mock(HttpServletRequest.class)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", MY_ROOMMATE_NOT_REGISTERED);
    }
}