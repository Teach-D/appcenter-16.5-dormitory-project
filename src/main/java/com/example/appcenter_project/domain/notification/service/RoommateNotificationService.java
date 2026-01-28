package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseFilterTestDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.entity.RoommateNotificationFilter;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateNotificationFilterRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoommateNotificationService {

    private final RoommateNotificationFilterRepository filterRepository;
    private final RoommateBoardRepository boardRepository;
    private final FcmMessageService fcmMessageService;
    private final NotificationService notificationService;

    public void sendFilteredNotifications(RoommateBoard board) {
        // 1. 게시글 작성자와 RoommateCheckList 조회 (LAZY 로딩을 위해 트랜잭션 내에서 조회)
        User boardAuthor = board.getUser();
        RoommateCheckList checkList = board.getRoommateCheckList();

        // RoommateCheckList가 없으면 알림 전송 불가
        if (checkList == null) {
            log.info("RoommateCheckList가 없어 알림을 전송하지 않습니다. boardId: {}", board.getId());
            return;
        }

        Long boardAuthorId = boardAuthor.getId();

        // 2. 필터가 설정된 모든 사용자 조회 (JOIN FETCH로 N+1 문제 해결)
        // User와 receiveNotificationTypes를 함께 조회
        List<RoommateNotificationFilter> filters = filterRepository.findAllWithUser();
        
        // 3. 필터링 조건에 맞는 사용자 찾기
        List<User> targetUsers = new ArrayList<>();
        log.info("필터링 시작. 총 필터 수: {}, boardId: {}", filters.size(), board.getId());
        
        for (RoommateNotificationFilter filter : filters) {
            User filterUser = filter.getUser();
            
            // 게시글 작성자 본인은 제외
            if (filterUser.getId().equals(boardAuthorId)) {
                log.debug("게시글 작성자 본인 제외. userId: {}", filterUser.getId());
                continue;
            }

            // ROOMMATE 알림을 받지 않도록 설정한 사용자는 제외
            if (!filterUser.getReceiveNotificationTypes().contains(NotificationType.ROOMMATE)) {
                log.debug("ROOMMATE 알림 수신 비활성화 사용자 제외. userId: {}", filterUser.getId());
                continue;
            }

            // 필터 조건 체크
            if (matchesFilter(filter, checkList, board.getId(), filterUser.getId())) {
                targetUsers.add(filterUser);
                log.info("필터 조건 일치! userId: {}, boardId: {}", filterUser.getId(), board.getId());
            } else {
                log.debug("필터 조건 불일치. userId: {}, boardId: {}", filterUser.getId(), board.getId());
            }
        }
        
        log.info("필터링 완료. 일치하는 사용자 수: {}명, boardId: {}", targetUsers.size(), board.getId());

        if (targetUsers.isEmpty()) {
            log.info("필터링 조건에 맞는 사용자가 없습니다. boardId: {}", board.getId());
            return;
        }

        // 4. 알림 생성
        Notification notification = notificationService.createRoommateBoardNotification(
                boardAuthor.getName(),
                board.getId()
        );

        // 5. UserNotification 저장 및 FCM 전송
        // 기존 코드 스타일: QuickMessageService와 동일한 패턴 사용
        for (User targetUser : targetUsers) {
            // UserNotification 저장
            notificationService.createUserNotification(targetUser, notification);
            
            // FCM 전송 (기존 QuickMessageService와 동일한 예외 처리 방식)
            try {
                fcmMessageService.sendNotification(targetUser, notification.getTitle(), notification.getBody());
            } catch (Exception e) {
                log.error("FCM 전송 실패. userId: {}, boardId: {}, error: {}", 
                        targetUser.getId(), board.getId(), e.getMessage());
                // UserNotification은 이미 저장되었으므로 계속 진행
            }
        }

        log.info("룸메이트 게시글 알림 전송 완료. boardId: {}, 전송 대상: {}명", board.getId(), targetUsers.size());
    }

    private boolean matchesFilter(RoommateNotificationFilter filter, RoommateCheckList checkList, Long boardId, Long userId) {
        // 기본 정보 필터링
        if (filter.getDormType() != null && !filter.getDormType().equals(checkList.getDormType())) {
            log.debug("필터 불일치: dormType. filter: {}, checkList: {}, userId: {}, boardId: {}", 
                    filter.getDormType(), checkList.getDormType(), userId, boardId);
            return false;
        }

        if (filter.getDormPeriodDays() != null && !filter.getDormPeriodDays().isEmpty()) {
            Set<com.example.appcenter_project.domain.roommate.enums.DormDay> checkListDormPeriod = checkList.getDormPeriod();
            if (checkListDormPeriod == null || checkListDormPeriod.isEmpty()) {
                log.debug("필터 불일치: dormPeriodDays (게시글에 상주기간 없음). userId: {}, boardId: {}", userId, boardId);
                return false;
            }
            // 필터에서 선택한 날짜는 비상주 기간이므로, 교집합이 없어야 함
            // 예: 필터 ["월", "화"] 선택 = 월, 화에 비상주 = 수, 목, 금, 토, 일에 상주하는 사람의 글만 알림
            // 게시글 작성자의 상주기간과 필터의 비상주기간이 겹치면 안 됨
            boolean hasIntersection = checkListDormPeriod.stream()
                    .anyMatch(filter.getDormPeriodDays()::contains);
            if (hasIntersection) {
                log.debug("필터 불일치: dormPeriodDays (교집합 있음). filter: {}, checkList: {}, userId: {}, boardId: {}", 
                        filter.getDormPeriodDays(), checkListDormPeriod, userId, boardId);
                return false;
            }
        }

        if (filter.getColleges() != null && !filter.getColleges().isEmpty()) {
            if (checkList.getCollege() == null || !filter.getColleges().contains(checkList.getCollege())) {
                log.debug("필터 불일치: colleges. filter: {}, checkList: {}, userId: {}, boardId: {}", 
                        filter.getColleges(), checkList.getCollege(), userId, boardId);
                return false;
            }
        }

        // 생활 습관 필터링
        if (filter.getSmoking() != null && !filter.getSmoking().equals(checkList.getSmoking())) {
            log.debug("필터 불일치: smoking. filter: {}, checkList: {}, userId: {}, boardId: {}", 
                    filter.getSmoking(), checkList.getSmoking(), userId, boardId);
            return false;
        }

        if (filter.getSnoring() != null && !filter.getSnoring().equals(checkList.getSnoring())) {
            log.debug("필터 불일치: snoring. userId: {}, boardId: {}", userId, boardId);
            return false;
        }

        if (filter.getToothGrind() != null && !filter.getToothGrind().equals(checkList.getToothGrind())) {
            log.debug("필터 불일치: toothGrind. userId: {}, boardId: {}", userId, boardId);
            return false;
        }

        if (filter.getSleeper() != null && !filter.getSleeper().equals(checkList.getSleeper())) {
            log.debug("필터 불일치: sleeper. userId: {}, boardId: {}", userId, boardId);
            return false;
        }

        // 생활 리듬 필터링
        if (filter.getShowerHour() != null && !filter.getShowerHour().equals(checkList.getShowerHour())) {
            log.debug("필터 불일치: showerHour. userId: {}, boardId: {}", userId, boardId);
            return false;
        }

        if (filter.getShowerTime() != null && !filter.getShowerTime().equals(checkList.getShowerTime())) {
            log.debug("필터 불일치: showerTime. userId: {}, boardId: {}", userId, boardId);
            return false;
        }

        if (filter.getBedTime() != null && !filter.getBedTime().equals(checkList.getBedTime())) {
            log.debug("필터 불일치: bedTime. userId: {}, boardId: {}", userId, boardId);
            return false;
        }

        // 성향 필터링
        if (filter.getArrangement() != null && !filter.getArrangement().equals(checkList.getArrangement())) {
            log.debug("필터 불일치: arrangement. userId: {}, boardId: {}", userId, boardId);
            return false;
        }

        if (filter.getReligions() != null && !filter.getReligions().isEmpty()) {
            if (checkList.getReligion() == null || !filter.getReligions().contains(checkList.getReligion())) {
                log.debug("필터 불일치: religions. filter: {}, checkList: {}, userId: {}, boardId: {}", 
                        filter.getReligions(), checkList.getReligion(), userId, boardId);
                return false;
            }
        }

        log.debug("모든 필터 조건 일치! userId: {}, boardId: {}", userId, boardId);
        return true;
    }

    /**
     * 테스트용: 특정 게시글에 대해 필터링 결과를 확인하는 메서드 (알림 전송 없음)
     */
    @Transactional(readOnly = true)
    public ResponseFilterTestDto testFiltering(Long boardId) {
        RoommateBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. boardId: " + boardId));

        User boardAuthor = board.getUser();
        RoommateCheckList checkList = board.getRoommateCheckList();

        if (checkList == null) {
            return ResponseFilterTestDto.builder()
                    .boardId(boardId)
                    .boardTitle(board.getTitle())
                    .boardAuthorId(boardAuthor.getId())
                    .boardAuthorName(boardAuthor.getName())
                    .totalFilters(0)
                    .matchedUsers(0)
                    .matchedUserList(new ArrayList<>())
                    .filteredOutUserList(new ArrayList<>())
                    .build();
        }

        Long boardAuthorId = boardAuthor.getId();
        List<RoommateNotificationFilter> filters = filterRepository.findAllWithUser();

        List<ResponseFilterTestDto.MatchedUserInfo> matchedUsers = new ArrayList<>();
        List<ResponseFilterTestDto.FilteredOutUserInfo> filteredOutUsers = new ArrayList<>();

        for (RoommateNotificationFilter filter : filters) {
            User filterUser = filter.getUser();

            // 게시글 작성자 본인은 제외
            if (filterUser.getId().equals(boardAuthorId)) {
                filteredOutUsers.add(ResponseFilterTestDto.FilteredOutUserInfo.builder()
                        .userId(filterUser.getId())
                        .userName(filterUser.getName())
                        .reason("게시글 작성자 본인")
                        .build());
                continue;
            }

            // ROOMMATE 알림을 받지 않도록 설정한 사용자는 제외
            if (!filterUser.getReceiveNotificationTypes().contains(NotificationType.ROOMMATE)) {
                filteredOutUsers.add(ResponseFilterTestDto.FilteredOutUserInfo.builder()
                        .userId(filterUser.getId())
                        .userName(filterUser.getName())
                        .reason("ROOMMATE 알림 수신 비활성화")
                        .build());
                continue;
            }

            // 필터 조건 체크
            String mismatchReason = getMismatchReason(filter, checkList);
            if (mismatchReason == null) {
                matchedUsers.add(ResponseFilterTestDto.MatchedUserInfo.builder()
                        .userId(filterUser.getId())
                        .userName(filterUser.getName())
                        .reason("모든 필터 조건 일치")
                        .build());
            } else {
                filteredOutUsers.add(ResponseFilterTestDto.FilteredOutUserInfo.builder()
                        .userId(filterUser.getId())
                        .userName(filterUser.getName())
                        .reason(mismatchReason)
                        .build());
            }
        }

        return ResponseFilterTestDto.builder()
                .boardId(boardId)
                .boardTitle(board.getTitle())
                .boardAuthorId(boardAuthorId)
                .boardAuthorName(boardAuthor.getName())
                .totalFilters(filters.size())
                .matchedUsers(matchedUsers.size())
                .matchedUserList(matchedUsers)
                .filteredOutUserList(filteredOutUsers)
                .build();
    }

    private String getMismatchReason(RoommateNotificationFilter filter, RoommateCheckList checkList) {
        if (filter.getDormType() != null && !filter.getDormType().equals(checkList.getDormType())) {
            return "dormType 불일치 (필터: " + filter.getDormType() + ", 게시글: " + checkList.getDormType() + ")";
        }

        if (filter.getDormPeriodDays() != null && !filter.getDormPeriodDays().isEmpty()) {
            Set<com.example.appcenter_project.domain.roommate.enums.DormDay> checkListDormPeriod = checkList.getDormPeriod();
            if (checkListDormPeriod == null || checkListDormPeriod.isEmpty()) {
                return "dormPeriodDays 불일치 (게시글에 상주기간 없음)";
            }
            // 필터에서 선택한 날짜는 비상주 기간이므로, 교집합이 없어야 함
            // 예: 필터 ["월", "화"] 선택 = 월, 화에 비상주 = 수, 목, 금, 토, 일에 상주하는 사람의 글만 알림
            boolean hasIntersection = checkListDormPeriod.stream()
                    .anyMatch(filter.getDormPeriodDays()::contains);
            if (hasIntersection) {
                return "dormPeriodDays 불일치 (교집합 있음, 필터 비상주기간: " + filter.getDormPeriodDays() + ", 게시글 상주기간: " + checkListDormPeriod + ")";
            }
        }

        if (filter.getColleges() != null && !filter.getColleges().isEmpty()) {
            if (checkList.getCollege() == null || !filter.getColleges().contains(checkList.getCollege())) {
                return "colleges 불일치 (필터: " + filter.getColleges() + ", 게시글: " + checkList.getCollege() + ")";
            }
        }

        if (filter.getSmoking() != null && !filter.getSmoking().equals(checkList.getSmoking())) {
            return "smoking 불일치 (필터: " + filter.getSmoking() + ", 게시글: " + checkList.getSmoking() + ")";
        }

        if (filter.getSnoring() != null && !filter.getSnoring().equals(checkList.getSnoring())) {
            return "snoring 불일치";
        }

        if (filter.getToothGrind() != null && !filter.getToothGrind().equals(checkList.getToothGrind())) {
            return "toothGrind 불일치";
        }

        if (filter.getSleeper() != null && !filter.getSleeper().equals(checkList.getSleeper())) {
            return "sleeper 불일치";
        }

        if (filter.getShowerHour() != null && !filter.getShowerHour().equals(checkList.getShowerHour())) {
            return "showerHour 불일치";
        }

        if (filter.getShowerTime() != null && !filter.getShowerTime().equals(checkList.getShowerTime())) {
            return "showerTime 불일치";
        }

        if (filter.getBedTime() != null && !filter.getBedTime().equals(checkList.getBedTime())) {
            return "bedTime 불일치";
        }

        if (filter.getArrangement() != null && !filter.getArrangement().equals(checkList.getArrangement())) {
            return "arrangement 불일치";
        }

        if (filter.getReligions() != null && !filter.getReligions().isEmpty()) {
            if (checkList.getReligion() == null || !filter.getReligions().contains(checkList.getReligion())) {
                return "religions 불일치 (필터: " + filter.getReligions() + ", 게시글: " + checkList.getReligion() + ")";
            }
        }

        return null; // 모든 조건 일치
    }
}

