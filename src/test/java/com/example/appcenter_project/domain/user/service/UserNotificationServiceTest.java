package com.example.appcenter_project.domain.user.service;

import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.dto.response.ResponseUserNotificationDto;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserNotificationServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserNotificationRepository userNotificationRepository;

    @InjectMocks UserNotificationService userNotificationService;

    private User createTestUser() {
        return User.createNewUser("202312345", "encoded");
    }

    // ===== addReceiveNotificationType =====

    @Test
    @DisplayName("addReceiveNotificationType - 미등록 알림 유형 추가 성공")
    void addReceiveNotificationType_success() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userNotificationService.addReceiveNotificationType(1L, List.of("COUPON"));

        assertThat(user.getReceiveNotificationTypes()).contains(NotificationType.COUPON);
    }

    @Test
    @DisplayName("addReceiveNotificationType - 이미 있는 유형은 중복 추가되지 않음")
    void addReceiveNotificationType_alreadyExists_notDuplicated() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        int before = user.getReceiveNotificationTypes().size();

        userNotificationService.addReceiveNotificationType(1L, List.of("ROOMMATE"));

        assertThat(user.getReceiveNotificationTypes()).hasSize(before);
    }

    // ===== addGroupOrderKeyword =====

    @Test
    @DisplayName("addGroupOrderKeyword - 키워드 추가 성공")
    void addGroupOrderKeyword_success() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userNotificationService.addGroupOrderKeyword(1L, "치킨");

        assertThat(user.getGroupOrderKeywords()).contains("치킨");
    }

    @Test
    @DisplayName("addGroupOrderKeyword - 중복 키워드 추가 시 예외 발생")
    void addGroupOrderKeyword_duplicate_throwsException() {
        User user = createTestUser();
        user.getGroupOrderKeywords().add("치킨");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userNotificationService.addGroupOrderKeyword(1L, "치킨"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_GROUP_ORDER_KEYWORD_ALREADY_EXISTS);
    }

    // ===== addGroupOrderCategory =====

    @Test
    @DisplayName("addGroupOrderCategory - 카테고리 추가 성공")
    void addGroupOrderCategory_success() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userNotificationService.addGroupOrderCategory(1L, GroupOrderType.DELIVERY);

        assertThat(user.getGroupOrderTypes()).contains(GroupOrderType.DELIVERY);
    }

    @Test
    @DisplayName("addGroupOrderCategory - 중복 카테고리 추가 시 예외 발생")
    void addGroupOrderCategory_duplicate_throwsException() {
        User user = createTestUser();
        user.getGroupOrderTypes().add(GroupOrderType.DELIVERY);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userNotificationService.addGroupOrderCategory(1L, GroupOrderType.DELIVERY))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_GROUP_ORDER_CATEGORY_ALREADY_EXISTS);
    }

    // ===== findReceiveNotificationType =====

    @Test
    @DisplayName("findReceiveNotificationType - 초기 알림 수신 유형 조회 성공")
    void findReceiveNotificationType_success() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseUserNotificationDto result = userNotificationService.findReceiveNotificationType(1L);

        assertThat(result).isNotNull();
        assertThat(result.isRoommateNotification()).isTrue();
        assertThat(result.isGroupOrderNotification()).isTrue();
        assertThat(result.isDormitoryNotification()).isTrue();
        assertThat(result.isUnidormNotification()).isTrue();
        assertThat(result.isSupportersNotification()).isTrue();
        assertThat(result.isComplaintNotification()).isTrue();
    }

    // ===== updateGroupOrderKeyword =====

    @Test
    @DisplayName("updateGroupOrderKeyword - 존재하는 키워드를 새 키워드로 수정 성공")
    void updateGroupOrderKeyword_success() {
        User user = createTestUser();
        user.getGroupOrderKeywords().add("치킨");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userNotificationService.updateGroupOrderKeyword(1L, "치킨", "피자");

        assertThat(user.getGroupOrderKeywords()).contains("피자");
        assertThat(user.getGroupOrderKeywords()).doesNotContain("치킨");
    }

    @Test
    @DisplayName("updateGroupOrderKeyword - afterKeyword 중복이면 예외 발생")
    void updateGroupOrderKeyword_duplicateAfter_throwsException() {
        User user = createTestUser();
        user.getGroupOrderKeywords().add("치킨");
        user.getGroupOrderKeywords().add("피자");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userNotificationService.updateGroupOrderKeyword(1L, "치킨", "피자"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_GROUP_ORDER_KEYWORD_ALREADY_EXISTS);
    }

    // ===== deleteReceiveNotificationType =====

    @Test
    @DisplayName("deleteReceiveNotificationType - 알림 수신 유형 삭제 성공")
    void deleteReceiveNotificationType_success() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userNotificationService.deleteReceiveNotificationType(1L, List.of("ROOMMATE"));

        assertThat(user.getReceiveNotificationTypes()).doesNotContain(NotificationType.ROOMMATE);
    }

    @Test
    @DisplayName("deleteReceiveNotificationType - 없는 유형은 조용히 무시됨")
    void deleteReceiveNotificationType_notExists_ignored() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        int before = user.getReceiveNotificationTypes().size();

        userNotificationService.deleteReceiveNotificationType(1L, List.of("COUPON"));

        assertThat(user.getReceiveNotificationTypes()).hasSize(before);
    }

    // ===== findUserGroupOrderKeyword =====

    @Test
    @DisplayName("findUserGroupOrderKeyword - 키워드 목록 조회 성공")
    void findUserGroupOrderKeyword_success() {
        User user = createTestUser();
        user.getGroupOrderKeywords().add("치킨");
        user.getGroupOrderKeywords().add("피자");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<String> result = userNotificationService.findUserGroupOrderKeyword(1L);

        assertThat(result).containsExactlyInAnyOrder("치킨", "피자");
    }

    @Test
    @DisplayName("findUserGroupOrderKeyword - 사용자 없으면 예외 발생")
    void findUserGroupOrderKeyword_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userNotificationService.findUserGroupOrderKeyword(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_NOT_FOUND);
    }

    // ===== findUserGroupOrderCategory =====

    @Test
    @DisplayName("findUserGroupOrderCategory - 카테고리 목록 조회 성공")
    void findUserGroupOrderCategory_success() {
        User user = createTestUser();
        user.getGroupOrderTypes().add(GroupOrderType.DELIVERY);
        user.getGroupOrderTypes().add(GroupOrderType.GROCERY);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<String> result = userNotificationService.findUserGroupOrderCategory(1L);

        assertThat(result).containsExactlyInAnyOrder("배달", "식자재");
    }

    @Test
    @DisplayName("findUserGroupOrderCategory - 사용자 없으면 예외 발생")
    void findUserGroupOrderCategory_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userNotificationService.findUserGroupOrderCategory(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_NOT_FOUND);
    }

    // ===== updateGroupOrderCategory =====

    @Test
    @DisplayName("updateGroupOrderCategory - 카테고리 수정 성공")
    void updateGroupOrderCategory_success() {
        User user = createTestUser();
        user.getGroupOrderTypes().add(GroupOrderType.DELIVERY);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userNotificationService.updateGroupOrderCategory(1L, GroupOrderType.DELIVERY, GroupOrderType.GROCERY);

        assertThat(user.getGroupOrderTypes()).contains(GroupOrderType.GROCERY);
        assertThat(user.getGroupOrderTypes()).doesNotContain(GroupOrderType.DELIVERY);
    }

    @Test
    @DisplayName("updateGroupOrderCategory - afterCategory 중복이면 예외 발생")
    void updateGroupOrderCategory_duplicateAfter_throwsException() {
        User user = createTestUser();
        user.getGroupOrderTypes().add(GroupOrderType.DELIVERY);
        user.getGroupOrderTypes().add(GroupOrderType.GROCERY);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userNotificationService.updateGroupOrderCategory(1L, GroupOrderType.DELIVERY, GroupOrderType.GROCERY))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_GROUP_ORDER_CATEGORY_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("updateGroupOrderCategory - beforeCategory 없으면 예외 발생")
    void updateGroupOrderCategory_notFound_throwsException() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userNotificationService.updateGroupOrderCategory(1L, GroupOrderType.DELIVERY, GroupOrderType.GROCERY))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_GROUP_ORDER_TYPE_NOT_FOUND);
    }

    // ===== updateGroupOrderKeyword - keyword not found branch =====

    @Test
    @DisplayName("updateGroupOrderKeyword - beforeKeyword 없으면 예외 발생")
    void updateGroupOrderKeyword_notFound_throwsException() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userNotificationService.updateGroupOrderKeyword(1L, "없는키워드", "새키워드"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_GROUP_ORDER_KEYWORD_NOT_FOUND);
    }

    // ===== deleteUserGroupOrderKeyword =====

    @Test
    @DisplayName("deleteUserGroupOrderKeyword - 키워드 삭제 성공")
    void deleteUserGroupOrderKeyword_success() {
        User user = createTestUser();
        user.getGroupOrderKeywords().add("치킨");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userNotificationService.deleteUserGroupOrderKeyword(1L, "치킨");

        assertThat(user.getGroupOrderKeywords()).doesNotContain("치킨");
    }

    // ===== deleteUserGroupOrderCategory =====

    @Test
    @DisplayName("deleteUserGroupOrderCategory - 카테고리 삭제 성공")
    void deleteUserGroupOrderCategory_success() {
        User user = createTestUser();
        user.getGroupOrderTypes().add(GroupOrderType.DELIVERY);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userNotificationService.deleteUserGroupOrderCategory(1L, GroupOrderType.DELIVERY);

        assertThat(user.getGroupOrderTypes()).doesNotContain(GroupOrderType.DELIVERY);
    }

    // ===== deleteUserNotification =====

    @Test
    @DisplayName("deleteUserNotification - 알림 삭제 성공")
    void deleteUserNotification_success() {
        UserNotification notification = mock(UserNotification.class);
        when(userNotificationRepository.findByUserIdAndNotificationId(1L, 10L))
                .thenReturn(Optional.of(notification));

        userNotificationService.deleteUserNotification(1L, 10L);

        verify(userNotificationRepository).delete(notification);
    }

    @Test
    @DisplayName("deleteUserNotification - 알림 없으면 예외 발생")
    void deleteUserNotification_notFound_throwsException() {
        when(userNotificationRepository.findByUserIdAndNotificationId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userNotificationService.deleteUserNotification(1L, 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(USER_NOTIFICATION_NOT_FOUND);
    }
}
