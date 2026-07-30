package com.example.appcenter_project.domain.announcement.repository;

import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import com.example.appcenter_project.domain.announcement.enums.ScheduleExtractStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * findScheduleExtractTargets cutoff 완화 검증 (#702).
 *
 * <p>실제 MySQL(Testcontainers) 기반 JPA 슬라이스 테스트. @DataJpaTest라 JPA 빈만 로드하므로
 * FirebaseMessaging 등 풀 컨텍스트 빈이 필요 없다. Docker 필요.
 *
 * <p>쿼리: status IN (PENDING, FAILED) AND retryCount < 3
 *        AND (crawledDate >= cutoff OR status = PENDING) AND id > lastId
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class CrawledAnnouncementRepositoryTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    CrawledAnnouncementRepository crawledAnnouncementRepository;

    private static final List<ScheduleExtractStatus> TARGET_STATUSES =
            List.of(ScheduleExtractStatus.PENDING, ScheduleExtractStatus.FAILED);

    private LocalDate cutoff;

    @BeforeEach
    void setUp() {
        cutoff = LocalDate.now().minusDays(3);

        save("P-OLD", cutoff.minusDays(5), 0);   // PENDING + 오래된 작성일
        save("F-OLD", cutoff.minusDays(5), 1);   // FAILED(retry 1) + 오래된 작성일
        save("F-NEW", LocalDate.now(), 1);       // FAILED(retry 1) + 최근 작성일
        save("F-MAX", LocalDate.now(), 3);       // FAILED(retry 3) + 최근 작성일
        saveSuccess("S-NEW", LocalDate.now());   // SUCCESS + 최근 작성일
    }

    private List<String> queryTargetNumbers() {
        return crawledAnnouncementRepository
                .findScheduleExtractTargets(TARGET_STATUSES, cutoff, 0L, PageRequest.of(0, 50))
                .stream()
                .map(CrawledAnnouncement::getNumber)
                .toList();
    }

    @Test
    @DisplayName("#702: PENDING은 작성일이 cutoff를 지나도 재추출 대상에 포함된다")
    void pending_old_is_included_despite_cutoff() {
        assertThat(queryTargetNumbers()).contains("P-OLD");
    }

    @Test
    @DisplayName("#702: FAILED는 작성일이 cutoff 이전이면 제외된다 (cutoff 유지)")
    void failed_old_is_excluded_by_cutoff() {
        assertThat(queryTargetNumbers()).doesNotContain("F-OLD");
    }

    @Test
    @DisplayName("#702: FAILED는 작성일이 cutoff 이내면 포함된다")
    void failed_recent_is_included() {
        assertThat(queryTargetNumbers()).contains("F-NEW");
    }

    @Test
    @DisplayName("#702: retryCount >= 3 이면 제외된다")
    void failed_over_retry_limit_is_excluded() {
        assertThat(queryTargetNumbers()).doesNotContain("F-MAX");
    }

    @Test
    @DisplayName("#702: SUCCESS 상태는 재추출 대상이 아니다")
    void success_is_excluded() {
        assertThat(queryTargetNumbers()).doesNotContain("S-NEW");
    }

    // ── helpers ──────────────────────────────────────────────
    private void save(String number, LocalDate crawledDate, int failCount) {
        CrawledAnnouncement a = build(number, crawledDate);
        for (int i = 0; i < failCount; i++) {
            a.markFailed("test-error");
        }
        crawledAnnouncementRepository.saveAndFlush(a);
    }

    private void saveSuccess(String number, LocalDate crawledDate) {
        CrawledAnnouncement a = build(number, crawledDate);
        a.markSuccess();
        crawledAnnouncementRepository.saveAndFlush(a);
    }

    private CrawledAnnouncement build(String number, LocalDate crawledDate) {
        return CrawledAnnouncement.builder()
                .category(AnnouncementCategory.from("기타"))
                .number(number)
                .title("제목-" + number)
                .writer("작성자")
                .viewCount(0)
                .announcementType(AnnouncementType.DORMITORY)
                .content("본문")
                .crawledAnnouncementFiles(new ArrayList<>())
                .crawledDate(crawledDate)
                .link("https://dorm.inu.ac.kr/" + number)
                .build();
    }
}