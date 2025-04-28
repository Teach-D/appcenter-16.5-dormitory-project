package com.example.appcenter_project.config;

import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatRoomDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseReadGroupOrderChat;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChat;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import com.example.appcenter_project.repository.groupOrder.GroupOrderChatRepository;
import com.example.appcenter_project.repository.groupOrder.GroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.groupOrder.UserGroupOrderChatRoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final SubProtocolWebSocketHandler webSocketHandler;
    private final UserGroupOrderChatRoomRepository userGroupOrderChatRoomRepository;
    private final GroupOrderChatRoomRepository groupOrderChatRoomRepository;
    private final GroupOrderChatRepository groupOrderChatRepository;

    public static final Map<String, String> chatRoomListInUserMap = new ConcurrentHashMap<>(); // 세션 ID -> 사용자 ID
    public static final Map<String, List<String>> groupOrderChatRoomInUserMap = new ConcurrentHashMap<>(); // 세션 ID -> 사용자 ID
    public static final Map<String, String> groupOrderChatRoomMap = new ConcurrentHashMap<>(); // 세션 ID -> 사용자 ID
    public static final Map<String, String> groupOrderChatRoomUserMap = new ConcurrentHashMap<>();

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate, WebSocketHandler webSocketHandler, UserGroupOrderChatRoomRepository userGroupOrderChatRoomRepository, UserGroupOrderChatRoomRepository userGroupOrderChatRoomRepository1, GroupOrderChatRoomRepository groupOrderChatRoomRepository, GroupOrderChatRepository groupOrderChatRepository) {
        this.messagingTemplate = messagingTemplate;
        this.webSocketHandler = (SubProtocolWebSocketHandler) webSocketHandler;
        this.userGroupOrderChatRoomRepository = userGroupOrderChatRoomRepository1;
        this.groupOrderChatRoomRepository = groupOrderChatRoomRepository;
        this.groupOrderChatRepository = groupOrderChatRepository;
    }

    // /ws로 웹소켓 연결 시 호출
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        int sessionCount = webSocketHandler.getStats().getWebSocketSessions();
        log.info("웹소켓 연결됨. 현재 동시접속자 수: {}", sessionCount);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userId = accessor.getFirstNativeHeader("userId");

        if (userId != null) {
            accessor.getSessionAttributes().put("userId", userId); // 세션에 저장
        }

        log.info("연결된 사용자 ID: {}", userId);
    }

    // 프론트엔드에서 구독(/sub)할 때 호출됨
    @EventListener
    @Transactional
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination(); // 구독한 경로

        // 유저가 채팅방목록으로 입장 했을 때의 경우
        if (destination != null && destination.startsWith("/sub/chatRoomList/")) {
            String[] pathParts = destination.split("/");
            Long userId = Long.valueOf(pathParts[3]); // userId

            chatRoomListInUserMap.put(sessionId, String.valueOf(userId));

            // entity to dto
            List<ResponseGroupOrderChatRoomDto> groupOrderChatRoomDtos = new ArrayList<>();
            List<UserGroupOrderChatRoom> userGroupOrderChatRoomByUserId = userGroupOrderChatRoomRepository.findUserGroupOrderChatRoomByUser_Id(userId);
            for (UserGroupOrderChatRoom userGroupOrderChatRoom : userGroupOrderChatRoomByUserId) {
                ResponseGroupOrderChatRoomDto responseGroupOrderChatRoomDto = ResponseGroupOrderChatRoomDto.entityToDto(userGroupOrderChatRoom);
                groupOrderChatRoomDtos.add(responseGroupOrderChatRoomDto);
            }

            // db에서 특정 유저의 모든 GroupOrderChatRoom를 리턴
            messagingTemplate.convertAndSend("/sub/return/chatRoomList/" + userId, groupOrderChatRoomDtos);
        }
        // 유저가 자신이 가입한 GroupOrder 채팅방에 입장했을 경우
        else if (destination != null && destination.startsWith("/sub/groupOrderChatRoom/")) {
            String[] pathParts = destination.split("/");
            String groupOrderChatRoomId = pathParts[3]; // chatRoomId
            String userId = pathParts[5]; // memberId

            // 같은 sessionId로 특정 유저가 자신이 가입한 채팅방에 입장한 것을 저장
            groupOrderChatRoomMap.put(sessionId, String.valueOf(groupOrderChatRoomId));
            groupOrderChatRoomUserMap.put(sessionId, String.valueOf(userId));

            // 채팅방에 유저 입장
            List<String> chatRoomUser = groupOrderChatRoomInUserMap.get(groupOrderChatRoomId);
            chatRoomUser.add(userId);
            groupOrderChatRoomInUserMap.put(groupOrderChatRoomId, chatRoomUser);

            // 채팅방 입장시 읽지 않은 메시지 읽음으로 db저장, 읽은 List<GroupOrderChatId> websocket 리턴
            List<ResponseReadGroupOrderChat> readGroupOrderChatId = new ArrayList<>();
            List<GroupOrderChat> groupOrderChats = groupOrderChatRepository.findByGroupOrderChatRoom_id(Long.valueOf(groupOrderChatRoomId));
            for (GroupOrderChat groupOrderChat : groupOrderChats) {
                if (groupOrderChat.getUnreadUser().contains(Long.valueOf(userId))) {
                    ResponseReadGroupOrderChat.builder().readGroupOrderChatId(groupOrderChat.getId());

                    // 읽지 않은 메시지에서 유저 삭제
                    groupOrderChat.getUnreadUser().remove(Long.valueOf(userId));
                }
            }

            // 읽은 List GroupOrderChatId를 리턴
            messagingTemplate.convertAndSend("/sub/readUser/" + groupOrderChatRoomId, readGroupOrderChatId);

        }
    }
    // 클라이언트가 stompClient.deactivate()로 웹소켓 연결 해체 시 호출
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        // 유저가 채팅방 목록에서 퇴장했을 때 session삭제
        if (chatRoomListInUserMap.containsKey(sessionId)) {
            chatRoomListInUserMap.remove(sessionId);
        }
        // 유저가 채팅방에서 퇴장했을 때 session 삭제
        else {
            String groupChatRoomId = groupOrderChatRoomMap.get(sessionId);
            String userId = groupOrderChatRoomUserMap.get(sessionId);

            // sessionId로 GroupChatRoom에 입장해 있는 userId 획득
            List<String> userList = groupOrderChatRoomInUserMap.get(groupChatRoomId);
            if (userList != null && userList.size() > 0) {
                // 채팅방 퇴장
                userList.remove(userId);
                if (userList.isEmpty()) {
                    groupOrderChatRoomInUserMap.remove(groupChatRoomId); // 리스트가 비면 map에서 방 자체도 삭제
                }
            }

            groupOrderChatRoomMap.remove(sessionId);
            groupOrderChatRoomUserMap.remove(sessionId);
        }
    }
}
