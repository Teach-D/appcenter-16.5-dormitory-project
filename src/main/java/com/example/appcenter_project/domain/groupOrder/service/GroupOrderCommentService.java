package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.groupOrder.dto.request.RequestGroupOrderCommentDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderComment;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.shared.enums.ApiType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderCommentRepository;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupOrderCommentService {

    private final GroupOrderCommentRepository groupOrderCommentRepository;
    private final UserRepository userRepository;
    private final GroupOrderRepository groupOrderRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final FcmMessageService fcmMessageService;
    private final NotificationRepository notificationRepository;

    // ========== Public Methods ========== //

    public ResponseGroupOrderCommentDto saveGroupOrderComment(Long userId, RequestGroupOrderCommentDto requestDto) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        GroupOrder groupOrder = groupOrderRepository.findById(requestDto.getGroupOrderId())
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));

        GroupOrderComment comment;
        if (isParentComment(requestDto)) {
            comment = createParentComment(requestDto, groupOrder, currentUser);
            sendNotificationToGroupOrderWriter(currentUser, groupOrder, requestDto.getReply());
        } else {
            GroupOrderComment parentGroupOrderComment = groupOrderCommentRepository
                    .findById(requestDto.getParentCommentId()).orElseThrow(() -> new CustomException(GROUP_ORDER_COMMENT_NOT_FOUND));
            comment = createChildComment(requestDto, groupOrder, currentUser, parentGroupOrderComment);
            sendNotificationToParentCommentWriter(currentUser, groupOrder, requestDto.getReply(), parentGroupOrderComment);
        }

        return ResponseGroupOrderCommentDto.from(comment);
    }

    public void deleteGroupOrderComment(Long userId, Long groupOrderCommentId) {
        GroupOrderComment groupOrderComment = groupOrderCommentRepository.findByIdAndUserId(groupOrderCommentId, userId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_COMMENT_NOT_OWNED_BY_USER));
        groupOrderComment.updateIsDeleted();
    }

    // ========== Private Methods ========== //

    private GroupOrderComment createParentComment(RequestGroupOrderCommentDto requestDto, GroupOrder groupOrder, User currentUser) {
        GroupOrderComment comment = GroupOrderComment.builder()
                .reply(requestDto.getReply())
                .groupOrder(groupOrder)
                .user(currentUser)
                .parentGroupOrderComment(null)
                .build();

        return groupOrderCommentRepository.save(comment);
    }

    private void sendNotificationToGroupOrderWriter(User currentUser, GroupOrder groupOrder, String reply) {
        User groupOrderWriterUser = groupOrder.getUser();

        if (isDifferentUser(groupOrderWriterUser, currentUser)) {
            String title = "[" + groupOrder.getTitle() + "] 에 댓글이 달렸어요";
            Notification notification = createNotification(title, reply, groupOrder);
            createUserNotification(groupOrderWriterUser, notification);
            sendMessageTo(groupOrderWriterUser, notification);
        }
    }

    private Notification createNotification(String title, String reply, GroupOrder groupOrder) {
        Notification notification = Notification.of(
                title,
                reply,
                NotificationType.GROUP_ORDER,
                ApiType.GROUP_ORDER,
                groupOrder.getId()
        );

        notificationRepository.save(notification);
        return notification;
    }

    private void createUserNotification(User user, Notification notification) {
        UserNotification userNotification = UserNotification.of(user, notification);
        userNotificationRepository.save(userNotification);
    }

    private void sendMessageTo(User parentCommentWriter, Notification notification) {
        fcmMessageService.sendNotification(parentCommentWriter, notification.getTitle(), notification.getBody());
    }

    private GroupOrderComment createChildComment(RequestGroupOrderCommentDto requestDto, GroupOrder groupOrder, User currentUser, GroupOrderComment parentGroupOrderComment) {
        GroupOrderComment groupOrderComment = GroupOrderComment.builder()
                .reply(requestDto.getReply())
                .groupOrder(groupOrder)
                .user(currentUser)
                .parentGroupOrderComment(parentGroupOrderComment)
                .build();
        return groupOrderCommentRepository.save(groupOrderComment);
    }

    private void sendNotificationToParentCommentWriter(User currentUser, GroupOrder groupOrder, String reply, GroupOrderComment parentGroupOrderComment) {
        User parentCommentWriter = parentGroupOrderComment.getUser();

        if (isDifferentUser(parentCommentWriter, currentUser)) {
            String title = "[" + parentGroupOrderComment.getReply() + "]" + " 에 대댓글이 달렸어요";
            Notification notification = createNotification(title, reply, groupOrder);
            createUserNotification(parentCommentWriter, notification);
            sendMessageTo(parentCommentWriter, notification);
        }
    }


    private static boolean isParentComment(RequestGroupOrderCommentDto responseGroupOrderCommentDto) {
        return responseGroupOrderCommentDto.getParentCommentId() == null;
    }

    private static boolean isDifferentUser(User user1, User user2) {
        return !user1.equals(user2);
    }
}
