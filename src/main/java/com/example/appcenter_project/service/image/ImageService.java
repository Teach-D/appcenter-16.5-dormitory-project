package com.example.appcenter_project.service.image;

import com.example.appcenter_project.dto.ImageDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import jakarta.annotation.PostConstruct;
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
            String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\images\\user\\";
            UUID uuid = UUID.randomUUID();
            String imageFileName = uuid + "_" + file.getOriginalFilename();

            File destinationFile = new File(projectPath + imageFileName);

            try {
                file.transferTo(destinationFile);

                // 이미지 객체 생성 후 저장
                Image image = Image.builder()
                        .filePath(projectPath + imageFileName)
                        .isDefault(false)
                        .imageType(ImageType.USER)
                        .build();
                imageRepository.save(image);

                // user에 image 연관관계 세팅
                user.updateImage(image);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // 해당 유저의 이미지가 defaultImage가 아닌 이미지일 때 새로운 이미지로 수정하는 경우
        else if (file != null) {
            String filePath = user.getImage().getFilePath();

            new File(filePath).delete();

            String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\images\\user\\";
            UUID uuid = UUID.randomUUID();
            String imageFileName = uuid + "_" + file.getOriginalFilename();

            File destinationFile = new File(projectPath + imageFileName);

            Image image = imageRepository.findByFilePath(filePath).orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));
            image.updateFilePath(projectPath + imageFileName);

            try {
                file.transferTo(destinationFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
}
