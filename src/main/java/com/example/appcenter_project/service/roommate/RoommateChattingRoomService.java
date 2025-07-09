package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.response.roommate.ResponseRoommateChatRoomDto;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.roommate.RoommateChattingChat;
import com.example.appcenter_project.entity.roommate.RoommateChattingRoom;
import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.roommate.RoommateBoardRepository;
import com.example.appcenter_project.repository.roommate.RoommateChattingRoomRepository;
import com.example.appcenter_project.repository.roommate.RoommateCheckListRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.appcenter_project.exception.ErrorCode.*;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoommateChattingRoomService {

    private final RoommateChattingRoomRepository roommateChattingRoomRepository;
    private final RoommateBoardRepository roommateBoardRepository;
    private final UserRepository userRepository;

    //채팅방 생성
    @Transactional
    public Long createChatRoom(Long guestId, Long roommateBoardId) {
        // 게시글 조회
        RoommateBoard roommateBoard = roommateBoardRepository.findById(roommateBoardId)
                .orElseThrow(() -> new CustomException(ROOMMATE_BOARD_NOT_FOUND));

        // host는 게시글 작성자
        User host = roommateBoard.getUser();
        User guest = userRepository.findById(guestId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 자기 자신과 채팅 방지
        if (host.getId().equals(guest.getId())) {
            throw new CustomException(ROOMMATE_CHAT_CANNOT_CHAT_WITH_SELF);
        }

        // 이미 채팅방이 있는지 확인
        boolean exists = roommateChattingRoomRepository.existsByRoommateBoardAndGuest(roommateBoard, guest);
        if (exists) {
            throw new CustomException(DUPLICATE_CHAT_ROOM);
        }

        // 채팅방 생성
        RoommateChattingRoom chattingRoom = RoommateChattingRoom.builder()
                .roommateBoard(roommateBoard)
                .host(host)
                .guest(guest)
                .hostChecklist(host.getRoommateCheckList())
                .guestChecklist(guest.getRoommateCheckList())
                .build();

        roommateChattingRoomRepository.save(chattingRoom);
        return chattingRoom.getId();
    }

    //채팅방 나가기
    @Transactional
    public void leaveChatRoom(Long guestId, Long chatRoomId) {
        // 유저 조회
        User guest = userRepository.findById(guestId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 채팅방 조회
        RoommateChattingRoom chatRoom = roommateChattingRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ROOMMATE_CHAT_ROOM_NOT_FOUND));

        // 채팅방의 게스트와 일치하는지 확인
        if (!chatRoom.getGuest().getId().equals(guest.getId())) {
            throw new CustomException(ROOMMATE_FORBIDDEN_ACCESS);
        }

        // 채팅방 삭제
        roommateChattingRoomRepository.delete(chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ResponseRoommateChatRoomDto> findRoommateChatRoomListByUser(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<RoommateChattingRoom> rooms = roommateChattingRoomRepository.findAllByHostOrGuest(user, user);

        List<ResponseRoommateChatRoomDto> result = new ArrayList<>();
        int index = 1;

        for (RoommateChattingRoom room : rooms) {
            // 익명 N 설정
            String opponentNickname = "익명 " + index++;

            // 마지막 메시지
            Optional<RoommateChattingChat> lastChat = room.getChattingChatList().stream()
                    .max(Comparator.comparing(RoommateChattingChat::getCreatedDate));

            String lastMessage = lastChat.map(RoommateChattingChat::getContent).orElse("");
            LocalDateTime lastMessageTime = lastChat.map(RoommateChattingChat::getCreatedDate).orElse(null);

            // DTO 생성
            result.add(ResponseRoommateChatRoomDto.builder()
                    .chatRoomId(room.getId())
                    .opponentNickname(opponentNickname)
                    .lastMessage(lastMessage)
                    .lastMessageTime(lastMessageTime)
                    .build());
        }

        // 마지막 메시지 시간 기준 내림차순 정렬 (null은 맨 뒤)
        result.sort(Comparator.comparing(ResponseRoommateChatRoomDto::getLastMessageTime,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return result;
    }

    @Transactional(readOnly = true)
    public RoommateCheckList getOpponentChecklist(Long userId, Long chatRoomId) {
        RoommateChattingRoom chatRoom = roommateChattingRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ROOMMATE_CHAT_ROOM_NOT_FOUND));

        // 본인이 참여 중인지 검증
        if (!chatRoom.getHost().getId().equals(userId) && !chatRoom.getGuest().getId().equals(userId)) {
            throw new CustomException(ROOMMATE_FORBIDDEN_ACCESS);
        }

        // 상대방 체크리스트 반환
        if (chatRoom.getHost().getId().equals(userId)) {
            return chatRoom.getGuestChecklist(); // 상대는 guest
        } else {
            return chatRoom.getHostChecklist(); // 상대는 host
        }
    }




}

