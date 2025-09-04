package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static com.example.appcenter_project.exception.ErrorCode.IMAGE_NOT_FOUND;

@Slf4j
@Getter
public class ResponseGroupOrderDto extends ResponseBoardDto {
    private String deadline;
    private int price;
    private GroupOrderType groupOrderType;
    private boolean isRecruitmentComplete;
    private int viewCount;

    public ResponseGroupOrderDto(Long id, String title, String type, int price, String link,
                                 int currentPeople, int maxPeople, String deadline, int viewCount,
                                 int groupOrderLike, String description, LocalDateTime createTime, String fileName, String groupOrderType, boolean isRecruitmentComplete) {
        super(id, title, type, createTime, fileName);
        this.price = price;
        this.deadline = deadline;
        // from() 메서드를 사용하여 안전하게 변환
        this.groupOrderType = GroupOrderType.from(groupOrderType);
        this.viewCount = viewCount;
        this.isRecruitmentComplete = isRecruitmentComplete;
    }

    @Builder
    public ResponseGroupOrderDto(Long id, String title, String type, LocalDateTime createTime, String fileName, int viewCount,
                                 String deadline, int price, int currentPeople, int maxPeople, String groupOrderType, boolean isRecruitmentComplete) {
        super(id, title, type, createTime, fileName);
        this.deadline = deadline;
        this.price = price;
        this.viewCount = viewCount;
        // from() 메서드를 사용하여 안전하게 변환
        this.groupOrderType = GroupOrderType.from(groupOrderType);
        this.isRecruitmentComplete = isRecruitmentComplete;
    }

    // Keep your existing entityToDto method unchanged
    public static ResponseGroupOrderDto entityToDto(GroupOrder groupOrder, HttpServletRequest request) {

        String fileName = null;

        // 이미지 리스트가 존재하고 비어있지 않은 경우에만 처리
        if (groupOrder.getImageList() != null && !groupOrder.getImageList().isEmpty()) {
            Image image = groupOrder.getImageList().get(0);
            fileName = getGroupOrderImage(image, request);
        }

        // groupOrderType이 null인 경우 기본값 설정
        String groupOrderTypeStr = groupOrder.getGroupOrderType() != null
                ? groupOrder.getGroupOrderType().name()
                : GroupOrderType.ETC.name();

        return ResponseGroupOrderDto.builder()
                .id(groupOrder.getId())
                .title(groupOrder.getTitle())
                .type("GROUP_ORDER")
                .deadline(String.valueOf(groupOrder.getDeadline()))
                .price(groupOrder.getPrice())
                .createTime(groupOrder.getCreatedDate())
                .fileName(fileName)
                .viewCount(groupOrder.getGroupOrderViewCount())
                .groupOrderType(groupOrderTypeStr)
                .isRecruitmentComplete(groupOrder.isRecruitmentComplete())
                .build();
    }

    public static ResponseGroupOrderDto entityToDto(GroupOrder groupOrder, String fileName) {

        // groupOrderType이 null인 경우 기본값 설정
        String groupOrderTypeStr = groupOrder.getGroupOrderType() != null
                ? groupOrder.getGroupOrderType().name()
                : GroupOrderType.ETC.name();

        return ResponseGroupOrderDto.builder()
                .id(groupOrder.getId())
                .title(groupOrder.getTitle())
                .type("GROUP_ORDER")
                .deadline(String.valueOf(groupOrder.getDeadline()))
                .price(groupOrder.getPrice())
                .createTime(groupOrder.getCreatedDate())
                .fileName(fileName)
                .viewCount(groupOrder.getGroupOrderViewCount())
                .groupOrderType(groupOrderTypeStr)
                .isRecruitmentComplete(groupOrder.isRecruitmentComplete())
                .build();
    }

    public static String getGroupOrderImage(Image image, HttpServletRequest request) {
        File file = new File(image.getFilePath());
        log.info("Checking group-order image file: {}", image.getFilePath());
        log.info("File exists: {}", file.exists());

        if (!file.exists()) {
            log.error("GroupOrder image file not found: {}", image.getFilePath());
            throw new CustomException(IMAGE_NOT_FOUND);
        }

        // 이미지 URL 생성
        String baseUrl = getBaseUrl(request);
        String imageUrl = baseUrl + "/api/images/group-order/" + image.getId();

        // 정적 리소스 URL 생성
        String staticImageUrl = getStaticGroupOrderImageUrl(image.getFilePath(), baseUrl);
        String changeUrl = staticImageUrl.replace("http", "https");

        return changeUrl;
    }

    // 유틸리티: 베이스 URL 생성 (ImageService와 동일)
    private static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        // 기본 포트가 아닌 경우에만 포트 추가
        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }

        baseUrl.append(contextPath);
        return baseUrl.toString();
    }

    // 정적 GroupOrder 이미지 URL 생성 헬퍼 메소드
    private static String getStaticGroupOrderImageUrl(String filePath, String baseUrl) {
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            return baseUrl + "/images/group-order/" + fileName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for group-order image path: {}", filePath);
            return null;
        }
    }

}