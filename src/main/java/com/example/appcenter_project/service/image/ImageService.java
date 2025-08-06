package com.example.appcenter_project.service.image;

import com.example.appcenter_project.dto.ImageDto;
import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.UUID;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void initializeDefaultUserImage() {
        try {
            // 이미 기본 유저 이미지가 존재하는지 확인
            boolean defaultImageExists = imageRepository.existsByImageTypeAndIsDefault(ImageType.USER, true);

            if (!defaultImageExists) {
                createDefaultUserImage();
            }
        } catch (Exception e) {
            log.error("Failed to initialize default user image", e);
            // 기본 이미지 초기화 실패 시에도 애플리케이션이 시작되도록 함
        }
    }

    private void createDefaultUserImage() {
        String storagePath = System.getProperty("user.dir") + "/images/user/";

        // 기본 이미지 파일명
        String defaultImageFileName = "default_user_image.png";
        String defaultImagePath = storagePath + defaultImageFileName;

        // 디렉토리가 없으면 생성
        File directory = new File(storagePath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                log.error("Failed to create directory: {}", storagePath);
                throw new CustomException(IMAGE_NOT_FOUND);
            }
        }

        // 기본 이미지 파일이 존재하는지 확인
        File defaultImageFile = new File(defaultImagePath);
        if (!defaultImageFile.exists()) {
            // 클래스패스에서 기본 이미지 복사
            try {
                ClassPathResource resource = new ClassPathResource("static/images/user/default_user_image.png");
                if (resource.exists()) {
                    try (InputStream inputStream = resource.getInputStream();
                         FileOutputStream outputStream = new FileOutputStream(defaultImageFile)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        log.info("Default image copied from classpath to: {}", defaultImagePath);
                    }
                } else {
                    log.warn("Default image not found in classpath, skipping default image creation");
                    return; // 기본 이미지가 없으면 그냥 건너뛰기
                }
            } catch (IOException e) {
                log.error("Failed to copy default image from classpath", e);
                return; // 예외 발생 시 건너뛰기
            }
        }

        // 이미지 객체 생성 후 저장
        Image image = Image.builder()
                .filePath(defaultImagePath)
                .imageType(ImageType.USER)
                .isDefault(true)
                .build();

        imageRepository.save(image);
    }

    public void updateUserImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 이미 user의 이미지가 defaultImage인 경우
        if (file != null && user.getImage().getIsDefault()) {
            // 개발 환경에 맞는 경로 설정
            String basePath = System.getProperty("user.dir");
            String imagePath = basePath + "/images/user/";

            // 파일 확장자 추출
            String fileExtension = getFileExtension(file.getOriginalFilename());

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            // 사용자별 고유 파일명 생성 (user_${userId}.확장자)
            String imageFileName = "user_" + userId + timestamp.getTime() + fileExtension;

            // 디렉토리 생성 (존재하지 않으면)
            File directory = new File(imagePath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File destinationFile = new File(imagePath + imageFileName);

            try {
                file.transferTo(destinationFile);

                // 이미지 객체 생성 후 저장
                Image image = Image.builder()
                        .filePath(destinationFile.getAbsolutePath())
                        .isDefault(false)
                        .imageType(ImageType.USER)
                        .build();
                imageRepository.save(image);

                // user에 image 연관관계 세팅
                user.updateImage(image);

            } catch (IOException e) {
                log.error("Failed to save image file for user {}: ", userId, e);
            }
        }
        // 해당 유저의 이미지가 defaultImage가 아닌 이미지일 때 새로운 이미지로 수정하는 경우
        else if (file != null) {
            String oldFilePath = user.getImage().getFilePath();

            // 기존 파일 삭제
            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            // 새 파일 경로 설정
            String basePath = System.getProperty("user.dir");
            String imagePath = basePath + "/images/user/";

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String imageFileName = "user_" + userId + timestamp.getTime() + fileExtension;

            // 디렉토리 생성 (존재하지 않으면)
            File directory = new File(imagePath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File destinationFile = new File(imagePath + imageFileName);

            Image image = imageRepository.findByFilePath(oldFilePath)
                    .orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));
            image.updateFilePath(destinationFile.getAbsolutePath());

            try {
                file.transferTo(destinationFile);
            } catch (IOException e) {
                log.error("Failed to update image file for user {}: ", userId, e);
            }
        }
    }

    // 파일 확장자 추출 헬퍼 메소드
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".jpg"; // 기본 확장자
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return ".jpg"; // 확장자가 없으면 기본값
        }

        return fileName.substring(lastDotIndex).toLowerCase();
    }

    // 안전한 파일명 생성 (특수문자 제거)
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "image";

        // 특수문자를 언더스코어로 대체
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public ImageDto findUserImageByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Image image = user.getImage();

        File file = new File(image.getFilePath());
        if (!file.exists()) {
            throw new CustomException(IMAGE_NOT_FOUND);
        }

        Resource resource = new FileSystemResource(file);

        String contentType;
        try {
            contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not determine file type.", e);
        }

        return ImageDto.builder().resource(resource).contentType(contentType).build();
    }

    public void setDefaultUserImage(MultipartFile file) {
        String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\images\\user\\";
        UUID uuid = UUID.randomUUID();
        String imageFileName = uuid + "_" + file.getOriginalFilename();

        File destinationFile = new File(projectPath + imageFileName);

        try {
            file.transferTo(destinationFile);
            // 이미지 객체 생성 후 저장
            Image image = Image.builder()
                    .filePath(projectPath + imageFileName)
                    .imageType(ImageType.USER)
                    .isDefault(true)
                    .build();
            imageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ImageLinkDto findUserImageUrlByUserId(Long userId, HttpServletRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            Image image = user.getImage();
            if (image == null) {
                throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
            }

            // 파일 존재 확인
            File file = new File(image.getFilePath());
            log.info("Checking file: {}", image.getFilePath());
            log.info("File exists: {}", file.exists());
            log.info("File readable: {}", file.canRead());
            log.info("File size: {}", file.length());

            if (!file.exists()) {
                log.error("Image file not found: {}", image.getFilePath());

                // 디렉토리 존재 여부도 확인
                File parentDir = file.getParentFile();
                log.error("Parent directory exists: {}", parentDir.exists());
                if (parentDir.exists()) {
                    log.error("Files in directory: {}", Arrays.toString(parentDir.listFiles()));
                }

                throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
            }

            // 이미지 URL 생성 (기존 API 방식)
            String baseUrl = getBaseUrl(request);
            String imageUrl = baseUrl + "/api/images/" + image.getId();

            // 정적 리소스 URL 생성 (직접 접근 가능)
            String staticImageUrl = getStaticImageUrl(image.getFilePath(), baseUrl);
            String changeUrl = staticImageUrl.replace("http", "https");

            // 안전한 컨텐츠 타입 확인
            String contentType = getSafeContentType(file);

            // 실제 파일명 추출 (경로에서 파일명만)
            String actualFileName = Paths.get(image.getFilePath()).getFileName().toString();


            return ImageLinkDto.builder()
                    .imageUrl(imageUrl)
                    .fileName(changeUrl)  // 정적 리소스로 직접 접근 가능한 URL
                    .contentType(contentType)
                    .fileSize(file.length())
                    .build();
        } catch (Exception e) {
            log.error("Error in findUserImageByUserId: ", e);
            throw e;
        }
    }

    // 정적 리소스 URL 생성 헬퍼 메소드
    private String getStaticImageUrl(String filePath, String baseUrl) {
        try {
            // 파일 경로에서 static 이후 경로 추출
            // 예: /path/to/resources/static/images/user/file.png -> /images/user/file.png
            String staticPath = extractStaticPath(filePath);
            return baseUrl + staticPath;
        } catch (Exception e) {
            log.warn("Could not generate static URL for path: {}", filePath);
            return null;
        }
    }

    // static 폴더 이후의 경로를 추출
    private String extractStaticPath(String fullPath) {
        // 실제 파일 경로에서 resources/static 이후의 경로를 추출
        // 또는 단순히 파일명으로만 구성된 경우 /images/user/ 경로로 조합
        String fileName = Paths.get(fullPath).getFileName().toString();
        return "/images/user/" + fileName;
    }

    // 유틸리티: 안전한 파일 컨텐츠 타입 확인
    private String getSafeContentType(File file) {
        try {
            String fileName = file.getName().toLowerCase();

            // 확장자 기반으로 먼저 판단 (더 안정적)
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                return "image/png";
            } else if (fileName.endsWith(".gif")) {
                return "image/gif";
            } else if (fileName.endsWith(".webp")) {
                return "image/webp";
            } else if (fileName.endsWith(".svg")) {
                return "image/svg+xml";
            }

            // Files.probeContentType이 실패할 수 있으므로 try-catch
            try {
                String detectedType = Files.probeContentType(file.toPath());
                if (detectedType != null && detectedType.startsWith("image/")) {
                    return detectedType;
                }
            } catch (Exception e) {
                log.warn("Could not probe content type for file: {}", file.getName());
            }

            // 기본값
            return "image/jpeg";

        } catch (Exception e) {
            log.error("Error determining content type for file: {}", file.getName(), e);
            return "image/jpeg"; // 안전한 기본값
        }
    }

    // 유틸리티: 베이스 URL 생성
    private String getBaseUrl(HttpServletRequest request) {
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

    public void updateUserTimeTableImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (file == null || file.isEmpty()) {
            throw new CustomException(IMAGE_NOT_FOUND);
        }

        // 기존 시간표 이미지가 있다면 파일 삭제
        if (user.getTimeTableImage() != null) {
            String oldFilePath = user.getTimeTableImage().getFilePath();
            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                boolean deleted = oldFile.delete();
                if (!deleted) {
                    log.warn("Failed to delete old timetable image file: {}", oldFilePath);
                }
            }
            // 기존 이미지 엔티티 삭제
            imageRepository.delete(user.getTimeTableImage());
        }

        // 개발 환경에 맞는 경로 설정 (기존 user 이미지 방식과 동일)
        String basePath = System.getProperty("user.dir");
        String imagePath = basePath + "/images/user/timetable/";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String imageFileName = "timetable_user_" + userId + timestamp.getTime() + fileExtension;

        // 디렉토리 생성 (존재하지 않으면)
        File directory = new File(imagePath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                log.error("Failed to create timetable directory: {}", imagePath);
                throw new CustomException(IMAGE_NOT_FOUND);
            }
        }

        File destinationFile = new File(imagePath + imageFileName);

        try {
            file.transferTo(destinationFile);
            log.info("Timetable image saved successfully: {}", destinationFile.getAbsolutePath());

            // 이미지 객체 생성 후 저장
            Image image = Image.builder()
                    .filePath(destinationFile.getAbsolutePath())
                    .isDefault(false)
                    .imageType(ImageType.TIME_TABLE)
                    .boardId(userId)
                    .build();
            imageRepository.save(image);

            // user에 시간표 이미지 연관관계 세팅
            user.updateTimeTableImage(image);

        } catch (IOException e) {
            log.error("Failed to save timetable image file for user {}: ", userId, e);
            throw new CustomException(IMAGE_NOT_FOUND);
        }
    }

    public ImageLinkDto findUserTimeTableImageUrlByUserId(Long userId, HttpServletRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            Image timeTableImage = user.getTimeTableImage();
            if (timeTableImage == null) {
                throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
            }

            // 파일 존재 확인
            File file = new File(timeTableImage.getFilePath());
            log.info("Checking timetable file: {}", timeTableImage.getFilePath());
            log.info("File exists: {}", file.exists());

            if (!file.exists()) {
                log.error("Timetable image file not found: {}", timeTableImage.getFilePath());
                throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
            }

            // 이미지 URL 생성
            String baseUrl = getBaseUrl(request);
            String imageUrl = baseUrl + "/api/images/timetable/" + timeTableImage.getId();

            // 정적 리소스 URL 생성
            String staticImageUrl = getStaticTimeTableImageUrl(timeTableImage.getFilePath(), baseUrl);
            String changeUrl = staticImageUrl.replace("http", "https");

            // 안전한 컨텐츠 타입 확인
            String contentType = getSafeContentType(file);

            return ImageLinkDto.builder()
                    .imageUrl(imageUrl)
                    .fileName(changeUrl)
                    .contentType(contentType)
                    .fileSize(file.length())
                    .build();
        } catch (Exception e) {
            log.error("Error in findUserTimeTableImageUrlByUserId: ", e);
            throw e;
        }
    }

    public void deleteUserTimeTableImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Image timeTableImage = user.getTimeTableImage();
        if (timeTableImage == null) {
            throw new CustomException(IMAGE_NOT_FOUND);
        }

        // 파일 삭제
        String filePath = timeTableImage.getFilePath();
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.warn("Failed to delete timetable image file: {}", filePath);
            }
        }

        // 데이터베이스에서 이미지 엔티티 삭제
        imageRepository.delete(timeTableImage);

        // 사용자 엔티티에서 시간표 이미지 참조 제거
        user.removeTimeTableImage();
    }

    // 정적 시간표 이미지 URL 생성 헬퍼 메소드
    private String getStaticTimeTableImageUrl(String filePath, String baseUrl) {
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            return baseUrl + "/images/user/timetable/" + fileName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for timetable path: {}", filePath);
            return null;
        }
    }

    // Tip 이미지 업데이트
    public void updateTipImage(Long tipId, MultipartFile file) {
        // Tip 엔티티 조회는 TipService에서 하도록 수정할 수도 있지만,
        // 일단 여기서는 tipId만으로 이미지를 관리하는 방식으로 구현

        if (file == null || file.isEmpty()) {
            throw new CustomException(IMAGE_NOT_FOUND);
        }

        // 개발 환경에 맞는 경로 설정
        String basePath = System.getProperty("user.dir");
        String imagePath = basePath + "/images/tip/";

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String imageFileName = "tip_" + tipId + "_" + UUID.randomUUID() + fileExtension;

        // 디렉토리 생성 (존재하지 않으면)
        File directory = new File(imagePath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                log.error("Failed to create tip image directory: {}", imagePath);
                throw new CustomException(IMAGE_NOT_FOUND);
            }
        }

        File destinationFile = new File(imagePath + imageFileName);

        try {
            file.transferTo(destinationFile);
            log.info("Tip image saved successfully: {}", destinationFile.getAbsolutePath());

            // 이미지 객체 생성 후 저장
            Image image = Image.builder()
                    .filePath(destinationFile.getAbsolutePath())
                    .isDefault(false)
                    .imageType(ImageType.TIP)
                    .boardId(tipId)
                    .build();
            imageRepository.save(image);

        } catch (IOException e) {
            log.error("Failed to save tip image file for tip {}: ", tipId, e);
            throw new CustomException(IMAGE_NOT_FOUND);
        }
    }

    // Tip 이미지 URL 조회
    public ImageLinkDto findTipImageUrlByTipId(Long tipId, HttpServletRequest request) {
        try {
            // boardId와 imageType으로 이미지 조회
            Image tipImage = imageRepository.findByBoardIdAndImageType(tipId, ImageType.TIP)
                    .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));

            // 파일 존재 확인
            File file = new File(tipImage.getFilePath());
            log.info("Checking tip image file: {}", tipImage.getFilePath());
            log.info("File exists: {}", file.exists());

            if (!file.exists()) {
                log.error("Tip image file not found: {}", tipImage.getFilePath());
                throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
            }

            // 이미지 URL 생성
            String baseUrl = getBaseUrl(request);
            String imageUrl = baseUrl + "/api/images/tip/" + tipImage.getId();

            // 정적 리소스 URL 생성
            String staticImageUrl = getStaticTipImageUrl(tipImage.getFilePath(), baseUrl);

            // 안전한 컨텐츠 타입 확인
            String contentType = getSafeContentType(file);

            return ImageLinkDto.builder()
                    .imageUrl(imageUrl)
                    .fileName(staticImageUrl)
                    .contentType(contentType)
                    .fileSize(file.length())
                    .build();
        } catch (Exception e) {
            log.error("Error in findTipImageUrlByTipId: ", e);
            throw e;
        }
    }

    // Tip 이미지 삭제
    public void deleteTipImage(Long tipId) {
        Image tipImage = imageRepository.findByBoardIdAndImageType(tipId, ImageType.TIP)
                .orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));

        // 파일 삭제
        String filePath = tipImage.getFilePath();
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.warn("Failed to delete tip image file: {}", filePath);
            }
        }

        // 데이터베이스에서 이미지 엔티티 삭제
        imageRepository.delete(tipImage);
    }

    // 정적 Tip 이미지 URL 생성 헬퍼 메소드
    private String getStaticTipImageUrl(String filePath, String baseUrl) {
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            return baseUrl + "/images/tip/" + fileName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for tip image path: {}", filePath);
            return null;
        }
    }
    private String getContentType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                // 확장자 기반 fallback
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    return "image/jpeg";
                } else if (fileName.endsWith(".png")) {
                    return "image/png";
                } else if (fileName.endsWith(".gif")) {
                    return "image/gif";
                } else if (fileName.endsWith(".webp")) {
                    return "image/webp";
                } else {
                    return "application/octet-stream";
                }
            }
            return contentType;
        } catch (IOException e) {
            log.error("Could not determine file type for: {}", file.getPath(), e);
            throw new RuntimeException("Could not determine file type.", e);
        }
    }
}
