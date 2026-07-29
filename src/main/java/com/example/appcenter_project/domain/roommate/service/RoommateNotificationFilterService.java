package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateNotificationFilterDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseFilteredRoommatePostDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateNotificationFilterDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.entity.RoommateNotificationFilter;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardReadRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateNotificationFilterRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.shared.utils.DormDayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.appcenter_project.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoommateNotificationFilterService {

    private final RoommateNotificationFilterRepository filterRepository;
    private final RoommateBoardRepository boardRepository;
    private final RoommateMatchingRepository roommateMatchingRepository;
    private final UserRepository userRepository;
    private final RoommateBoardReadRepository roommateBoardReadRepository;
    private final ImageService imageService;

    public void saveOrUpdateFilter(Long userId, RequestRoommateNotificationFilterDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        RoommateNotificationFilter filter = filterRepository.findByUserId(userId).orElse(null);

        if (filter == null) {
            // 새로 생성
            filter = RoommateNotificationFilter.builder()
                    .user(user)
                    .dormType(dto.getDormType())
                    .dormPeriodDays(dto.getDormPeriodDays())
                    .colleges(dto.getColleges())
                    .smoking(dto.getSmoking())
                    .snoring(dto.getSnoring())
                    .toothGrind(dto.getToothGrind())
                    .sleeper(dto.getSleeper())
                    .showerHour(dto.getShowerHour())
                    .showerTime(dto.getShowerTime())
                    .bedTime(dto.getBedTime())
                    .arrangement(dto.getArrangement())
                    .religions(dto.getReligions())
                    .build();
            filterRepository.save(filter);
        } else {
            // 기존 필터 업데이트
            filter.update(
                    dto.getDormType(),
                    dto.getDormPeriodDays(),
                    dto.getColleges(),
                    dto.getSmoking(),
                    dto.getSnoring(),
                    dto.getToothGrind(),
                    dto.getSleeper(),
                    dto.getShowerHour(),
                    dto.getShowerTime(),
                    dto.getBedTime(),
                    dto.getArrangement(),
                    dto.getReligions()
            );
        }
    }

    @Transactional(readOnly = true)
    public ResponseRoommateNotificationFilterDto getFilter(Long userId) {
        // User 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new CustomException(USER_NOT_FOUND);
        }

        RoommateNotificationFilter filter = filterRepository.findByUserId(userId).orElse(null);
        return ResponseRoommateNotificationFilterDto.from(filter);
    }

    public void deleteFilter(Long userId) {
        // User 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new CustomException(USER_NOT_FOUND);
        }

        RoommateNotificationFilter filter = filterRepository.findByUserId(userId).orElse(null);
        if (filter != null) {
            filterRepository.delete(filter);
        }
    }

    @Transactional(readOnly = true)
    public List<ResponseFilteredRoommatePostDto> getFilteredBoards(Long userId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        RoommateNotificationFilter filter = filterRepository.findByUserId(userId).orElse(null);
        if (filter == null) {
            log.warn("필터가 설정되지 않았습니다. userId: {}", userId);
            return List.of();
        }
        log.info("필터 조회 성공. userId: {}, dormType: {}, dormPeriodDays: {}, colleges: {}",
                userId, filter.getDormType(), filter.getDormPeriodDays(), filter.getColleges());

        List<RoommateBoard> allBoards = boardRepository.findAllWithCheckListAndUserOrderByCreatedDateDesc();
        log.info("필터링 시작. 총 게시글 수: {}, 필터 설정 사용자 ID: {}", allBoards.size(), userId);

        // 필터 조건에 맞는 게시글만 추출
        List<RoommateBoard> matchedBoards = allBoards.stream()
                .filter(board -> {
                    if (board.getUser().getId().equals(userId)) {
                        log.debug("본인 게시글 제외. boardId: {}, userId: {}", board.getId(), userId);
                        return false;
                    }
                    RoommateCheckList checkList = board.getRoommateCheckList();
                    if (checkList == null) {
                        log.debug("RoommateCheckList 없음. boardId: {}", board.getId());
                        return false;
                    }
                    User writer = board.getUser();
                    boolean matches = matchesFilter(filter, checkList, writer, board.getId());
                    if (!matches) {
                        log.debug("필터 조건 불일치. boardId: {}, writerId: {}", board.getId(), writer.getId());
                    } else {
                        log.info("✅ 필터 조건 일치! boardId: {}, writerId: {}, writerName: {}",
                                board.getId(), writer.getId(), writer.getName());
                    }
                    return matches;
                })
                .toList();

        // 로그인 유저가 읽은 게시글 id (매칭된 범위로 한정)
        List<Long> matchedIds = matchedBoards.stream().map(RoommateBoard::getId).toList();
        Set<Long> readBoardIds = matchedIds.isEmpty()
                ? Set.of()
                : new HashSet<>(roommateBoardReadRepository.findReadBoardIds(userId, matchedIds));

        List<ResponseFilteredRoommatePostDto> result = matchedBoards.stream()
                .map(board -> {
                    RoommateCheckList cl = board.getRoommateCheckList();
                    User writer = board.getUser();

                    boolean isMatched = roommateMatchingRepository.existsBySenderAndStatus(writer, MatchingStatus.COMPLETED)
                            || roommateMatchingRepository.existsByReceiverAndStatus(writer, MatchingStatus.COMPLETED);

                    String writerImg = null;
                    try {
                        writerImg = imageService.findStaticImageUrl(ImageType.USER, writer.getId(), request);
                    } catch (Exception ignored) {}

                    ResponseRoommatePostDto post = ResponseRoommatePostDto.builder()
                            .id(board.getId())
                            .title(cl.getTitle())
                            .dormPeriod(DormDayUtil.sortDormDays(cl.getDormPeriod()))
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
                            .createDate(board.getCreatedDate())
                            .isMatched(isMatched)
                            .userProfileImageUrl(writerImg)
                            .isRead(readBoardIds.contains(board.getId()))
                            .build();

                    return ResponseFilteredRoommatePostDto.builder()
                            .post(post)
                            .matchedFilterFields(getMatchedFields(filter, cl))
                            .build();
                })
                .toList();

        log.info("필터링 완료. 일치하는 게시글 수: {}개, 필터 설정 사용자 ID: {}", result.size(), userId);
        return result;
    }

    private boolean matchesFilter(RoommateNotificationFilter filter, RoommateCheckList checkList, User writer, Long boardId) {
        // 기본 정보 필터링
        // dormType과 college는 RoommateCheckList에 저장된 값을 사용 (게시글 작성 시 저장된 값)
        if (filter.getDormType() != null && !filter.getDormType().equals(checkList.getDormType())) {
            log.debug("필터 불일치: dormType. filter: {}, checkList: {}, boardId: {}", 
                    filter.getDormType(), checkList.getDormType(), boardId);
            return false;
        }

        if (filter.getDormPeriodDays() != null && !filter.getDormPeriodDays().isEmpty()) {
            Set<com.example.appcenter_project.domain.roommate.enums.DormDay> checkListDormPeriod = checkList.getDormPeriod();
            if (checkListDormPeriod == null || checkListDormPeriod.isEmpty()) {
                log.debug("필터 불일치: dormPeriodDays (게시글에 상주기간 없음). boardId: {}", boardId);
                return false;
            }
            // 필터에서 선택한 날짜는 비상주 기간이므로, 교집합이 없어야 함
            // 예: 필터 ["월"] 선택 = 월에 비상주 = 화, 수, 목, 금, 토, 일에 상주하는 사람의 글만 알림
            // 게시글 작성자의 상주기간 ["화"]와 필터의 비상주기간 ["월"]은 교집합 없음 → 통과
            boolean hasIntersection = checkListDormPeriod.stream()
                    .anyMatch(filter.getDormPeriodDays()::contains);
            if (hasIntersection) {
                log.debug("필터 불일치: dormPeriodDays (교집합 있음). filter 비상주: {}, 게시글 상주: {}, boardId: {}", 
                        filter.getDormPeriodDays(), checkListDormPeriod, boardId);
                return false;
            }
        }

        if (filter.getColleges() != null && !filter.getColleges().isEmpty()) {
            if (checkList.getCollege() == null || !filter.getColleges().contains(checkList.getCollege())) {
                log.debug("필터 불일치: colleges. filter: {}, checkList: {}, boardId: {}", 
                        filter.getColleges(), checkList.getCollege(), boardId);
                return false;
            }
        }

        // 생활 습관 필터링
        if (filter.getSmoking() != null && !filter.getSmoking().equals(checkList.getSmoking())) {
            log.debug("필터 불일치: smoking. filter: {}, checkList: {}, boardId: {}", 
                    filter.getSmoking(), checkList.getSmoking(), boardId);
            return false;
        }

        if (filter.getSnoring() != null && !filter.getSnoring().equals(checkList.getSnoring())) {
            log.debug("필터 불일치: snoring. boardId: {}", boardId);
            return false;
        }

        if (filter.getToothGrind() != null && !filter.getToothGrind().equals(checkList.getToothGrind())) {
            log.debug("필터 불일치: toothGrind. boardId: {}", boardId);
            return false;
        }

        if (filter.getSleeper() != null && !filter.getSleeper().equals(checkList.getSleeper())) {
            log.debug("필터 불일치: sleeper. boardId: {}", boardId);
            return false;
        }

        // 생활 리듬 필터링
        if (filter.getShowerHour() != null && !filter.getShowerHour().equals(checkList.getShowerHour())) {
            log.debug("필터 불일치: showerHour. boardId: {}", boardId);
            return false;
        }

        if (filter.getShowerTime() != null && !filter.getShowerTime().equals(checkList.getShowerTime())) {
            log.debug("필터 불일치: showerTime. boardId: {}", boardId);
            return false;
        }

        if (filter.getBedTime() != null && !filter.getBedTime().equals(checkList.getBedTime())) {
            log.debug("필터 불일치: bedTime. boardId: {}", boardId);
            return false;
        }

        // 성향 필터링
        if (filter.getArrangement() != null && !filter.getArrangement().equals(checkList.getArrangement())) {
            log.debug("필터 불일치: arrangement. boardId: {}", boardId);
            return false;
        }

        if (filter.getReligions() != null && !filter.getReligions().isEmpty()) {
            if (checkList.getReligion() == null || !filter.getReligions().contains(checkList.getReligion())) {
                log.debug("필터 불일치: religions. filter: {}, checkList: {}, boardId: {}", 
                        filter.getReligions(), checkList.getReligion(), boardId);
                return false;
            }
        }

        log.debug("모든 필터 조건 일치! boardId: {}", boardId);
        return true;
    }

    private List<String> getMatchedFields(RoommateNotificationFilter filter, RoommateCheckList checkList) {
        List<String> matched = new ArrayList<>();

        if (filter.getDormType() != null && filter.getDormType().equals(checkList.getDormType())) {
            matched.add("dormType");
        }
        if (filter.getDormPeriodDays() != null && !filter.getDormPeriodDays().isEmpty()) {
            Set<com.example.appcenter_project.domain.roommate.enums.DormDay> checkListDormPeriod = checkList.getDormPeriod();
            if (checkListDormPeriod != null && !checkListDormPeriod.isEmpty()) {
                boolean hasIntersection = checkListDormPeriod.stream()
                        .anyMatch(filter.getDormPeriodDays()::contains);
                if (!hasIntersection) {
                    matched.add("dormPeriodDays");
                }
            }
        }
        if (filter.getColleges() != null && !filter.getColleges().isEmpty()
                && checkList.getCollege() != null && filter.getColleges().contains(checkList.getCollege())) {
            matched.add("colleges");
        }
        if (filter.getSmoking() != null && filter.getSmoking().equals(checkList.getSmoking())) {
            matched.add("smoking");
        }
        if (filter.getSnoring() != null && filter.getSnoring().equals(checkList.getSnoring())) {
            matched.add("snoring");
        }
        if (filter.getToothGrind() != null && filter.getToothGrind().equals(checkList.getToothGrind())) {
            matched.add("toothGrind");
        }
        if (filter.getSleeper() != null && filter.getSleeper().equals(checkList.getSleeper())) {
            matched.add("sleeper");
        }
        if (filter.getShowerHour() != null && filter.getShowerHour().equals(checkList.getShowerHour())) {
            matched.add("showerHour");
        }
        if (filter.getShowerTime() != null && filter.getShowerTime().equals(checkList.getShowerTime())) {
            matched.add("showerTime");
        }
        if (filter.getBedTime() != null && filter.getBedTime().equals(checkList.getBedTime())) {
            matched.add("bedTime");
        }
        if (filter.getArrangement() != null && filter.getArrangement().equals(checkList.getArrangement())) {
            matched.add("arrangement");
        }
        if (filter.getReligions() != null && !filter.getReligions().isEmpty()
                && checkList.getReligion() != null && filter.getReligions().contains(checkList.getReligion())) {
            matched.add("religions");
        }

        return matched;
    }
}

