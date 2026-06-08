package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.dto.request.RequestOpenChatMessageDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatMessageDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatMessageListDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatMessage;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatMessageType;
import com.example.appcenter_project.domain.openChat.repository.OpenChatMessageRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OpenChatMessageService {

    private final OpenChatRoomRepository openChatRoomRepository;
    private final OpenChatParticipantRepository openChatParticipantRepository;
    private final OpenChatMessageRepository openChatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(Long userId, RequestOpenChatMessageDto request) {
        OpenChatRoom room = openChatRoomRepository.findById(request.getRoomId()).orElse(null);
        if (room == null) return;

        OpenChatParticipant participant = openChatParticipantRepository
                .findByRoomIdAndUserId(request.getRoomId(), userId).orElse(null);
        if (participant == null) return;

        User sender = userRepository.findById(userId).orElse(null);
        if (sender == null) return;

        OpenChatMessage message = OpenChatMessage.create(request.getRoomId(), userId, request.getContent(), OpenChatMessageType.TEXT);
        openChatMessageRepository.save(message);

        room.updateLastMessage(message.getContent(), message.getCreatedDate());

        participant.updateLastReadMessageId(message.getId());

        int unreadCount = calculateUnreadCount(request.getRoomId(), message.getId());

        ResponseOpenChatMessageDto response = ResponseOpenChatMessageDto.from(message, sender.getName(), unreadCount);
        messagingTemplate.convertAndSend("/sub/openchat/" + request.getRoomId(), response);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendSystemMessage(Long roomId, String content) {
        OpenChatRoom room = openChatRoomRepository.findById(roomId).orElse(null);
        if (room == null) return;

        OpenChatMessage message = OpenChatMessage.create(roomId, 0L, content, OpenChatMessageType.SYSTEM);
        openChatMessageRepository.save(message);

        room.updateLastMessage(message.getContent(), message.getCreatedDate());

        int unreadCount = calculateUnreadCount(roomId, message.getId());

        ResponseOpenChatMessageDto response = ResponseOpenChatMessageDto.from(message, null, unreadCount);
        messagingTemplate.convertAndSend("/sub/openchat/" + roomId, response);
    }

    @Transactional
    public ResponseOpenChatMessageListDto getMessages(Long userId, Long roomId, Long lastMessageId, int size) {
        openChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_NOT_PARTICIPANT));

        List<OpenChatMessage> messages = openChatMessageRepository.findByRoomIdWithCursor(roomId, lastMessageId, size + 1);

        boolean hasNext = messages.size() > size;
        if (hasNext) {
            messages = messages.subList(0, size);
        }

        Long latestId = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
        if (latestId != null) {
            openChatParticipantRepository.updateLastReadMessageId(roomId, userId, latestId);
        }

        List<ResponseOpenChatMessageDto> dtos = messages.stream()
                .map(msg -> {
                    String nickname = msg.getType() == OpenChatMessageType.SYSTEM ? null :
                            userRepository.findById(msg.getSenderId()).map(User::getName).orElse(null);
                    int unreadCount = calculateUnreadCount(roomId, msg.getId());
                    return ResponseOpenChatMessageDto.from(msg, nickname, unreadCount);
                })
                .toList();

        Long nextCursor = hasNext && !messages.isEmpty() ? messages.get(0).getId() : null;

        return ResponseOpenChatMessageListDto.builder()
                .messages(dtos)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    public int calculateUnreadCount(Long roomId, Long messageId) {
        long total = openChatParticipantRepository.countByRoomId(roomId);
        long readCount = openChatParticipantRepository.countReadByRoomIdAndMessageId(roomId, messageId);
        return (int) (total - readCount);
    }
}
