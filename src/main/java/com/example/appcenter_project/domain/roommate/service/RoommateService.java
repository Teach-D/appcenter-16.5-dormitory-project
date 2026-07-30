package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateFormDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateCheckListDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateMatchingStatusDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateSimilarityDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoardLike;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoardRead;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.enums.RoommateMatchingStatus;
import com.example.appcenter_project.domain.roommate.repository.*;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.notification.service.RoommateNotificationService;
import com.example.appcenter_project.global.mixpanel.MixpanelService;
import com.example.appcenter_project.shared.utils.DormDayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class RoommateService {

    private final UserRepository userRepository;
    private final RoommateCheckListRepository roommateCheckListRepository;
    private final RoommateBoardRepository roommateBoardRepository;
    private final RoommateBoardLikeRepository roommateBoardLikeRepository;
    private final RoommateMatchingRepository roommateMatchingRepository;
    private final RoommateChattingRoomRepository roommateChattingRoomRepository;
    private final ImageService imageService;
    private final RoommateNotificationService roommateNotificationService;
    private final MixpanelService mixpanelService;
    private final RoommateBoardReadRepository roommateBoardReadRepository;
    private final RoommateMatchingPeriodResolver periodResolver;

    @Transactional
    public ResponseRoommatePostDto createRoommateCheckListandBoard(RequestRoommateFormDto requestDto, Long userId) {
        // 1. 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        // 2. 학기 계산 (현재 매칭 기간으로 항상 태깅 — null 저장 방지)
        MatchingPeriod period = periodResolver.resolveCurrent(LocalDate.now());
        Integer year = period.year();
        SemesterType semester = period.semester();

        // 3. 체크리스트 생성 + user 설정
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
                .user(user)
                .year(year)
                .semester(semester)
                .build();

        RoommateCheckList savedCheckList = roommateCheckListRepository.save(roommateCheckList);

        // 4. 게시글 생성 및 저장
        RoommateBoard roommateBoard = RoommateBoard.builder()
                .title(requestDto.getTitle())
                .user(user)
                .roommateBoardLike(0)
                .roommateCheckList(savedCheckList)
                .year(year)
                .semester(semester)
                .build();

        RoommateBoard savedBoard = roommateBoardRepository.save(roommateBoard);

        // 5. 알림 전송
        roommateNotificationService.sendFilteredNotifications(savedBoard);

        // 6. 응답
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
                .year(year)
                .semester(semester)
                .build();
    }

    //전체 학기 목록 조회 (최신순)
    @Transactional(readOnly = true)
    public List<ResponseRoommatePostDto> getRoommateBoardList(Long userId, HttpServletRequest request) {
        List<RoommateBoard> boards = roommateBoardRepository.findAllByOrderByCreatedDateDesc();

        if (boards.isEmpty()){
            throw new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND);
        }

        MatchingPeriod current = periodResolver.resolveCurrent(LocalDate.now());
        List<Long> boardIds = boards.stream().map(RoommateBoard::getId).toList();
        Set<Long> readBoardIds = loadReadBoardIds(userId, boardIds);

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
                            .isRead(readBoardIds.contains(board.getId()))
                            .isMyPost(userId != null && writer.getId().equals(userId))
                            .year(board.getYear())
                            .semester(board.getSemester())
                            .isCurrentPeriod(isInCurrentPeriod(board, current))
                            .build();
                })
                .toList();
    }

    //단일 조회 (조회 시 로그인 유저 읽음 처리)
    @Transactional
    public ResponseRoommatePostDto getRoommateBoardDetail(Long boardId, Long userId, jakarta.servlet.http.HttpServletRequest request){
        RoommateBoard board = roommateBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));
        boolean isMatched = isRoommateBoardOwnerMatched(boardId);

        boolean read = (userId != null) && markReadIfAbsent(userId, board);

        String writerImg = null;
        try {
            writerImg = imageService.findStaticImageUrl(ImageType.USER, board.getUser().getId(), request);
        } catch (Exception ignored) {}

        ResponseRoommatePostDto dto = ResponseRoommatePostDto.entityToDto(board, isMatched, writerImg);
        dto.updateIsRead(read);
        return dto;
    }

    private boolean markReadIfAbsent(Long userId, RoommateBoard board) {
        if (roommateBoardReadRepository.existsByUserIdAndRoommateBoardId(userId, board.getId())) {
            return true; // 이미 읽음
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));
        try {
            roommateBoardReadRepository.saveAndFlush(RoommateBoardRead.of(user, board));
        } catch (DataIntegrityViolationException e) {
        }
        return true;
    }

    //유사도 조회 (현재 OPEN 기간만)
    public List<ResponseRoommateSimilarityDto> getSimilarRoommateBoards(Long userId, jakarta.servlet.http.HttpServletRequest request) {
        // 0. 현재 매칭이 CLOSED면 유사도 추천 없음
        MatchingPeriod current = periodResolver.resolveCurrent(LocalDate.now());
        if (current.status() != RoommateMatchingStatus.OPEN) {
            return List.of();
        }

        // 1. 내 체크리스트 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        RoommateBoard myBoard = roommateBoardRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        RoommateCheckList myChecklist = myBoard.getRoommateCheckList();

        // 2. 다른 사람 게시글 가져오기 (현재 OPEN 기간만)
        List<RoommateBoard> allBoards = roommateBoardRepository.findAll();
        List<RoommateBoard> otherBoards = allBoards.stream()
                .filter(b -> !b.getUser().getId().equals(userId))
                .filter(b -> isInCurrentPeriod(b, current))
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

        try {
            mixpanelService.trackEvent(user.getId().toString(), "room_rules_edit", new JSONObject());
        } catch (Exception e) {
            log.warn("Mixpanel room_rules_edit 이벤트 추적 실패 - userId: {}", userId);
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

    // 내가 작성한 이번 학기 체크리스트 내용 조회 (수정 화면 표시용)
    @Transactional(readOnly = true)
    public ResponseRoommateCheckListDto getMyCheckList(Long userId) {
        RoommateCheckList checkList = roommateCheckListRepository.findFirstByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_CHECKLIST_NOT_FOUND));
        return ResponseRoommateCheckListDto.from(checkList);
    }

    // 이전 학기 체크리스트 내용 조회 (현재 학기 제외 가장 최근 과거 데이터)
    @Transactional(readOnly = true)
    public ResponseRoommateCheckListDto getPreviousCheckListContent(Long userId) {
        MatchingPeriod current = periodResolver.resolveCurrent(LocalDate.now());
        RoommateCheckList previous = roommateCheckListRepository.findByUserIdOrderByIdDesc(userId).stream()
                .filter(checkList -> !isSamePeriod(checkList, current))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_CHECKLIST_NOT_FOUND));
        return ResponseRoommateCheckListDto.from(previous);
    }

    private boolean isSamePeriod(RoommateCheckList checkList, MatchingPeriod period) {
        return Objects.equals(checkList.getYear(), period.year())
                && checkList.getSemester() == period.semester();
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

    //전체 학기 스크롤 조회 (id 내림차순)
    @Transactional(readOnly = true)
    public List<ResponseRoommatePostDto> getRoommateBoardListScroll(Long userId, HttpServletRequest request, Long lastId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<RoommateBoard> boards;

        if (lastId == null) {
            boards = roommateBoardRepository.findAllByOrderByIdDesc(pageable);
        } else {
            boards = roommateBoardRepository.findByIdLessThanOrderByIdDesc(lastId, pageable);
        }

        if (boards.isEmpty()) {
            return List.of(); // 마지막 페이지
        }

        MatchingPeriod current = periodResolver.resolveCurrent(LocalDate.now());
        List<Long> boardIds = boards.stream().map(RoommateBoard::getId).toList();
        Set<Long> readBoardIds = loadReadBoardIds(userId, boardIds);

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
                    .isRead(readBoardIds.contains(board.getId()))
                    .isMyPost(userId != null && writer.getId().equals(userId))
                    .year(board.getYear())
                    .semester(board.getSemester())
                    .isCurrentPeriod(isInCurrentPeriod(board, current))
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

        // 현재 매칭이 CLOSED면 유사도 추천 없음
        MatchingPeriod current = periodResolver.resolveCurrent(LocalDate.now());
        if (current.status() != RoommateMatchingStatus.OPEN) {
            return List.of();
        }

        List<RoommateBoard> others = roommateBoardRepository.findAllByIdNot(myBoard.getId()).stream()
                .filter(b -> isInCurrentPeriod(b, current))
                .toList();

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

        List<Long> boardIds = page.stream().map(e -> e.getKey().getId()).toList();
        Set<Long> readBoardIds = loadReadBoardIds(userId, boardIds);

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
                    .isRead(readBoardIds.contains(board.getId()))
                    .build();
        }).toList();
    }

    @Transactional
    public void deleteRoommateBoard(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_USER_NOT_FOUND));

        RoommateBoard board = roommateBoardRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ROOMMATE_DELETE_NOT_ALLOWED);
        }

        RoommateCheckList checkList = board.getRoommateCheckList();

        // 연결된 채팅방의 board/checklist 참조를 null로 처리 (채팅방과 채팅 내역은 유지)
        roommateChattingRoomRepository.detachBoard(board.getId());
        if (checkList != null) {
            roommateChattingRoomRepository.detachGuestChecklist(checkList.getId());
            roommateChattingRoomRepository.detachHostChecklist(checkList.getId());
        }

        // 읽음 기록 삭제 (FK 정합성)
        roommateBoardReadRepository.deleteAllByRoommateBoardId(board.getId());

        // 게시글 삭제 (좋아요는 orphanRemoval로 자동 삭제)
        roommateBoardRepository.delete(board);

        // 체크리스트 삭제
        if (checkList != null) {
            roommateCheckListRepository.delete(checkList);
        }
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

    private Set<Long> loadReadBoardIds(Long userId, List<Long> boardIds) {
        if (userId == null || boardIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(roommateBoardReadRepository.findReadBoardIds(userId, boardIds));
    }

    // 현재 매칭 기간(year+semester)에 속하는 게시글인지 (레거시 null 데이터 제외)
    private boolean isInCurrentPeriod(RoommateBoard board, MatchingPeriod current) {
        return current.semester() == board.getSemester()
                && board.getYear() != null
                && board.getYear() == current.year();
    }

    //현재 매칭 기간 + OPEN/CLOSED 상태 조회
    @Transactional(readOnly = true)
    public ResponseRoommateMatchingStatusDto getMatchingStatus() {
        MatchingPeriod current = periodResolver.resolveCurrent(LocalDate.now());
        return ResponseRoommateMatchingStatusDto.of(current.year(), current.semester(), current.status());
    }

}