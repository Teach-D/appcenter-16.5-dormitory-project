package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseMyRoommateInfoDto;
import com.example.appcenter_project.domain.roommate.entity.MyRoommate;
import com.example.appcenter_project.domain.roommate.enums.RoommateMatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.domain.roommate.repository.MyRoommateRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.College;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MyRoommateMultiSemesterServiceTest {

    @Mock
    MyRoommateRepository myRoommateRepository;

    @Mock
    ImageService imageService;

    @Mock
    RoommateMatchingRepository roommateMatchingRepository;

    @Mock
    RoommateMatchingPeriodResolver periodResolver;

    @InjectMocks
    MyRoommateService myRoommateService;

    @BeforeEach
    void setUp() {
        given(periodResolver.resolveCurrent(any(LocalDate.class)))
                .willReturn(new MatchingPeriod(2026, SemesterType.FIRST, RoommateMatchingStatus.OPEN));
    }

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
    @DisplayName("정상 반환 — AC-11 BR-710 현재 학기 MyRoommate GET /my-roommates/info")
    void should_return_current_semester_myRoommate_info() {
        // given
        MyRoommate myRoommate = buildMockMyRoommate(1L, 2L);
        given(myRoommateRepository.findByUserIdAndYearAndSemester(1L, 2026, SemesterType.FIRST))
                .willReturn(Optional.of(myRoommate));

        User roommateUser = myRoommate.getRoommate();
        User myUser = myRoommate.getUser();
        given(roommateMatchingRepository.findBySenderAndReceiverAndStatus(any(), any(), any()))
                .willReturn(Optional.of(mock(com.example.appcenter_project.domain.roommate.entity.RoommateMatching.class)));

        // when
        ResponseMyRoommateInfoDto result = myRoommateService.getMyRoommateInfo(1L, mock(HttpServletRequest.class));

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("현재 학기 스코프 쿼리 사용 — AC-11 BR-710 전 학기 포함 메서드 미호출")
    void should_use_semester_scoped_query_not_legacy_query_for_myRoommateInfo() {
        // given
        MyRoommate myRoommate = buildMockMyRoommate(1L, 2L);
        given(myRoommateRepository.findByUserIdAndYearAndSemester(1L, 2026, SemesterType.FIRST))
                .willReturn(Optional.of(myRoommate));
        given(roommateMatchingRepository.findBySenderAndReceiverAndStatus(any(), any(), any()))
                .willReturn(Optional.of(mock(com.example.appcenter_project.domain.roommate.entity.RoommateMatching.class)));

        // when
        myRoommateService.getMyRoommateInfo(1L, mock(HttpServletRequest.class));

        // then
        then(myRoommateRepository).should(never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("CustomException 발생 — AC-11 BR-710 현재 학기에 MyRoommate 없으면 404")
    void should_throw_CustomException_when_myRoommate_not_found_in_current_semester() {
        // given
        given(myRoommateRepository.findByUserIdAndYearAndSemester(99L, 2026, SemesterType.FIRST))
                .willReturn(Optional.empty());

        // when
        org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
                () -> myRoommateService.getMyRoommateInfo(99L, mock(HttpServletRequest.class));

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MY_ROOMMATE_NOT_REGISTERED);
    }
}
