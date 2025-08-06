package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateFormDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateSimilarityDto;
import com.example.appcenter_project.entity.like.RoommateBoardLike;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.roommate.MatchingStatus;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.like.RoommateBoardLikeRepository;
import com.example.appcenter_project.repository.roommate.RoommateBoardRepository;
import com.example.appcenter_project.repository.roommate.RoommateCheckListRepository;
import com.example.appcenter_project.repository.roommate.RoommateMatchingRepository;
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
    private final RoommateBoardLikeRepository roommateBoardLikeRepository;
    private final RoommateMatchingRepository roommateMatchingRepository;

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
                .religion(requestDto.getReligion())
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
                .religion(savedCheckList.getReligion())
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
                .userId(user.getId())
                .userName(user.getName())
                .createdDate(roommateBoard.getCreatedDate())
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
                    User writer = board.getUser();
                    return ResponseRoommatePostDto.builder() //화면에 보여줄 정보를 담아줌
                            .boardId(board.getId())
                            .title(cl.getTitle())
                            .dormPeriod(cl.getDormPeriod())
                            .dormType(cl.getDormType())
                            .college(cl.getCollege())
                            .religion(cl.getReligion())
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
                            .roommateBoardLike(board.getRoommateBoardLike())
                            .userId(writer.getId())
                            .userName(writer.getName())
                            .createdDate(board.getCreatedDate())
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
                    if (myChecklist.getReligion() == other.getReligion()) score++;
                    if (myChecklist.getMbti().equals(other.getMbti())) score++;
                    if (myChecklist.getSmoking() == other.getSmoking()) score++;
                    if (myChecklist.getSnoring() == other.getSnoring()) score++;
                    if (myChecklist.getToothGrind() == other.getToothGrind()) score++;
                    if (myChecklist.getSleeper() == other.getSleeper()) score++;
                    if (myChecklist.getShowerHour() == other.getShowerHour()) score++;
                    if (myChecklist.getShowerTime() == other.getShowerTime()) score++;
                    if (myChecklist.getBedTime() == other.getBedTime()) score++;
                    if (myChecklist.getArrangement() == other.getArrangement()) score++;

                    int similarityPercentage = (int) ((score / 12.0) * 100);

                    return Map.entry(board, similarityPercentage);
                })
                .sorted((e1, e2) -> {
                    int compareSim = e2.getValue().compareTo(e1.getValue()); // 유사도 내림차순
                    if (compareSim != 0) return compareSim;
                    return e2.getKey().getCreatedDate().compareTo(e1.getKey().getCreatedDate()); // 유사도 같으면 최신순
                })
                .map(entry -> {
                    RoommateBoard board = entry.getKey();
                    RoommateCheckList cl = entry.getKey().getRoommateCheckList();
                    User writer = board.getUser();

                    return ResponseRoommateSimilarityDto.builder()
                            .boardId(entry.getKey().getId())
                            .title(cl.getTitle())
                            .dormType(cl.getDormType())
                            .college(cl.getCollege())
                            .religion(cl.getReligion())
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
                            .roommateBoardLike(entry.getKey().getRoommateBoardLike())
                            .userId(writer.getId())
                            .userName(writer.getName())
                            .createdDate(board.getCreatedDate())
                            .build();
                })
                .toList();
    }

    @Transactional
    public ResponseRoommatePostDto updateRoommateChecklistAndBoard(RequestRoommateFormDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        RoommateBoard board = roommateBoardRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        RoommateCheckList checkList = board.getRoommateCheckList();

        if (!checkList.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ROOMMATE_UPDATE_NOT_ALLOWED);
        }

        try {
            checkList.update(requestDto);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ROOMMATE_CHECKLIST_UPDATE_FAILED);
        }

        return ResponseRoommatePostDto.entityToDto(board);
    }

    // 좋아요 추가 (Like)
    @Transactional
    public Integer likePlusRoommateBoard(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));
        RoommateBoard board = roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        // 이미 좋아요 누른 경우 예외처리
        if (roommateBoardLikeRepository.existsByUserAndRoommateBoard(user, board)) {
            throw new CustomException(ErrorCode.ALREADY_ROOMMATE_BOARD_LIKE_USER);
        }

        RoommateBoardLike roommateBoardLike = RoommateBoardLike.builder()
                .user(user)
                .roommateBoard(board)
                .build();

        roommateBoardLikeRepository.save(roommateBoardLike);

        // user에 좋아요 정보 추가 (양방향 연관관계 유지 시)
        user.addRoommateBoardLike(roommateBoardLike);

        // board에 좋아요 정보 추가
        board.getRoommateBoardLikeList().add(roommateBoardLike);

        // 좋아요 카운트 증가
        return board.plusLike();
    }

    // 좋아요 취소 (Unlike)
    @Transactional
    public Integer likeMinusRoommateBoard(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));
        RoommateBoard board = roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        if (!roommateBoardLikeRepository.existsByUserAndRoommateBoard(user, board)) {
            throw new CustomException(ErrorCode.ROOMMATE_BOARD_LIKE_NOT_FOUND);
        }

        RoommateBoardLike roommateBoardLike = roommateBoardLikeRepository.findByUserAndRoommateBoard(user, board)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_LIKE_NOT_FOUND));

        // user에서 좋아요 정보 제거
        user.removeRoommateBoardLike(roommateBoardLike);

        // board에서 좋아요 정보 제거
        board.getRoommateBoardLikeList().remove(roommateBoardLike);

        // 좋아요 DB에서 삭제
        roommateBoardLikeRepository.delete(roommateBoardLike);

        // 좋아요 카운트 감소
        return board.minusLike();
    }

    public boolean isRoommateBoardOwnerMatched(Long boardId) {
        // 1. 게시글 조회
        RoommateBoard board = roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        User owner = board.getUser();

        // 2. owner가 COMPLETED 상태인 매칭에 포함되어 있는지 검사
        boolean alreadyMatched =
                roommateMatchingRepository.existsBySenderAndStatus(owner, MatchingStatus.COMPLETED)
                        || roommateMatchingRepository.existsByReceiverAndStatus(owner, MatchingStatus.COMPLETED);

        return alreadyMatched;
    }

}
