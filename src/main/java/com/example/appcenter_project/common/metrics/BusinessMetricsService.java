package com.example.appcenter_project.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 비즈니스 메트릭 수집 서비스
 *
 * Prometheus에서 수집할 수 있는 커스텀 메트릭을 관리합니다.
 * - 사용자 가입/로그인 수
 * - 게시글 작성 수
 * - 채팅 메시지 수
 * - 좋아요/댓글 수
 */
@Slf4j
@Service
public class BusinessMetricsService {

    private final MeterRegistry meterRegistry;

    // User 관련 메트릭
    private final Counter userRegistrationCounter;
    private final Counter userLoginCounter;

    // Content 관련 메트릭
    private final Counter roommatePostCounter;
    private final Counter groupOrderPostCounter;
    private final Counter tipPostCounter;
    private final Counter complaintCounter;

    // Engagement 관련 메트릭
    private final Counter likeCounter;
    private final Counter commentCounter;
    private final Counter chatMessageCounter;

    // Performance 관련 메트릭
    private final Timer crawlingTimer;

    // API Call 관련 메트릭
    private final Counter announcementFindCounter;
    private final Counter complaintSaveCounter;
    private final Counter roommateChattingSendCounter;
    private final Counter surveyFindCounter;
    private final Counter tipFindCounter;
    private final Counter userSaveCounter;
    private final Counter roommateSaveCounter;
    private final Counter roommateRequestCounter;

    public BusinessMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // User 메트릭 초기화
        this.userRegistrationCounter = Counter.builder("user_registration_total")
                .description("Total number of user registrations")
                .register(meterRegistry);

        this.userLoginCounter = Counter.builder("user_login_total")
                .description("Total number of user logins")
                .tag("status", "success")
                .register(meterRegistry);

        // Content 메트릭 초기화
        this.roommatePostCounter = Counter.builder("post_created_total")
                .description("Total number of posts created")
                .tag("type", "roommate")
                .register(meterRegistry);

        this.groupOrderPostCounter = Counter.builder("post_created_total")
                .description("Total number of posts created")
                .tag("type", "group_order")
                .register(meterRegistry);

        this.tipPostCounter = Counter.builder("post_created_total")
                .description("Total number of posts created")
                .tag("type", "tip")
                .register(meterRegistry);

        this.complaintCounter = Counter.builder("complaint_created_total")
                .description("Total number of complaints created")
                .register(meterRegistry);

        // Engagement 메트릭 초기화
        this.likeCounter = Counter.builder("engagement_total")
                .description("Total number of engagements")
                .tag("type", "like")
                .register(meterRegistry);

        this.commentCounter = Counter.builder("engagement_total")
                .description("Total number of engagements")
                .tag("type", "comment")
                .register(meterRegistry);

        this.chatMessageCounter = Counter.builder("chat_message_total")
                .description("Total number of chat messages")
                .register(meterRegistry);

        // Performance 메트릭 초기화
        this.crawlingTimer = Timer.builder("crawling_duration_seconds")
                .description("Time taken to crawl announcements")
                .register(meterRegistry);

        // API Call 메트릭 초기화
        this.announcementFindCounter = Counter.builder("announcement_find_total")
                .description("Total number of announcement find API calls")
                .register(meterRegistry);

        this.complaintSaveCounter = Counter.builder("complaint_save_total")
                .description("Total number of complaint save API calls")
                .register(meterRegistry);

        this.roommateChattingSendCounter = Counter.builder("roommate_chatting_send_total")
                .description("Total number of roommate chatting send API calls")
                .register(meterRegistry);

        this.surveyFindCounter = Counter.builder("survey_find_total")
                .description("Total number of survey find API calls")
                .register(meterRegistry);

        this.tipFindCounter = Counter.builder("tip_find_total")
                .description("Total number of tip find API calls")
                .register(meterRegistry);

        this.userSaveCounter = Counter.builder("user_save_total")
                .description("Total number of user save API calls")
                .register(meterRegistry);

        this.roommateSaveCounter = Counter.builder("roommate_save_total")
                .description("Total number of roommate save API calls")
                .register(meterRegistry);

        this.roommateRequestCounter = Counter.builder("roommate_request_total")
                .description("Total number of roommate request API calls")
                .register(meterRegistry);
    }

    // ==================== User Metrics ====================

    /**
     * 사용자 가입 메트릭 증가
     */
    public void incrementUserRegistration() {
        userRegistrationCounter.increment();
        log.debug("User registration metric incremented");
    }

    /**
     * 사용자 로그인 메트릭 증가
     */
    public void incrementUserLogin(boolean success) {
        if (success) {
            userLoginCounter.increment();
            log.debug("User login metric incremented");
        } else {
            Counter.builder("user_login_total")
                    .description("Total number of user login attempts")
                    .tag("status", "failed")
                    .register(meterRegistry)
                    .increment();
        }
    }

    // ==================== Content Metrics ====================

    /**
     * 룸메이트 게시글 작성 메트릭 증가
     */
    public void incrementRoommatePost() {
        roommatePostCounter.increment();
        log.debug("Roommate post metric incremented");
    }

    /**
     * 공동구매 게시글 작성 메트릭 증가
     */
    public void incrementGroupOrderPost() {
        groupOrderPostCounter.increment();
        log.debug("Group order post metric incremented");
    }

    /**
     * 꿀팁 게시글 작성 메트릭 증가
     */
    public void incrementTipPost() {
        tipPostCounter.increment();
        log.debug("Tip post metric incremented");
    }

    /**
     * 민원 제기 메트릭 증가
     */
    public void incrementComplaint() {
        complaintCounter.increment();
        log.debug("Complaint metric incremented");
    }

    // ==================== Engagement Metrics ====================

    /**
     * 좋아요 메트릭 증가
     */
    public void incrementLike() {
        likeCounter.increment();
        log.debug("Like metric incremented");
    }

    /**
     * 댓글 메트릭 증가
     */
    public void incrementComment() {
        commentCounter.increment();
        log.debug("Comment metric incremented");
    }

    /**
     * 채팅 메시지 메트릭 증가
     */
    public void incrementChatMessage() {
        chatMessageCounter.increment();
        log.debug("Chat message metric incremented");
    }

    // ==================== Performance Metrics ====================

    /**
     * 크롤링 소요 시간 기록
     *
     * @param durationMillis 크롤링에 소요된 시간 (밀리초)
     */
    public void recordCrawlingDuration(long durationMillis) {
        crawlingTimer.record(durationMillis, TimeUnit.MILLISECONDS);
        log.debug("Crawling duration recorded: {}ms", durationMillis);
    }

    /**
     * 크롤링 실행을 Timer로 감싸서 자동으로 시간 측정
     *
     * @param runnable 크롤링 작업
     */
    public void recordCrawling(Runnable runnable) {
        crawlingTimer.record(runnable);
    }
}