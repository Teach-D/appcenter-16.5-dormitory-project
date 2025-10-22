package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateFormDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateCheckListDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateSimilarityDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoardLike;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardLikeRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateCheckListRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.shared.util.DormDayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@Transactional
@Service
@RequiredArgsConstructor
public class RoommateService {

    private final UserRepository userRepository;
    private final RoommateCheckListRepository roommateCheckListRepository;
    private final RoommateBoardRepository roommateBoardRepository;
    private final RoommateBoardLikeRepository roommateBoardLikeRepository;
    private final RoommateMatchingRepository roommateMatchingRepository;
    private final ImageService imageService;


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
                .id(roommateBoard.getId())
                .title(savedCheckList.getTitle())
                .dormPeriod(DormDayUtil.sortDormDays(savedCheckList.getDormPeriod()))
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
                .createDate(roommateBoard.getCreatedDate())
                .isMatched(false)
                .build();
    }

    //최신순 조회
    public List<ResponseRoommatePostDto> getRoommateBoardList(jakarta.servlet.http.HttpServletRequest request) {
        List<RoommateBoard> boards = roommateBoardRepository.findAllByOrderByCreatedDateDesc();

        if (boards.isEmpty()){
            throw new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND);
        }

        return boards.stream()
                .map(board -> {
                    RoommateCheckList cl = board.getRoommateCheckList();
                    User writer = board.getUser();
                    boolean isMatched = isRoommateBoardOwnerMatched(board.getId());

                    String writerImg = null;
                    try {
                        writerImg = imageService.findStaticImageUrl(ImageType.USER, writer.getId(), request);
                    } catch (Exception ignored) {}

                    return ResponseRoommatePostDto.builder()
                            .id(board.getId())
                            .title(cl.getTitle())
                            .dormPeriod(DormDayUtil.sortDormDays(cl.getDormPeriod())) // 정렬해서 넣음
                            .dormType(writer.getDormType())
                            .college(writer.getCollege())
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
                            .createDate(board.getCreatedDate())
                            .isMatched(isMatched)
                            .userProfileImageUrl(writerImg)
                            .build();
                })
                .toList();
    }

    //단일 조회
    public ResponseRoommatePostDto getRoommateBoardDetail(Long boardId, jakarta.servlet.http.HttpServletRequest request){
        RoommateBoard board = roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        boolean isMatched = isRoommateBoardOwnerMatched(boardId);
        String writerImg = null;
        try {
            writerImg = imageService.findStaticImageUrl(ImageType.USER, board.getUser().getId(), request);
        } catch (Exception ignored) {}

        return ResponseRoommatePostDto.entityToDto(board, isMatched, writerImg);
    }

    //유사도 조회
    public List<ResponseRoommateSimilarityDto> getSimilarRoommateBoards(Long userId, jakarta.servlet.http.HttpServletRequest request) {
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
                    boolean isMatched = isRoommateBoardOwnerMatched(board.getId());
                    RoommateCheckList cl = entry.getKey().getRoommateCheckList();
                    User writer = board.getUser();

                    String writerImg = null;
                    try {
                        writerImg = imageService.findStaticImageUrl(ImageType.USER, writer.getId(), request);
                    } catch (Exception ignored) {}

                    return ResponseRoommateSimilarityDto.builder()
                            .boardId(entry.getKey().getId())
                            .title(cl.getTitle())
                            .dormPeriod(DormDayUtil.sortDormDays(cl.getDormPeriod()))
                            .dormType(writer.getDormType())
                            .college(writer.getCollege())
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
                            .isMatched(isMatched)
                            .userProfileImageUrl(writerImg)
                            .build();
                })
                .toList();
    }

    @Transactional
    public ResponseRoommatePostDto updateRoommateChecklistAndBoard(
            RequestRoommateFormDto requestDto,
            Long userId,
            jakarta.servlet.http.HttpServletRequest request // 추가
    )  {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        RoommateBoard board = roommateBoardRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        RoommateCheckList checkList = board.getRoommateCheckList();
        boolean isMatched = isRoommateBoardOwnerMatched(board.getId());

        if (!checkList.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ROOMMATE_UPDATE_NOT_ALLOWED);
        }

        try {
            checkList.update(requestDto);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ROOMMATE_CHECKLIST_UPDATE_FAILED);
        }

        String writerImg = null;
        try {
            writerImg = imageService.findStaticImageUrl(ImageType.USER, user.getId(), request);
        } catch (Exception ignored) {}

        return ResponseRoommatePostDto.entityToDto(board, isMatched, writerImg);
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

    //로그인한 사용자가 좋아요를 눌렀는지 여부를 반환
    public boolean isRoommateBoardLikedByUser(Long boardId, Long userId) {
        RoommateBoard board = roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));
        return roommateBoardLikeRepository.existsByUserAndRoommateBoard(user, board);
    }

    @Transactional(readOnly = true)
    public ResponseRoommateCheckListDto getMyRoommateCheckList(Long userId) {
        RoommateCheckList checkList = roommateCheckListRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_CHECKLIST_NOT_FOUND));
        return ResponseRoommateCheckListDto.from(checkList);
    }

    @Transactional(readOnly = true)
    public ResponseRoommatePostDto getRandomFromLatest10(Long userId, HttpServletRequest request) {
        List<RoommateBoard> latest10 = roommateBoardRepository.findTop10ByOrderByCreatedDateDesc();
        if (latest10.isEmpty()) {
            throw new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND);
        }

        RoommateBoard target = latest10.get(ThreadLocalRandom.current().nextInt(latest10.size()));

        boolean isMatched = isRoommateBoardOwnerMatched(target.getId());

        String writerImg = null;
        try {
            writerImg = imageService.findStaticImageUrl(ImageType.USER, target.getUser().getId(), request);
        } catch (Exception ignored) {}

        return ResponseRoommatePostDto.entityToDto(target, isMatched, writerImg);
    }

    @Transactional(readOnly = true)
    public List<ResponseRoommatePostDto> getRoommateBoardListScroll(HttpServletRequest request, Long lastId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<RoommateBoard> boards;

        if (lastId == null) {
            // 첫 로딩: 최신순 size개
            boards = roommateBoardRepository.findAllByOrderByIdDesc(pageable);
        } else {
            // 이후 로딩: lastId보다 작은 데이터
            boards = roommateBoardRepository.findByIdLessThanOrderByIdDesc(lastId, pageable);
        }

        if (boards.isEmpty()) {
            return List.of(); // 마지막 페이지
        }

        return boards.stream().map(board -> {
            RoommateCheckList cl = board.getRoommateCheckList();
            User writer = board.getUser();
            boolean isMatched = isRoommateBoardOwnerMatched(board.getId());

            String writerImg = null;
            try {
                writerImg = imageService.findStaticImageUrl(ImageType.USER, writer.getId(), request);
            } catch (Exception ignored) {}

            return ResponseRoommatePostDto.builder()
                    .id(board.getId())
                    .title(cl.getTitle())
                    .dormPeriod(DormDayUtil.sortDormDays(cl.getDormPeriod()))
                    .dormType(writer.getDormType())
                    .college(writer.getCollege())
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
                    .createDate(board.getCreatedDate())
                    .isMatched(isMatched)
                    .userProfileImageUrl(writerImg)
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<ResponseRoommateSimilarityDto> getSimilarRoommateBoardListScrollForMe(
            HttpServletRequest request,
            Long userId,
            Integer lastPct,
            Long lastBoardId,
            int size
    ) {
        RoommateBoard myBoard = roommateBoardRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        List<RoommateBoard> others = roommateBoardRepository.findAllByIdNot(myBoard.getId());

        // 유사도 계산 + 정렬 (유사도 desc, id desc)
        List<Map.Entry<RoommateBoard, Integer>> ranked = others.stream()
                .map(b -> Map.entry(b, toPercent(calculateSimilarity(myBoard, b))))
                .sorted((e1, e2) -> {
                    int cmp = Integer.compare(e2.getValue(), e1.getValue()); // pct desc
                    if (cmp != 0) return cmp;
                    return Long.compare(e2.getKey().getId(), e1.getKey().getId()); // id desc
                })
                .toList();

        // 커서 스킵
        Stream<Map.Entry<RoommateBoard, Integer>> stream = ranked.stream();
        if (lastPct != null && lastBoardId != null) {
            stream = stream.dropWhile(e -> {
                int pct = e.getValue();
                long id = e.getKey().getId();

                if (pct > lastPct) return true;   // 더 높은 유사도 → 스킵
                if (pct < lastPct) return false;  // 더 낮은 유사도 → 시작
                return id >= lastBoardId;         // 같은 유사도면 id 작은 것부터
            });
        }

        // 페이지 사이즈만큼 가져오기
        List<Map.Entry<RoommateBoard, Integer>> page = stream.limit(size).toList();

        // DTO 변환
        return page.stream().map(entry -> {
            RoommateBoard board = entry.getKey();
            int pct = entry.getValue();
            String img = null;
            try {
                img = imageService.findStaticImageUrl(ImageType.USER, board.getUser().getId(), request);
            } catch (Exception ignored) {}

            RoommateCheckList cl = board.getRoommateCheckList();
            User writer = board.getUser();
            boolean isMatched = isRoommateBoardOwnerMatched(board.getId());

            return ResponseRoommateSimilarityDto.builder()
                    .boardId(board.getId())
                    .title(cl.getTitle())
                    .dormPeriod(DormDayUtil.sortDormDays(cl.getDormPeriod()))
                    .dormType(writer.getDormType())
                    .college(writer.getCollege())
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
                    .similarityPercentage(pct)
                    .roommateBoardLike(board.getRoommateBoardLike())
                    .userId(writer.getId())
                    .userName(writer.getName())
                    .createdDate(board.getCreatedDate())
                    .isMatched(isMatched)
                    .userProfileImageUrl(img)
                    .build();
        }).toList();
    }


    private static int toPercent(double ratio) {
        return (int) Math.round(ratio * 100.0);
    }

    // 필요시 네가 쓰던 12개 항목 비교식 그대로 사용
    private double calculateSimilarity(RoommateBoard a, RoommateBoard b) {
        var ac = a.getRoommateCheckList();
        var bc = b.getRoommateCheckList();
        int matches = 0, total = 12;

        if (ac.getDormType() == bc.getDormType()) matches++;
        if (ac.getCollege() == bc.getCollege()) matches++;
        if (ac.getReligion() == bc.getReligion()) matches++;
        if (ac.getMbti() != null && ac.getMbti().equals(bc.getMbti())) matches++;
        if (ac.getSmoking() == bc.getSmoking()) matches++;
        if (ac.getSnoring() == bc.getSnoring()) matches++;
        if (ac.getToothGrind() == bc.getToothGrind()) matches++;
        if (ac.getSleeper() == bc.getSleeper()) matches++;
        if (ac.getShowerHour() == bc.getShowerHour()) matches++;
        if (ac.getShowerTime() == bc.getShowerTime()) matches++;
        if (ac.getBedTime() == bc.getBedTime()) matches++;
        if (ac.getArrangement() == bc.getArrangement()) matches++;

        return (total == 0) ? 0.0 : (double) matches / total;
    }



}
