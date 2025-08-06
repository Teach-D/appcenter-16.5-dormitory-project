package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChat;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChatRoom;
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
                .currentPeople(groupOrder.getCurrentPeople())
                .maxPeople(groupOrder.getMaxPeople())
                .deadline(groupOrder.getDeadline())
                .groupOrderChatDtoList(groupOrderChatDtoList)
                .build();
    }
}
