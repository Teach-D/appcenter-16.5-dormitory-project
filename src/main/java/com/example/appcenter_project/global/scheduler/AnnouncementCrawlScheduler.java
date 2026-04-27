package com.example.appcenter_project.global.scheduler;

// ... (기존 import 구문 유지) ...
import com.example.appcenter_project.common.file.entity.CrawledAnnouncementFile;
import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.shared.enums.ApiType;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.announcement.repository.CrawledAnnouncementRepository;
import com.example.appcenter_project.common.file.repository.CrawledAnnouncementFileRepository;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementCrawlScheduler {

    private static final String GENERAL_NOTICE_BASE_URL = "https://dorm.inu.ac.kr/dorm/6528/subview.do";
    private static final String DORMITORY_MOVE_BASE_URL = "https://dorm.inu.ac.kr/dorm/6521/subview.do";

    private final CrawledAnnouncementRepository crawledAnnouncementRepository;
    private final CrawledAnnouncementFileRepository crawledAnnouncementFileRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final FcmMessageService fcmMessageService;

    @Scheduled(cron = "0 0 9,14,18 * * ?")
    public void crawling() {
        WebDriver driver = null; // 드라이버 선언

        try {
            // 1. WebDriver 생성 (단 한 번)
            ChromeOptions options = createChromeOptions(); // 옵션 설정
            driver = new ChromeDriver(options);
            log.info("WebDriver 인스턴스를 성공적으로 생성했습니다.");

            // 2. 목록 크롤링 (드라이버 재활용)
            List<Map<String, String>> crawlGeneralNoticesLinks = crawlWithSeleniumNotices(driver, GENERAL_NOTICE_BASE_URL);
            List<Map<String, String>> crawlDormitoryMoveNoticesLinks = crawlWithSeleniumNotices(driver, DORMITORY_MOVE_BASE_URL);

            // 입퇴사 공지 링크 Set 생성
            Set<String> dormitoryMoveLinks = crawlDormitoryMoveNoticesLinks.stream()
                    .map(map -> map.keySet().stream().findFirst().orElse(""))
                    .collect(Collectors.toSet());

            List<Map<String, String>> allNotices = new ArrayList<>();
            allNotices.addAll(crawlGeneralNoticesLinks);
            allNotices.addAll(crawlDormitoryMoveNoticesLinks);

            // value(date) 기준으로 정렬 (최신순)
            allNotices.sort((map1, map2) -> {
                String date1 = map1.values().stream().findFirst().orElse("");
                String date2 = map2.values().stream().findFirst().orElse("");
                return date1.compareTo(date2); // 오름차순
            });

            List<String> links = allNotices.stream()
                    .map(map -> map.keySet().stream().findFirst().orElse("")) // key(link) 추출
                    .collect(Collectors.toList());

            // 3. 개별 공지사항 상세 저장 (드라이버 재활용)
            saveCrawlAnnouncements(driver, links, dormitoryMoveLinks);

        } catch (Exception e) {
            log.error("전체 크롤링 작업 중 치명적인 오류 발생: {}", e.getMessage(), e);
        } finally {
            // 4. WebDriver 종료 (작업 완료 후 단 한 번)
            if (driver != null) {
                driver.quit();
                log.info("WebDriver 인스턴스를 성공적으로 종료했습니다.");
            }
        }
    }

    private ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--disable-extensions");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        options.addArguments("--disable-background-networking");
        options.addArguments("--disable-default-apps");
        options.addArguments("--no-first-run");
        options.addArguments("--disable-sync");
        options.addArguments("--disable-crash-reporter");
        return options;
    }


    private void saveCrawlAnnouncements(WebDriver driver, List<String> crawlLinks, Set<String> dormitoryMoveLinks) {
        for (String crawlLink : crawlLinks) {
            try {
                // 드라이버 인스턴스 전달
                saveCrawlAnnouncement(driver, crawlLink, dormitoryMoveLinks.contains(crawlLink));
            } catch (Exception e) {
                log.error("공지사항 저장 실패 (링크: {}): {}", crawlLink, e.getMessage());
                // 한 건 실패해도 계속 진행
            }
        }
    }

    /**
     * 💡 변경됨: WebDriver를 인수로 받으며, 드라이버 생성/종료 로직이 삭제되었습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCrawlAnnouncement(WebDriver driver, String link, boolean isDormitoryMove) {
        // WebDriver driver = null; // ❌ 삭제됨

        try {
            // ChromeOptions 설정 및 driver = new ChromeDriver(options); ❌ 삭제됨

            driver.get(link); // 재활용된 드라이버 사용

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".view-title")));

            // ... (기존 상세 크롤링 및 저장 로직 유지) ...

            String title = "";
            String category = "";
            try {
                WebElement titleElement = driver.findElement(By.cssSelector(".view-title"));
                String fullTitle = titleElement.getText().trim();

                // 입퇴사 공지인 경우 무조건 "입퇴사 공지"로 설정
                if (isDormitoryMove) {
                    category = "입퇴사 공지";
                    title = fullTitle; // 입퇴사 공지는 전체 제목 사용
                } else {
                    // 카테고리 추출 및 제목 분리
                    if (fullTitle.startsWith("[") && fullTitle.contains("]")) {
                        int endIndex = fullTitle.indexOf("]");
                        category = fullTitle.substring(1, endIndex);

                        // ] 이후의 문자열을 제목으로 (공백 제거)
                        title = fullTitle.substring(endIndex + 1).trim();
                    } else {
                        category = "기타";
                        title = fullTitle;
                    }
                }

            } catch (Exception e) {
                log.debug("제목 추출 실패");
                category = isDormitoryMove ? "입퇴사 공지" : "기타";
            }

            // 조회수
            int viewCountInt = 0;
            try {
                WebElement viewCountElement = driver.findElement(By.cssSelector("dl.count dd"));
                String viewCount = viewCountElement.getText().trim();
                if (!viewCount.isEmpty() && viewCount.matches("\\d+")) {
                    viewCountInt = Integer.parseInt(viewCount);
                }
            } catch (Exception e) {
                log.debug("조회수 추출 실패, 기본값 0 사용");
            }

            // 글번호 (String으로 처리)
            String number = "";
            try {
                WebElement numberElement = driver.findElement(By.cssSelector("dl.view-num dd"));
                number = numberElement.getText().trim();

                // 빈 값이면 건너뛰기
                if (number.isEmpty()) {
                    log.warn("빈 글번호, 건너뛰기");
                    return;
                }

                // 이미 저장되어 있는 공지사항은 저장 제외
                Optional<CrawledAnnouncement> existingAnnouncement =
                        crawledAnnouncementRepository.findByNumber(number);

                if (existingAnnouncement.isPresent()) {
                    CrawledAnnouncement announcement = existingAnnouncement.get();
                    announcement.updateViewCount(viewCountInt);
                    crawledAnnouncementRepository.saveAndFlush(announcement); // 명시적 저장
                    log.info("기존 공지사항 조회수 업데이트 - 번호: {}, 조회수: {}", number, viewCountInt);
                    return;
                }
            } catch (Exception e) {
                log.error("글번호 추출 실패: {}", e.getMessage());
                return; // 글번호가 없으면 저장하지 않음
            }

            // 작성일
            String createDate = "";
            try {
                WebElement createDateElement = driver.findElement(By.cssSelector("dl.write dd"));
                createDate = createDateElement.getText().trim();
            } catch (Exception e) {
                log.debug("작성일 추출 실패");
            }

            // 작성자
            String writer = "";
            try {
                WebElement writerElement = driver.findElement(By.cssSelector("dl.writer dd"));
                writer = writerElement.getText().trim();
            } catch (Exception e) {
                log.debug("작성자 추출 실패");
            }



            // 본문 내용
            String content = "";
            try {
                WebElement contentElement = driver.findElement(By.cssSelector(".view-con"));
                List<WebElement> children = contentElement.findElements(By.xpath("./*"));

                for (WebElement child : children) {
                    String textContent = child.getText().trim();
                    content = content + textContent + "\n";
                }
            } catch (Exception e) {
                log.debug("본문 내용 추출 실패");
            }

            // 첨부파일 목록
            List<CrawledAnnouncementFile> crawledAnnouncementFiles = new ArrayList<>();
            try {
                List<WebElement> fileElements = driver.findElements(By.cssSelector(".view-file .insert ul li"));
                for (WebElement fileElement : fileElements) {
                    try {
                        WebElement linkElement = fileElement.findElement(By.tagName("a"));
                        String fileName = linkElement.getText().trim();
                        String downloadUrl = linkElement.getAttribute("href");

                        if (!fileName.isEmpty() && downloadUrl != null && !downloadUrl.isEmpty()) {
                            if (!downloadUrl.startsWith("http")) {
                                downloadUrl = "https://dorm.inu.ac.kr" + downloadUrl;
                            }

                            CrawledAnnouncementFile fileDto = CrawledAnnouncementFile.builder()
                                    .fileName(fileName)
                                    .filePath(downloadUrl)
                                    .build();
                            crawledAnnouncementFiles.add(fileDto);
                        }
                    } catch (Exception e) {
                        log.debug("개별 파일 추출 실패: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.debug("첨부파일 목록 추출 실패");
            }

            log.info("상세 정보 크롤링 완료: {}", title);

            CrawledAnnouncement crawledAnnouncement = CrawledAnnouncement.builder()
                    .category(AnnouncementCategory.from(category))
                    .number(number)  // String으로 저장
                    .title(title)
                    .writer(writer)
                    .viewCount(viewCountInt)
                    .announcementType(AnnouncementType.DORMITORY)
                    .content(content)
                    .crawledAnnouncementFiles(crawledAnnouncementFiles)
                    .crawledDate(LocalDate.parse(createDate))
                    .link(link)
                    .build();

            crawledAnnouncementRepository.save(crawledAnnouncement);

            for (CrawledAnnouncementFile attachedFile : crawledAnnouncementFiles) {
                attachedFile.updateCrawledAnnouncement(crawledAnnouncement);
                crawledAnnouncementFileRepository.save(attachedFile);
            }

            Notification notification = Notification.builder()
                    .boardId(crawledAnnouncement.getId())
                    .title("새로운 공지사항이 올라왔어요!")
                    .body(crawledAnnouncement.getTitle())
                    .notificationType(NotificationType.DORMITORY)
                    .apiType(ApiType.ANNOUNCEMENT)
                    .build();

            notificationRepository.save(notification);

            List<Role> dormitoryUserRoles = Arrays.asList(Role.ROLE_DORM_MANAGER, Role.ROLE_DORM_LIFE_MANAGER, Role.ROLE_DORM_ROOMMATE_MANAGER);

            List<User> allUsers = userRepository.findByReceiveNotificationTypesContainsAndRoleNotIn(NotificationType.DORMITORY, dormitoryUserRoles);

            for (User receiveUser : allUsers) {
                UserNotification userNotification = UserNotification.of(receiveUser, notification);
                userNotificationRepository.save(userNotification);

                fcmMessageService.sendNotification(receiveUser, notification.getTitle(), notification.getBody());
            }

        } catch (Exception e) {
            log.error("링크 크롤링 실패: {}", e.getMessage(), e);
        }
        // finally 블록에서 driver.quit(); ❌ 삭제됨 (crawling()에서 최종적으로 처리)
    }

    private List<Map<String, String>> extractNoticesFromPage(WebDriver driver) {
        List<Map<String, String>> notices = new ArrayList<>();
        // ... (기존 로직 유지) ...

        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.board-table tbody tr"));

            for (WebElement row : rows) {
                try {
                    Map<String, String> noticeInfo = new HashMap<>();

                    // 링크 추출
                    String link = "";
                    try {
                        WebElement linkElement = row.findElement(By.cssSelector("td.td-subject a"));
                        String href = linkElement.getAttribute("href");
                        if (href != null && !href.isEmpty()) {
                            link = href;
                        } else {
                            String onclick = linkElement.getAttribute("onclick");
                            if (onclick != null && onclick.contains("jf_viewArtcl")) {
                                link = "javascript:" + onclick;
                            }
                        }
                    } catch (Exception e) {
                        // 링크 없는 경우 무시
                    }

                    // 날짜 추출
                    String date = "";
                    try {
                        WebElement dateElement = row.findElement(By.cssSelector("td.td-date"));
                        date = dateElement.getText().trim();
                    } catch (Exception e) {
                        log.warn("날짜 파싱 실패: {}", e.getMessage());
                    }

                    noticeInfo.put(link, date);
                    notices.add(noticeInfo);

                } catch (Exception e) {
                    log.warn("행 파싱 중 오류 발생: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("페이지 파싱 실패: ", e);
        }
        return notices;
    }

    /**
     * 💡 변경됨: 두 크롤링 메서드를 하나로 통합하고, WebDriver와 URL을 인수로 받습니다.
     */
    public List<Map<String, String>> crawlWithSeleniumNotices(WebDriver driver, String baseUrl) {
        List<Map<String, String>> crawlLinks = new ArrayList<>();
        // WebDriver driver = null; // ❌ 삭제됨

        try {
            // ChromeOptions 설정 및 driver = new ChromeDriver(options); ❌ 삭제됨

            driver.get(baseUrl); // 재활용된 드라이버 사용

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.board-table")));

            int totalPages = getTotalPages(driver);
            log.info("총 페이지 수: {}", totalPages);

            // 기존 로직 유지 (1~2 페이지 크롤링)
            for (int page = 1; page <= 2; page++) {
                log.info("페이지 {} 크롤링 시작...", page);

                if (page > 1) {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("page_link('" + page + "')");
                    // Thread.sleep()은 권장되지 않지만, 페이지 로딩 문제로 임시 유지
                    Thread.sleep(1000);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.board-table")));
                }

                List<Map<String, String>> extractCrawlLinks = extractNoticesFromPage(driver);
                crawlLinks.addAll(extractCrawlLinks);

                log.info("페이지 {} 완료: {}개의 공지사항 수집", page, extractCrawlLinks.size());
            }

            log.info("전체 크롤링 완료: 총 {}개의 공지사항 수집", crawlLinks.size());

        } catch (Exception e) {
            log.error("Selenium 크롤링 실패 (URL: {}): ", baseUrl, e);
        }
        // finally 블록에서 driver.quit(); ❌ 삭제됨 (crawling()에서 최종적으로 처리)

        return crawlLinks;
    }


    private int getTotalPages(WebDriver driver) {
        // ... (기존 로직 유지) ...

        try {
            WebElement totPageElement = driver.findElement(By.cssSelector("._paging ._totPage"));
            String totalPagesText = totPageElement.getText().trim();

            log.debug("추출된 총 페이지 텍스트: '{}'", totalPagesText);

            if (totalPagesText.isEmpty()) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Object result = js.executeScript("return document.querySelector('._paging ._totPage').textContent;");
                totalPagesText = result != null ? result.toString().trim() : "";
                log.debug("JavaScript로 추출된 텍스트: '{}'", totalPagesText);
            }

            if (!totalPagesText.isEmpty()) {
                return Integer.parseInt(totalPagesText);
            }

            List<WebElement> pageLinks = driver.findElements(By.cssSelector("._paging ul li a"));
            int maxPage = 1;
            for (WebElement link : pageLinks) {
                try {
                    String pageText = link.getText().trim();
                    if (!pageText.isEmpty() && pageText.matches("\\d+")) {
                        int pageNum = Integer.parseInt(pageText);
                        if (pageNum > maxPage) {
                            maxPage = pageNum;
                        }
                    }
                } catch (Exception e) {
                    // 숫자가 아닌 경우 무시
                }
            }

            try {
                WebElement lastButton = driver.findElement(By.cssSelector("._paging ._last"));
                String onclick = lastButton.getAttribute("href");
                if (onclick != null && onclick.contains("page_link")) {
                    String pageNum = onclick.replaceAll("[^0-9]", "");
                    if (!pageNum.isEmpty()) {
                        return Integer.parseInt(pageNum);
                    }
                }
            } catch (Exception e) {
                log.debug("끝 버튼에서 페이지 추출 실패");
            }

            log.warn("총 페이지 수를 정확히 파악할 수 없어 최댓값 {}을 사용합니다", maxPage);
            return maxPage > 1 ? maxPage : 1;

        } catch (Exception e) {
            log.warn("총 페이지 수를 가져올 수 없습니다. 기본값 1로 설정: {}", e.getMessage());
            return 1;
        }
    }
}