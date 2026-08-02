package com.example.appcenter_project.domain.user.service;

import com.example.appcenter_project.domain.openChat.service.OpenChatDormOfficialRoomService;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.domain.user.fixture.UserFixture;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.*;

// NOTE: UserService.updateUser()에서 dormType 변경 시 reassignDormRoom()을 호출하도록
// 수정된 후, 이 테스트는 실제 RequestUserDto와 updateUser() 시그니처로 교체한다.
// 현재 테스트는 OpenChatDormOfficialRoomService 클래스 부재로 인해 컴파일 RED 상태다.
@ExtendWith(MockitoExtension.class)
class UserServiceDormReassignTest {

    @Mock
    UserRepository userRepository;

    // BR-720: UserService에 OpenChatDormOfficialRoomService 의존성이 주입되어야 한다.
    // 구현 전이므로 @InjectMocks가 이 필드를 주입하지 못해 컴파일/런타임에 RED 상태가 된다.
    @Mock
    OpenChatDormOfficialRoomService openChatDormOfficialRoomService;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("reassignDormRoom 호출 — BR-720 updateUser() 호출 시 dormType 변경되면 재배정 메서드 호출")
    void should_call_reassignDormRoom_when_dorm_type_changed() {
        // given
        User user = UserFixture.createUserWithDorm(1L, DormType.DORM_2);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        // BR-720 구현 완료 후 실제 RequestUserDto를 사용하도록 교체한다.
        // 현재는 OpenChatDormOfficialRoomService 클래스 부재로 컴파일 실패(RED).
        userService.reassignDormRoomOnDormTypeChange(1L, DormType.DORM_2, DormType.DORM_1);

        // then
        then(openChatDormOfficialRoomService).should()
                .reassignDormRoom(1L, DormType.DORM_2, DormType.DORM_1);
    }

    @Test
    @DisplayName("reassignDormRoom 미호출 — AC-8 dormType 변경 없으면 재배정 메서드 미호출")
    void should_not_call_reassignDormRoom_when_dorm_type_unchanged() {
        // given
        User user = UserFixture.createUserWithDorm(1L, DormType.DORM_1);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        userService.reassignDormRoomOnDormTypeChange(1L, DormType.DORM_1, DormType.DORM_1);

        // then
        then(openChatDormOfficialRoomService).should(never())
                .reassignDormRoom(any(), any(), any());
    }
}
