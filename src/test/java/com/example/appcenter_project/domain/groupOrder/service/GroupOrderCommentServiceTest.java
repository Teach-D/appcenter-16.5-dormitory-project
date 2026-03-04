package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.domain.groupOrder.dto.request.RequestGroupOrderCommentDto;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderComment;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderCommentRepository;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.entity.User;
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

import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupOrderCommentServiceTest {

    @Mock private GroupOrderCommentRepository groupOrderCommentRepository;
    @Mock private UserRepository userRepository;
    @Mock private GroupOrderRepository groupOrderRepository;
    @Mock private UserNotificationRepository userNotificationRepository;
    @Mock private FcmMessageService fcmMessageService;
    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private GroupOrderCommentService groupOrderCommentService;

    // ===== saveGroupOrderComment - 부모 댓글 =====

    @Test
    @DisplayName("부모 댓글 저장 - 게시글 작성자와 다른 유저면 알림 발송")
    void saveGroupOrderComment_부모댓글_타인_댓글_알림_발송() {
        User writer = mock(User.class);
        User commenter = mock(User.class);
        GroupOrder groupOrder = mock(GroupOrder.class);

        when(writer.getId()).thenReturn(1L);
        when(commenter.getId()).thenReturn(2L);
        when(groupOrder.getId()).thenReturn(1L);
        when(groupOrder.getTitle()).thenReturn("테스트 공동구매");
        when(groupOrder.getUser()).thenReturn(writer);

        RequestGroupOrderCommentDto dto = mock(RequestGroupOrderCommentDto.class);
        when(dto.getGroupOrderId()).thenReturn(1L);
        when(dto.getParentCommentId()).thenReturn(null);
        when(dto.getReply()).thenReturn("댓글 내용");

        when(userRepository.findById(2L)).thenReturn(Optional.of(commenter));
        when(groupOrderRepository.findById(1L)).thenReturn(Optional.of(groupOrder));

        GroupOrderComment savedComment = mock(GroupOrderComment.class);
        when(savedComment.getUser()).thenReturn(commenter);
        when(savedComment.getGroupOrder()).thenReturn(groupOrder);
        when(groupOrderCommentRepository.save(any(GroupOrderComment.class))).thenReturn(savedComment);

        Notification mockNotification = mock(Notification.class);
        when(mockNotification.getTitle()).thenReturn("알림 제목");
        when(mockNotification.getBody()).thenReturn("알림 본문");
        when(notificationRepository.save(any())).thenReturn(mockNotification);

        groupOrderCommentService.saveGroupOrderComment(2L, dto);

        verify(groupOrderCommentRepository).save(any(GroupOrderComment.class));
        verify(notificationRepository).save(any());
        verify(userNotificationRepository).save(any());
        verify(fcmMessageService).sendNotification(eq(writer), any(), any());
    }

    @Test
    @DisplayName("부모 댓글 저장 - 자신의 게시글에 댓글은 알림 미발송")
    void saveGroupOrderComment_부모댓글_자신_게시글_알림_없음() {
        // writer == commenter (같은 유저)
        User writer = mock(User.class);
        when(writer.getId()).thenReturn(1L);

        GroupOrder groupOrder = mock(GroupOrder.class);
        when(groupOrder.getId()).thenReturn(1L);
        when(groupOrder.getTitle()).thenReturn("내 공동구매");
        when(groupOrder.getUser()).thenReturn(writer);

        RequestGroupOrderCommentDto dto = mock(RequestGroupOrderCommentDto.class);
        when(dto.getGroupOrderId()).thenReturn(1L);
        when(dto.getParentCommentId()).thenReturn(null);
        when(dto.getReply()).thenReturn("내 댓글");

        when(userRepository.findById(1L)).thenReturn(Optional.of(writer));
        when(groupOrderRepository.findById(1L)).thenReturn(Optional.of(groupOrder));

        GroupOrderComment savedComment = mock(GroupOrderComment.class);
        when(savedComment.getUser()).thenReturn(writer);
        when(savedComment.getGroupOrder()).thenReturn(groupOrder);
        when(groupOrderCommentRepository.save(any())).thenReturn(savedComment);

        groupOrderCommentService.saveGroupOrderComment(1L, dto);

        // 같은 유저이므로 알림 없음
        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(fcmMessageService);
    }

    // ===== saveGroupOrderComment - 자식 댓글 =====

    @Test
    @DisplayName("자식 댓글 저장 - 부모 댓글 작성자에게 알림 발송")
    void saveGroupOrderComment_자식댓글_부모_댓글_작성자에게_알림() {
        User writer = mock(User.class);
        User commenter = mock(User.class);
        GroupOrder groupOrder = mock(GroupOrder.class);
        GroupOrderComment parentComment = mock(GroupOrderComment.class);

        when(writer.getId()).thenReturn(1L);
        when(commenter.getId()).thenReturn(2L);
        when(groupOrder.getId()).thenReturn(1L);
        when(groupOrder.getTitle()).thenReturn("테스트 공동구매");
        when(groupOrder.getUser()).thenReturn(writer);
        when(parentComment.getUser()).thenReturn(writer);
        when(parentComment.getReply()).thenReturn("부모 댓글");

        RequestGroupOrderCommentDto childDto = mock(RequestGroupOrderCommentDto.class);
        when(childDto.getGroupOrderId()).thenReturn(1L);
        when(childDto.getParentCommentId()).thenReturn(10L);
        when(childDto.getReply()).thenReturn("대댓글 내용");

        when(userRepository.findById(2L)).thenReturn(Optional.of(commenter));
        when(groupOrderRepository.findById(1L)).thenReturn(Optional.of(groupOrder));
        when(groupOrderCommentRepository.findById(10L)).thenReturn(Optional.of(parentComment));

        GroupOrderComment savedChild = mock(GroupOrderComment.class);
        when(savedChild.getUser()).thenReturn(commenter);
        when(savedChild.getGroupOrder()).thenReturn(groupOrder);
        when(groupOrderCommentRepository.save(any())).thenReturn(savedChild);

        Notification mockNotification = mock(Notification.class);
        when(mockNotification.getTitle()).thenReturn("대댓글 알림");
        when(mockNotification.getBody()).thenReturn("대댓글 내용");
        when(notificationRepository.save(any())).thenReturn(mockNotification);

        groupOrderCommentService.saveGroupOrderComment(2L, childDto);

        verify(groupOrderCommentRepository).save(any(GroupOrderComment.class));
        verify(fcmMessageService).sendNotification(eq(writer), any(), any());
    }

    @Test
    @DisplayName("자식 댓글 저장 - 부모 댓글 없으면 예외")
    void saveGroupOrderComment_부모댓글_없으면_예외() {
        User commenter = mock(User.class);
        GroupOrder groupOrder = mock(GroupOrder.class);
        when(groupOrder.getId()).thenReturn(1L);
        when(groupOrder.getUser()).thenReturn(commenter);

        RequestGroupOrderCommentDto childDto = mock(RequestGroupOrderCommentDto.class);
        when(childDto.getGroupOrderId()).thenReturn(1L);
        when(childDto.getParentCommentId()).thenReturn(99L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(commenter));
        when(groupOrderRepository.findById(1L)).thenReturn(Optional.of(groupOrder));
        when(groupOrderCommentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderCommentService.saveGroupOrderComment(2L, childDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GROUP_ORDER_COMMENT_NOT_FOUND);
    }

    // ===== 예외 케이스 =====

    @Test
    @DisplayName("댓글 저장 - 유저 없으면 예외")
    void saveGroupOrderComment_유저_없으면_예외() {
        RequestGroupOrderCommentDto dto = mock(RequestGroupOrderCommentDto.class);
        when(dto.getGroupOrderId()).thenReturn(1L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderCommentService.saveGroupOrderComment(99L, dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 저장 - 공동구매 없으면 예외")
    void saveGroupOrderComment_공동구매_없으면_예외() {
        User user = mock(User.class);
        RequestGroupOrderCommentDto dto = mock(RequestGroupOrderCommentDto.class);
        when(dto.getGroupOrderId()).thenReturn(99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderCommentService.saveGroupOrderComment(1L, dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GROUP_ORDER_NOT_FOUND);
    }

    // ===== deleteGroupOrderComment =====

    @Test
    @DisplayName("댓글 삭제 - 소프트 삭제 처리")
    void deleteGroupOrderComment_소프트_삭제() {
        GroupOrderComment comment = mock(GroupOrderComment.class);
        when(groupOrderCommentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(comment));

        groupOrderCommentService.deleteGroupOrderComment(1L, 1L);

        verify(comment).updateIsDeleted();
    }

    @Test
    @DisplayName("댓글 삭제 - 본인 댓글 아니면 예외")
    void deleteGroupOrderComment_본인_댓글_아니면_예외() {
        when(groupOrderCommentRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderCommentService.deleteGroupOrderComment(2L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GROUP_ORDER_COMMENT_NOT_OWNED_BY_USER);
    }
}
