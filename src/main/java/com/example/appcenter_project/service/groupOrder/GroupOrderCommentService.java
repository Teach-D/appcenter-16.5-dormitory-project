package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderCommentDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.ApiType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.groupOrder.GroupOrderCommentRepository;
import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.*;

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

        String title = "공동구매 게시글에 댓글이 작성되었습니다!";

        Notification notification = Notification.builder()
                .boardId(groupOrder.getId())
                .title(title)
                .body(groupOrder.getTitle())
                .notificationType(NotificationType.GROUP_ORDER)
                .apiType(ApiType.GROUP_ORDER)
                .build();

        notificationRepository.save(notification);

        UserNotification userNotification = UserNotification.of(groupOrder.getUser(), notification);
        userNotificationRepository.save(userNotification);

        fcmMessageService.sendNotification(groupOrder.getUser(), title, groupOrder.getTitle());

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


            String commentTitle = "공동구매 게시글의 대댓글이 작성되었습니다!";

            Notification commentNotification = Notification.builder()
                    .boardId(groupOrder.getId())
                    .title(commentTitle)
                    .body(groupOrder.getTitle())
                    .notificationType(NotificationType.GROUP_ORDER)
                    .apiType(ApiType.GROUP_ORDER)
                    .build();

            notificationRepository.save(commentNotification);

            UserNotification commentUserNotification = UserNotification.of(parentGroupOrderComment.getUser(), commentNotification);
            userNotificationRepository.save(commentUserNotification);

            fcmMessageService.sendNotification(parentGroupOrderComment.getUser(), title, groupOrder.getTitle());
        }

        // 양방향 매핑
        groupOrder.getGroupOrderCommentList().add(groupOrderComment);

        groupOrderCommentRepository.save(groupOrderComment);
        return ResponseGroupOrderCommentDto.entityToDto(groupOrderComment);
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
