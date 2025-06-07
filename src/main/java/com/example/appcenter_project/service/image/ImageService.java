package com.example.appcenter_project.service.image;

import com.example.appcenter_project.dto.ImageDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
