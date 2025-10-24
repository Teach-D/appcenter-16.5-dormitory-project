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

import java.util.ArrayList;
import java.util.List;

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

    public ResponseGroupOrderCommentDto saveGroupOrderComment(Long userId, RequestGroupOrderCommentDto responseGroupOrderCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        GroupOrder groupOrder = groupOrderRepository.findById(responseGroupOrderCommentDto.getGroupOrderId())
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));

        String title = "[" + groupOrder.getTitle() + "]" + " 에 댓글이 달렸어요";

        if (groupOrder.getUser() != user) {
            Notification notification = Notification.builder()
                    .boardId(groupOrder.getId())
                    .title(title)
                    .body(responseGroupOrderCommentDto.getReply())
                    .notificationType(NotificationType.GROUP_ORDER)
                    .apiType(ApiType.GROUP_ORDER)
                    .build();

            notificationRepository.save(notification);

            UserNotification userNotification = UserNotification.of(groupOrder.getUser(), notification);
            userNotificationRepository.save(userNotification);

            fcmMessageService.sendNotification(groupOrder.getUser(), notification.getTitle(), notification.getBody());

        }


        GroupOrderComment groupOrderComment;
        // 부모 댓글이 없을 때
        if (responseGroupOrderCommentDto.getParentCommentId() == null) {
            groupOrderComment = GroupOrderComment.builder()
                    .reply(responseGroupOrderCommentDto.getReply())
                    .groupOrder(groupOrder)
                    .user(user)
                    .build();

            // 부모 댓글이 없으므로 자신이 부모 댓글이 된다.
            groupOrderComment.setParentGroupOrderCommentNull();
        }
        // 부모 댓글이 있을 때
        else {
            GroupOrderComment parentGroupOrderComment = groupOrderCommentRepository
                    .findById(responseGroupOrderCommentDto.getParentCommentId()).orElseThrow(() -> new CustomException(GROUP_ORDER_COMMENT_NOT_FOUND));
            groupOrderComment = GroupOrderComment.builder()
                    .reply(responseGroupOrderCommentDto.getReply())
                    .groupOrder(groupOrder)
                    .user(user)
                    .parentGroupOrderComment(parentGroupOrderComment)
                    .build();
            parentGroupOrderComment.addChildGroupOrderComments(groupOrderComment);

            String commentTitle = "[" + parentGroupOrderComment.getReply() + "]" + " 에 대댓글이 달렸어요";

            if (parentGroupOrderComment.getUser() != user) {
                Notification commentNotification = Notification.builder()
                        .boardId(groupOrder.getId())
                        .title(commentTitle)
                        .body(groupOrderComment.getReply())
                        .notificationType(NotificationType.GROUP_ORDER)
                        .apiType(ApiType.GROUP_ORDER)
                        .build();

                notificationRepository.save(commentNotification);

                UserNotification commentUserNotification = UserNotification.of(parentGroupOrderComment.getUser(), commentNotification);
                userNotificationRepository.save(commentUserNotification);

                fcmMessageService.sendNotification(parentGroupOrderComment.getUser(), commentNotification.getTitle(), commentNotification.getBody());

            }

         }

        // 양방향 매핑
        groupOrder.getGroupOrderCommentList().add(groupOrderComment);

        groupOrderCommentRepository.save(groupOrderComment);
        return ResponseGroupOrderCommentDto.from(groupOrderComment);
    }

    public List<ResponseGroupOrderCommentDto> findGroupOrderComment(Long userId, Long groupOrderId) {
        List<ResponseGroupOrderCommentDto> responseGroupOrderCommentDtoList = new ArrayList<>();
        List<GroupOrderComment> groupOrderCommentList = groupOrderCommentRepository.findByGroupOrder_IdAndParentGroupOrderCommentIsNull(groupOrderId);
        for (GroupOrderComment groupOrderComment : groupOrderCommentList) {
            List<ResponseGroupOrderCommentDto> childResponseComments = new ArrayList<>();
            List<GroupOrderComment> childGroupOrderComments = groupOrderComment.getChildGroupOrderComments();
            for (GroupOrderComment childGroupOrderComment : childGroupOrderComments) {
                ResponseGroupOrderCommentDto responseGroupOrderCommentDto = ResponseGroupOrderCommentDto.builder()
                        .groupOrderCommentId(childGroupOrderComment.getId())
                        .userId(childGroupOrderComment.getUser().getId())
                        .reply(childGroupOrderComment.getReply())
                        .build();
                childResponseComments.add(responseGroupOrderCommentDto);
            }
            ResponseGroupOrderCommentDto responseGroupOrderCommentDto = ResponseGroupOrderCommentDto.builder()
                    .groupOrderCommentId(groupOrderComment.getId())
                    .userId(groupOrderComment.getUser().getId())
                    .reply(groupOrderComment.getReply())
                    .childGroupOrderCommentList(childResponseComments)
                    .build();
            responseGroupOrderCommentDtoList.add(responseGroupOrderCommentDto);

        }
        return responseGroupOrderCommentDtoList;
    }

    public void deleteGroupOrderComment(Long userId, Long groupOrderCommentId) {
        GroupOrderComment groupOrderComment = groupOrderCommentRepository.findByIdAndUserId(groupOrderCommentId, userId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_COMMENT_NOT_OWNED_BY_USER));
        groupOrderComment.updateIsDeleted();
    }
}
