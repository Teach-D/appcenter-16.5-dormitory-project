package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderCommentDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.groupOrder.GroupOrderCommentRepository;
import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import com.example.appcenter_project.repository.user.UserRepository;
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

    public ResponseGroupOrderCommentDto saveGroupOrderComment(Long userId, RequestGroupOrderCommentDto responseGroupOrderCommentDto) {
        User user = userRepository.findById(userId).orElseThrow();
        GroupOrder groupOrder = groupOrderRepository.findById(responseGroupOrderCommentDto.getGroupOrderId()).orElseThrow();
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
            GroupOrderComment parentGroupOrderComment = groupOrderCommentRepository.findById(responseGroupOrderCommentDto.getParentCommentId()).orElseThrow();
            groupOrderComment = GroupOrderComment.builder()
                    .reply(responseGroupOrderCommentDto.getReply())
                    .groupOrder(groupOrder)
                    .user(user)
                    .parentGroupOrderComment(parentGroupOrderComment)
                    .build();
            parentGroupOrderComment.addChildGroupOrderComments(groupOrderComment);
        }
        groupOrderCommentRepository.save(groupOrderComment);
        return ResponseGroupOrderCommentDto.entityToDto(groupOrderComment, user);
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
