package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseGroupOrderDetailDto {

    private Long id;
    private String title;
    private String deadline;
    private String createDate;
    private GroupOrderType groupOrderType;
    private int price;
    private int likeCount;
    private int viewCount;
    private String description;
    private String link;
    private String openChatLink;
    private boolean isMyPost = false;
    private String authorImagePath;
    private String username;
    private boolean isCheckLikeCurrentUser = false;
    private boolean isRecruitmentComplete;

    @Builder.Default
    private List<ResponseGroupOrderCommentDto> groupOrderCommentDtoList = new ArrayList<>();

    public static ResponseGroupOrderDetailDto entityToDto(GroupOrder groupOrder, List<ResponseGroupOrderCommentDto> responseGroupOrderCommentDto) {
        return ResponseGroupOrderDetailDto.builder()
                .id(groupOrder.getId())
                .title(groupOrder.getTitle())
                .deadline(String.valueOf(groupOrder.getDeadline()))
                .createDate(String.valueOf(groupOrder.getCreatedDate()))
                .groupOrderType(groupOrder.getGroupOrderType())
                .price(groupOrder.getPrice())
                .likeCount(groupOrder.getGroupOrderLike())
                .viewCount(groupOrder.getGroupOrderViewCount())
                .description(groupOrder.getDescription())
                .link(groupOrder.getLink())
                .openChatLink(groupOrder.getOpenChatLink())
                .groupOrderCommentDtoList(responseGroupOrderCommentDto)
                .username(groupOrder.getUser().getName())
                .isRecruitmentComplete(groupOrder.isRecruitmentComplete())
                .build();
    }

    public void updateGroupOrderCommentDtoList(List<ResponseGroupOrderCommentDto> groupedList) {
        this.groupOrderCommentDtoList = groupedList;
    }

    public void updateIsMyPost(boolean b) {
        this.isMyPost = b;
    }

    public void updateAuthorImagePath(String imagePath) {
        this.authorImagePath = imagePath;
    }

    public void updateIsCheckLikeCurrentUser(boolean check) {
        this.isCheckLikeCurrentUser = check;
    }

}