package com.example.appcenter_project.utils;

import com.example.appcenter_project.dto.response.announcement.ContentElementDto;
import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.announcement.AttachedFile;
import com.example.appcenter_project.enums.announcement.AnnouncementType;
import com.example.appcenter_project.repository.announcement.AnnouncementRepository;
import jakarta.transaction.Transactional;
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

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementCrawlScheduler {

    private static final String BASE_URL = "https://dorm.inu.ac.kr/dorm/6528/subview.do";

    private final AnnouncementRepository announcementRepository;

    @Scheduled(cron = "0 12 10,20 * * ?")
    public void crawling() {
        List<String> crawlLinks = crawlWithSelenium();
        saveCrawlAnnouncements(crawlLinks);
    }

    private void saveCrawlAnnouncements(List<String> crawlLinks) {
        for (String crawlLink : crawlLinks) {
            saveCrawlAnnouncement(crawlLink);
        }
    }

    public void saveCrawlAnnouncement(String link) {
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");

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

            // 글번호
            String number = "";
            try {
                WebElement numberElement = driver.findElement(By.cssSelector("dl.view-num dd"));
                number = numberElement.getText().trim();
            } catch (Exception e) {
                log.debug("글번호 추출 실패");
            }
            // 이미 저장되어 있는 공지사항은 저장 제외
            if (announcementRepository.existsByNumber(number)) {
                return;
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
            String viewCount = "";
            try {
                WebElement viewCountElement = driver.findElement(By.cssSelector("dl.count dd"));
                viewCount = viewCountElement.getText().trim();
            } catch (Exception e) {
                // 조회수 없을 수 있음
            }

            // 본문 내용
            List<ContentElementDto> contentElements = new ArrayList<>();
            try {
                WebElement contentElement = driver.findElement(By.cssSelector(".view-con"));
                // 모든 직접 자식 요소 (p, div, span 등)
                List<WebElement> children = contentElement.findElements(By.xpath("./*"));

                for (WebElement child : children) {
                    String tagName = child.getTagName();
                    String textContent = child.getText().trim();
                    String innerHTML = child.getAttribute("innerHTML");
                    String style = child.getAttribute("style");
                    String className = child.getAttribute("class");

                    ContentElementDto element = ContentElementDto.builder()
                            .tagName(tagName)
                            .textContent(textContent)
                            .innerHTML(innerHTML)
                            .styleAttribute(style != null ? style : "")
                            .className(className != null ? className : "")
                            .build();

                    contentElements.add(element);
                }
            } catch (Exception e) {
                log.debug("본문 내용 추출 실패");
            }

            // 첨부파일 목록
            List<AttachedFile> attachedFiles = new ArrayList<>();
            try {
                List<WebElement> fileElements = driver.findElements(By.cssSelector(".view-file .insert ul li"));
                for (WebElement fileElement : fileElements) {
                    try {
                        WebElement linkElement = fileElement.findElement(By.tagName("a"));
                        String fileName = linkElement.getText().trim();
                        String downloadUrl = linkElement.getAttribute("href");

                        if (!fileName.isEmpty() && downloadUrl != null && !downloadUrl.isEmpty()) {
                            // 상대 경로인 경우 절대 경로로 변환
                            if (!downloadUrl.startsWith("http")) {
                                downloadUrl = "https://dorm.inu.ac.kr" + downloadUrl;
                            }

                            AttachedFile fileDto = AttachedFile.builder()
                                    .fileName(fileName)
                                    .filePath(downloadUrl)
                                    .build();
                            attachedFiles.add(fileDto);
                        }
                    } catch (Exception e) {
                        log.debug("개별 파일 추출 실패: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.debug("첨부파일 목록 추출 실패");
            }

            log.info("상세 정보 크롤링 완료: {}", title);

            Announcement announcement = Announcement.builder()
                    .number(number)
                    .title(title)
                    .writer(writer)
                    .viewCount(Integer.parseInt(viewCount))
                    .isEmergency(false)
                    .crawlCreateDate(LocalDate.parse(createDate))
                    .announcementType(AnnouncementType.DORMITORY)
//                    .content(content)
                    .attachedFiles(attachedFiles)
                    .build();

            announcementRepository.save(announcement);

            for (AttachedFile attachedFile : attachedFiles) {
                attachedFile.updateAnnouncement(announcement);
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
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");

            driver = new ChromeDriver(options);

            // 첫 페이지 접속
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.board-table")));

            // 총 페이지 수 추출
            int totalPages = getTotalPages(driver);
            log.info("총 페이지 수: {}", totalPages);

            // 모든 페이지 순회
            for (int page = 1; page <= totalPages; page++) {
                log.info("페이지 {} 크롤링 시작...", page);

                if (page > 1) {
                    // JavaScript로 페이지 이동
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("page_link('" + page + "')");

                    // 페이지 로딩 대기
                    Thread.sleep(1000);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.board-table")));
                }

                List<String> extractCrawlLinks = extractNoticesFromPage(driver);
                crawlLinks.addAll(extractCrawlLinks);

                log.info("페이지 {} 완료: {}개의 공지사항 수집", page, extractCrawlLinks.size());
            }

            log.info("전체 크롤링 완료: 총 {}개의 공지사항 수집", crawlLinks.size());

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
                    String rowClass = row.getAttribute("class");

                    // 링크 추출
                    String link = "";
                    try {
                        WebElement linkElement = row.findElement(By.cssSelector("td.td-subject a"));
                        String href = linkElement.getAttribute("href");
                        if (href != null && !href.isEmpty()) {
                            link = href;
                        } else {
                            // onclick에서 링크 추출
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
            // ._totPage 요소의 내부 텍스트 추출
            WebElement totPageElement = driver.findElement(By.cssSelector("._paging ._totPage"));
            String totalPagesText = totPageElement.getText().trim();

            log.debug("추출된 총 페이지 텍스트: '{}'", totalPagesText);

            if (totalPagesText.isEmpty()) {
                // JavaScript로 직접 추출 시도
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Object result = js.executeScript("return document.querySelector('._paging ._totPage').textContent;");
                totalPagesText = result != null ? result.toString().trim() : "";
                log.debug("JavaScript로 추출된 텍스트: '{}'", totalPagesText);
            }

            if (!totalPagesText.isEmpty()) {
                return Integer.parseInt(totalPagesText);
            }

            // 페이지네이션 링크에서 최대 페이지 추출 시도
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

            // "끝" 버튼에서 페이지 추출
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
