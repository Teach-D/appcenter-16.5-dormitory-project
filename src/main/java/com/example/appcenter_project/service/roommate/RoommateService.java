package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateFormDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateSimilarityDto;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.roommate.RoommateBoardRepository;
import com.example.appcenter_project.repository.roommate.RoommateCheckListRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoommateService {

    private final UserRepository userRepository;
    private final RoommateCheckListRepository roommateCheckListRepository;
    private final RoommateBoardRepository roommateBoardRepository;

    @Transactional
    public ResponseRoommatePostDto createRoommateCheckListandBoard(RequestRoommateFormDto requestDto, Long userId) {
        // 1. 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        // 2. 체크리스트 생성 + user 설정
        RoommateCheckList roommateCheckList = RoommateCheckList.builder()
                .title(requestDto.getTitle())
                .dormPeriod(requestDto.getDormPeriod())
                .dormType(requestDto.getDormType())
                .college(requestDto.getCollege())
                .mbti(requestDto.getMbti())
                .smoking(requestDto.getSmoking())
                .snoring(requestDto.getSnoring())
                .toothGrind(requestDto.getToothGrind())
                .sleeper(requestDto.getSleeper())
                .showerHour(requestDto.getShowerHour())
                .showerTime(requestDto.getShowerTime())
                .bedTime(requestDto.getBedTime())
                .arrangement(requestDto.getArrangement())
                .comment(requestDto.getComment())
                .user(user) // 필수!
                .build();

        RoommateCheckList savedCheckList = roommateCheckListRepository.save(roommateCheckList);

        // 3. 게시글 생성 및 저장
        RoommateBoard roommateBoard = RoommateBoard.builder()
                .title(requestDto.getTitle())
                .user(user)
                .roommateBoardLike(0)
                .roommateCheckList(savedCheckList)
                .build();

        roommateBoardRepository.save(roommateBoard);

        // 4. 응답
        return ResponseRoommatePostDto.builder()
                .boardId(roommateBoard.getId())
                .title(savedCheckList.getTitle())
                .dormPeriod(savedCheckList.getDormPeriod())
                .dormType(savedCheckList.getDormType())
                .college(savedCheckList.getCollege())
                .mbti(savedCheckList.getMbti())
                .smoking(savedCheckList.getSmoking())
                .snoring(savedCheckList.getSnoring())
                .toothGrind(savedCheckList.getToothGrind())
                .sleeper(savedCheckList.getSleeper())
                .showerHour(savedCheckList.getShowerHour())
                .showerTime(savedCheckList.getShowerTime())
                .bedTime(savedCheckList.getBedTime())
                .arrangement(savedCheckList.getArrangement())
                .comment(savedCheckList.getComment())
                .build();
    }

    //최신순 조회
    public List<ResponseRoommatePostDto> getRoommateBoardList() {
        List<RoommateBoard> boards = roommateBoardRepository.findAllByOrderByCreatedDateDesc();

        if (boards.isEmpty()){
            throw new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND);
        }

        return boards.stream() //게시글 목록을 하나 꺼내서 준비
                .map(board -> { //꺼낸 게시글 하나를 꾸미기 시작
                    RoommateCheckList cl = board.getRoommateCheckList(); //룸메이트 보드에있는 체크리스트를 꺼냄
                    return ResponseRoommatePostDto.builder() //화면에 보여줄 정보를 담아줌
                            .boardId(board.getId())
                            .title(cl.getTitle())
                            .dormPeriod(cl.getDormPeriod())
                            .dormType(cl.getDormType())
                            .college(cl.getCollege())
                            .mbti(cl.getMbti())
                            .smoking(cl.getSmoking())
                            .snoring(cl.getSnoring())
                            .toothGrind(cl.getToothGrind())
                            .sleeper(cl.getSleeper())
                            .showerHour(cl.getShowerHour())
                            .showerTime(cl.getShowerTime())
                            .bedTime(cl.getBedTime())
                            .arrangement(cl.getArrangement())
                            .comment(cl.getComment())
                            .build(); //dto하나가 만들어짐
                })
                .toList(); //만든 dto들을 모아서 리스트로 뭉쳐줌
    }

    //단일 조회
    public ResponseRoommatePostDto getRoommateBoardDetail(Long boardId){
        RoommateBoard board = roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        return ResponseRoommatePostDto.entityToDto(board);
    }

    public List<ResponseRoommateSimilarityDto> getSimilarRoommateBoards(Long userId) {
        // 1. 내 체크리스트 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        RoommateBoard myBoard = roommateBoardRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        RoommateCheckList myChecklist = myBoard.getRoommateCheckList();

        // 2. 다른 사람 게시글 가져오기
        List<RoommateBoard> allBoards = roommateBoardRepository.findAll();
        List<RoommateBoard> otherBoards = allBoards.stream()
                .filter(b -> !b.getUser().getId().equals(userId))
                .toList();

        if (otherBoards.isEmpty()) {
            throw new CustomException(ErrorCode.ROOMMATE_NO_SIMILAR_BOARD); // 새로 정의 필요
        }

        // 3. 유사도 계산 및 정렬
        return otherBoards.stream()
                .map(board -> {
                    RoommateCheckList other = board.getRoommateCheckList();
                    int score = 0;

                    if (myChecklist.getDormType() == other.getDormType()) score++;
                    if (myChecklist.getCollege() == other.getCollege()) score++;
                    if (myChecklist.getMbti().equals(other.getMbti())) score++;
                    if (myChecklist.getSmoking() == other.getSmoking()) score++;
                    if (myChecklist.getSnoring() == other.getSnoring()) score++;
                    if (myChecklist.getToothGrind() == other.getToothGrind()) score++;
                    if (myChecklist.getSleeper() == other.getSleeper()) score++;
                    if (myChecklist.getShowerHour() == other.getShowerHour()) score++;
                    if (myChecklist.getShowerTime() == other.getShowerTime()) score++;
                    if (myChecklist.getBedTime() == other.getBedTime()) score++;
                    if (myChecklist.getArrangement() == other.getArrangement()) score++;

                    int similarityPercentage = (int) ((score / 11.0) * 100);

                    return Map.entry(board, similarityPercentage);
                })
                .sorted((e1, e2) -> {
                    int compareSim = e2.getValue().compareTo(e1.getValue()); // 유사도 내림차순
                    if (compareSim != 0) return compareSim;
                    return e2.getKey().getCreatedDate().compareTo(e1.getKey().getCreatedDate()); // 유사도 같으면 최신순
                })
                .map(entry -> {
                    RoommateCheckList cl = entry.getKey().getRoommateCheckList();
                    return ResponseRoommateSimilarityDto.builder()
                            .boardId(entry.getKey().getId())
                            .title(cl.getTitle())
                            .dormType(cl.getDormType())
                            .college(cl.getCollege())
                            .mbti(cl.getMbti())
                            .smoking(cl.getSmoking())
                            .snoring(cl.getSnoring())
                            .toothGrind(cl.getToothGrind())
                            .sleeper(cl.getSleeper())
                            .showerHour(cl.getShowerHour())
                            .showerTime(cl.getShowerTime())
                            .bedTime(cl.getBedTime())
                            .arrangement(cl.getArrangement())
                            .comment(cl.getComment())
                            .similarityPercentage(entry.getValue())
                            .build();
                })
                .toList();
    }
}
