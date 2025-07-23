package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.response.roommate.ResponseRoommateChatRoomDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateCheckListDto;
import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.roommate.RoommateChattingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/roommate-chatting-room")
@RequiredArgsConstructor
public class RoommateChattingRoomController implements RoommateChattingRoomApiSpecification{

    private final RoommateChattingRoomService roommateChattingRoomService;

    // 게스트가 채팅방 참여 요청
    @PostMapping("/board/{roommateBoardId}")
    public ResponseEntity<Long> createChatRoom(@AuthenticationPrincipal CustomUserDetails user,
                                               @PathVariable Long roommateBoardId) {
        Long roomId = roommateChattingRoomService.createChatRoom(user.getId(), roommateBoardId);
        return ResponseEntity.status(201).body(roomId);
    }

    // 채팅방 나가기 (게스트 기준)
    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<Void> leaveChatRoom(@AuthenticationPrincipal CustomUserDetails user,
                                              @PathVariable Long chatRoomId) {
        roommateChattingRoomService.leaveChatRoom(user.getId(), chatRoomId);
        return ResponseEntity.status(CREATED).build();
    }

    // 채팅방 목록 조회
    @GetMapping
    public ResponseEntity<List<ResponseRoommateChatRoomDto>> getRoommateChatRoomList(
            @AuthenticationPrincipal CustomUserDetails user) {
        List<ResponseRoommateChatRoomDto> chatRooms = roommateChattingRoomService.findRoommateChatRoomListByUser(user);
        return ResponseEntity.ok(chatRooms);
    }

    //상대방 체크리스트 확인
    @GetMapping("/roommate-chatrooms/{chatRoomId}/opponent-checklist")
    public ResponseEntity<ResponseRoommateCheckListDto> getOpponentChecklist(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long chatRoomId
    ) {
        RoommateCheckList checklist = roommateChattingRoomService.getOpponentChecklist(user.getId(), chatRoomId);
        return ResponseEntity.ok(ResponseRoommateCheckListDto.from(checklist));
    }


}