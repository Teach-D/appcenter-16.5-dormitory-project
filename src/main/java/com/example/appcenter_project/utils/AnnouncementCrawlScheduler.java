package com.example.appcenter_project.utils;

import com.example.appcenter_project.entity.announcement.CrawledAnnouncement;
import com.example.appcenter_project.entity.file.CrawledAnnouncementFile;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.ApiType;
import com.example.appcenter_project.enums.announcement.AnnouncementType;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.enums.user.Role;
import com.example.appcenter_project.repository.announcement.CrawledAnnouncementRepository;
import com.example.appcenter_project.repository.file.CrawledAnnouncementFileRepository;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementCrawlScheduler {

    private static final String BASE_URL = "https://dorm.inu.ac.kr/dorm/6528/subview.do";

    private final CrawledAnnouncementRepository crawledAnnouncementRepository;
    private final CrawledAnnouncementFileRepository crawledAnnouncementFileRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final FcmMessageService fcmMessageService;

    @Scheduled(cron = "0 0 9,18 * * ?")
    @Transactional
    public void crawling() {
        List<String> crawlLinks = crawlWithSelenium();
        saveCrawlAnnouncements(crawlLinks);
    }

    private void saveCrawlAnnouncements(List<String> crawlLinks) {
        for (String crawlLink : crawlLinks) {
            try {
                saveCrawlAnnouncement(crawlLink);
            } catch (Exception e) {
                log.error("공지사항 저장 실패 (링크: {}): {}", crawlLink, e.getMessage());
                // 한 건 실패해도 계속 진행
            }
        }
    }

    public void saveCrawlAnnouncement(String link) {
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--disable-extensions");
            options.addArguments("--remote-debugging-port=9222");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);
            driver.get(link);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".view-title")));

            // 제목
            String title = "";
            try {
                WebElement titleElement = driver.findElement(By.cssSelector(".view-title"));
                title = titleElement.getText().trim();
            } catch (Exception e) {
                log.debug("제목 추출 실패");
            }

            // 카테고리
            String category = "";
            try {
                WebElement categoryElement = driver.findElement(By.cssSelector(".view-title span"));
                category = categoryElement.getText().trim();
            } catch (Exception e) {
                // 카테고리 없을 수 있음
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
                if (crawledAnnouncementRepository.existsByNumber(number)) {
                    log.debug("이미 존재하는 공지사항 번호: {}", number);
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
                    .category(category)
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
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public List<String> crawlWithSelenium() {
        List<String> crawlLinks = new ArrayList<>();
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--disable-extensions");
            options.addArguments("--remote-debugging-port=9222");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);
            driver.get(BASE_URL);
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.board-table")));

            int totalPages = getTotalPages(driver);
            log.info("총 페이지 수: {}", totalPages);

            for (int page = 1; page <= 2; page++) {
                log.info("페이지 {} 크롤링 시작...", page);

                if (page > 1) {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("page_link('" + page + "')");
                    Thread.sleep(1000);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.board-table")));
                }

                List<String> extractCrawlLinks = extractNoticesFromPage(driver);
                crawlLinks.addAll(extractCrawlLinks);

                log.info("페이지 {} 완료: {}개의 공지사항 수집", page, extractCrawlLinks.size());
            }

            log.info("전체 크롤링 완료: 총 {}개의 공지사항 수집", crawlLinks.size());

/*            for (int page = 1; page <= totalPages; page++) {
                log.info("페이지 {} 크롤링 시작...", page);

                if (page > 1) {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("page_link('" + page + "')");
                    Thread.sleep(1000);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.board-table")));
                }

                List<String> extractCrawlLinks = extractNoticesFromPage(driver);
                crawlLinks.addAll(extractCrawlLinks);

                log.info("페이지 {} 완료: {}개의 공지사항 수집", page, extractCrawlLinks.size());
            }

            log.info("전체 크롤링 완료: 총 {}개의 공지사항 수집", crawlLinks.size());
            */

        } catch (Exception e) {
            log.error("Selenium 크롤링 실패: ", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return crawlLinks;
    }

    private List<String> extractNoticesFromPage(WebDriver driver) {
        List<String> notices = new ArrayList<>();

        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.board-table tbody tr"));

            for (WebElement row : rows) {
                try {
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

                    notices.add(link);

                } catch (Exception e) {
                    log.warn("행 파싱 중 오류 발생: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("페이지 파싱 실패: ", e);
        }

        return notices;
    }

    private int getTotalPages(WebDriver driver) {
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
