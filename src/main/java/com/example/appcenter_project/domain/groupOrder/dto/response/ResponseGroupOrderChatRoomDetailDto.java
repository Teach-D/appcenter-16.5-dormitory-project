package com.example.appcenter_project.domain.groupOrder.dto.response;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderChat;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderChatRoom;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseGroupOrderChatRoomDetailDto {

    private Long groupOrderChatRoomId;
    private String groupOrderChatRoomTitle;
    private Long groupOrderId;
    private Integer currentPeople;
    private Integer maxPeople;
    private LocalDateTime deadline;

    @Builder.Default
    private List<ResponseGroupOrderChatDto> groupOrderChatDtoList = new ArrayList<>();

    public static ResponseGroupOrderChatRoomDetailDto entityToDto(GroupOrderChatRoom groupOrderChatRoom, GroupOrder groupOrder) {
        List<ResponseGroupOrderChatDto> groupOrderChatDtoList = new ArrayList<>();
        for (GroupOrderChat groupOrderChat : groupOrderChatRoom.getGroupOrderChatList()) {
            ResponseGroupOrderChatDto responseGroupOrderChatDto = ResponseGroupOrderChatDto.entityToDto(groupOrderChat);
            groupOrderChatDtoList.add(responseGroupOrderChatDto);
        }

        return ResponseGroupOrderChatRoomDetailDto.builder()
                .groupOrderChatRoomId(groupOrderChatRoom.getId())
                .groupOrderChatRoomTitle(groupOrderChatRoom.getTitle())
                .groupOrderId(groupOrder.getId())
                .deadline(groupOrder.getDeadline())
                .groupOrderChatDtoList(groupOrderChatDtoList)
                .build();
    }
}
