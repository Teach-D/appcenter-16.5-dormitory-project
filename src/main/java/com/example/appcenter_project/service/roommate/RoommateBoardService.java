package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateBoardDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateBoardDto;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.repository.roommate.RoommateBoardRepository;
import com.example.appcenter_project.repository.roommate.RoommateCheckListRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RoommateBoardService {

    private final UserRepository userRepository;
    private final RoommateBoardRepository roommateBoardRepository;
    private final RoommateCheckListRepository roommateCheckListRepository;

    /**
     * 룸메이트 체크리스트 + 게시글 생성
     * @param userId 로그인한 사용자 ID
     * @param requestDto 게시글 + 체크리스트 요청 정보
     * @return 생성된 게시글 ID 및 메시지 응답 DTO
     */
    public ResponseRoommateBoardDto createBoard(Long userId, RequestRoommateBoardDto requestDto) {
        // 사용자 조회
        User user = userRepository.findById(userId).orElseThrow();

        // 체크리스트 생성 및 저장
        RoommateCheckList checklist = RequestRoommateBoardDto.dtoToCheckList(requestDto);
        roommateCheckListRepository.save(checklist);

        // 게시글 생성 및 저장 (체크리스트 + 사용자 연결)
        RoommateBoard board = RoommateBoard.builder()
                .roommateCheckList(checklist)
                .user(user)
                .title(requestDto.getTitle())
                .build();

        roommateBoardRepository.save(board);

        // 응답 반환
        return ResponseRoommateBoardDto.of(board.getId());
    }
}
