package com.example.appcenter_project.service.image;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.image.ImageRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private static final String USER_IMAGE_PREFIX = "user";
    private static final String TIP_IMAGE_PREFIX = "tip";
    private static final String GROUP_ORDER_IMAGE_PREFIX = "group_order";
    private static final String TIME_TABLE_IMAGE_PREFIX = "time_table";
    private static final String COMPLAINT_IMAGE_PREFIX = "complaint";
    private static final String COMPLAINT_REPLY_IMAGE_PREFIX = "complaint_reply";
    private static final String POPUP_NOTIFICATION_IMAGE_PREFIX = "popup_notification";
    private static final String DEFAULT_IMAGE_EXTENSION = ".jpg";

    private final ImageRepository imageRepository;

    // todo saveImages
    public void saveImages(ImageType imageType, Long entityId, List<MultipartFile> images) {
        if (isImagesNotEmpty(images)) {
            String directoryPath = createDirectory(imageType);
            createAndSaveImages(directoryPath, images, entityId, imageType);
        }
    }

    // todo saveImage
    public void saveImage(ImageType imageType, Long entityId, MultipartFile image) {
        if (isImageNotEmpty(image)) {
            String directoryPath = createDirectory(imageType);
            createAndSaveImage(directoryPath, image, entityId, imageType);
        }
    }

    // todo findImages
    public List<ImageLinkDto> findImages(ImageType imageType, Long entityId, HttpServletRequest request) {
        try {
            List<Image> images = findImagesByImageTypeAndEntityId(imageType, entityId);
            return createImageDtos(imageType, request, images);
        } catch (Exception e) {
            log.error("타입: {} id: {} 이미지 조회에 실패했습니다", imageType, entityId, e);
            throw new CustomException(IMAGE_NOT_FOUND);
        }
    }

    // todo findImage
    public ImageLinkDto findImage(ImageType imageType, Long entityId, HttpServletRequest request) {
        try {
            Optional<Image> image = findImageByImageTypeAndEntityId(imageType, entityId);
            return createImageDto(imageType, request, image);
        } catch (Exception e) {
            log.error("타입: {} id: {} 이미지 조회에 실패했습니다", imageType, entityId, e);
            throw new CustomException(IMAGE_NOT_FOUND);
        }
    }

    public List<String> findStaticImageUrls(ImageType imageType, Long entityId, HttpServletRequest request) {
        try {
            return findImagesByImageTypeAndEntityId(imageType, entityId).stream()
                    .map(findImage -> createImageUrl(imageType, request, findImage))
                    .toList();
        } catch (Exception e) {
            log.error("타입: {} id: {} 이미지 조회에 실패했습니다", imageType, entityId, e);
            throw new CustomException(IMAGE_NOT_FOUND);
        }
    }

    // todo updateImages
    public void updateImages(ImageType imageType, Long entityId, List<MultipartFile> images) {
        deleteImages(imageType, entityId);
        saveImages(imageType, entityId, images);
    }

    public void updateGroupOrderImages(ImageType imageType, Long entityId, List<MultipartFile> images) {
//        deleteImages(imageType, entityId);
        saveImages(imageType, entityId, images);
    }

    // todo updateImage
    public void updateImage(ImageType imageType, Long entityId, MultipartFile image) {
        deleteImage(imageType, entityId);
        saveImage(imageType, entityId, image);
    }

    // todo deleteImages
    public void deleteImages(ImageType imageType, Long entityId) {
        List<Image> images = findImagesByImageTypeAndEntityId(imageType, entityId);
        for (Image image : images) {
            deleteImageFromDirectory(image);
            deleteImageFromDB(image);
        }
    }

    // todo deleteImage
    public void deleteImage(ImageType imageType, Long entityId) {
        Optional<Image> image = findImageByImageTypeAndEntityId(imageType, entityId);

        image.ifPresent(img -> {
            deleteImageFromDirectory(img);
            deleteImageFromDB(img);
        });
    }

    public void deleteImage(Image image) {
        deleteImageFromDirectory(image);
        deleteImageFromDB(image);
    }

    public String findStaticImageUrl(ImageType imageType, Long entityId, HttpServletRequest request) {
        return findImage(imageType, entityId, request).getImageUrl();
    }

    private String findStaticImageUrl(ImageType imageType, String imagePath, String appcenterHttpsUrl) {
        try {
            String imageName = getImageName(imagePath);
            if (imageType == ImageType.USER) {
                imageName = "/images/" + USER_IMAGE_PREFIX + "/" + imageName;
            }
            if (imageType == ImageType.TIP) {
                imageName = "/images/" + TIP_IMAGE_PREFIX + "/" + imageName;
            }
            if (imageType == ImageType.GROUP_ORDER) {
                imageName = "/images/" + GROUP_ORDER_IMAGE_PREFIX + "/" + imageName;
            }
            if (imageType == ImageType.TIME_TABLE) {
                imageName = "/images/" + TIME_TABLE_IMAGE_PREFIX + "/" + imageName;
            }
            if (imageType == ImageType.COMPLAINT) {
                imageName = "/images/" + COMPLAINT_IMAGE_PREFIX + "/" + imageName;
            }
            if (imageType == ImageType.COMPLAINT_REPLY) {
                imageName = "/images/" + COMPLAINT_REPLY_IMAGE_PREFIX + "/" + imageName;
            }
            if (imageType == ImageType.POPUP_NOTIFICATION) {
                imageName = "/images/" + POPUP_NOTIFICATION_IMAGE_PREFIX + "/" + imageName;
            }

            return appcenterHttpsUrl + imageName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for group-order image path: {}", imagePath);
            return null;
        }
    }

    public String getImageUrl(ImageType imageType, Image image, HttpServletRequest request) {
        return createImageUrl(imageType, request, image);
    }

    private boolean isImagesNotEmpty(List<MultipartFile> images) {
        return images != null && !images.isEmpty();
    }

    private boolean isImageNotEmpty(MultipartFile image) {
        return image != null && !image.isEmpty();
    }


    private String createDirectory(ImageType imageType) {
        String directoryPath = createDirectoryPath(imageType);
        createDirectoryDetail(directoryPath);

        return directoryPath;
    }

    private String createDirectoryPath(ImageType imageType) {
        String basePath = System.getProperty("user.dir");
        String directoryPath = basePath + "/images/";

        if (imageType == ImageType.USER) {
            directoryPath += (USER_IMAGE_PREFIX + "/");
        }
        if (imageType == ImageType.TIP) {
            directoryPath += (TIP_IMAGE_PREFIX + "/");
        }
        if (imageType == ImageType.GROUP_ORDER) {
            directoryPath += (GROUP_ORDER_IMAGE_PREFIX + "/");
        }
        if (imageType == ImageType.TIME_TABLE) {
            directoryPath += (TIME_TABLE_IMAGE_PREFIX + "/");
        }
        if (imageType == ImageType.COMPLAINT) {
            directoryPath += (COMPLAINT_IMAGE_PREFIX + "/");
        }
        if (imageType == ImageType.COMPLAINT_REPLY) {
            directoryPath += (COMPLAINT_REPLY_IMAGE_PREFIX + "/");
        }
        if (imageType == ImageType.POPUP_NOTIFICATION) {
            directoryPath += (POPUP_NOTIFICATION_IMAGE_PREFIX + "/");
        }

        return directoryPath;
    }

    private void createDirectoryDetail(String directoryPath) {
        File directory = new File(directoryPath);
        if (doesNotDirectoryExists(directory)) {
            boolean created = directory.mkdirs();
            if (!created) {
                log.error("폴더를 만드는 것을 실패했습니다: {}", directoryPath);
                throw new CustomException(IMAGE_DIRECTORY_SAVE_FAIL);
            }
        }
    }

    private static boolean doesNotDirectoryExists(File directory) {
        return !directory.exists();
    }

    private void createAndSaveImages(String directoryPath, List<MultipartFile> images, Long entityId, ImageType imageType) {
        for (MultipartFile imageFile : images) {
           createAndSaveImage(directoryPath, imageFile, entityId, imageType);
        }
    }

    private void createAndSaveImage(String directoryPath, MultipartFile image, Long entityId, ImageType imageType) {
        String imageName = createImageName(entityId, image, imageType);
        try {
            File savedImageInDirectory = saveImageToDirectory(directoryPath, image, imageName);
            saveImageToDB(entityId, imageType, savedImageInDirectory, imageName);
        } catch (IOException e) {
            log.error("{} 타입의 entityId : {} 이미지가 저장 실패했습니다 ", imageType, entityId, e);
            throw new CustomException(IMAGE_SAVE_FAIL);
        }
    }

    private String createImageName(Long entityId, MultipartFile imageFile, ImageType imageType) {
        String imageName = createFixedImageName(entityId, imageFile);

        if (imageType == ImageType.USER) {
            imageName = USER_IMAGE_PREFIX + "_" + imageName;
        }
        if (imageType == ImageType.TIP) {
            imageName = TIP_IMAGE_PREFIX + "_" + imageName;
        }
        if (imageType == ImageType.GROUP_ORDER) {
            imageName = GROUP_ORDER_IMAGE_PREFIX + "_" + imageName;
        }
        if (imageType == ImageType.TIME_TABLE) {
            imageName = TIME_TABLE_IMAGE_PREFIX + "_" + imageName;
        }
        if (imageType == ImageType.COMPLAINT) {
            imageName = COMPLAINT_IMAGE_PREFIX + "_" + imageName;
        }
        if (imageType == ImageType.COMPLAINT_REPLY) {
            imageName = COMPLAINT_REPLY_IMAGE_PREFIX + "_" + imageName;
        }
        if (imageType == ImageType.POPUP_NOTIFICATION) {
            imageName = POPUP_NOTIFICATION_IMAGE_PREFIX + "_" + imageName;
        }


        return imageName;
    }

    private String createFixedImageName(Long entityId, MultipartFile imageFile) {
        String fileExtension = getFileExtension(imageFile.getOriginalFilename());
        String uuid = createImageUUID();
        String imageName = entityId + "_" + uuid + fileExtension;
        return imageName;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return DEFAULT_IMAGE_EXTENSION;
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return DEFAULT_IMAGE_EXTENSION;
        }

        return fileName.substring(lastDotIndex).toLowerCase();
    }

    private String createImageUUID() {
        return UUID.randomUUID().toString();
    }

    private File saveImageToDirectory(String directoryPath, MultipartFile imageFile, String imageName) throws IOException {
        File destinationFile = new File(directoryPath + imageName);
        imageFile.transferTo(destinationFile);

        log.info("Image transfer successfully: {}", destinationFile.getAbsolutePath());

        return destinationFile;
    }

    private void saveImageToDB(Long entityId, ImageType imageType, File destinationFile, String imageName) {
        Image image = Image.of(destinationFile.getAbsolutePath(), imageName, imageType, entityId);
        imageRepository.save(image);
    }

    private List<Image> findImagesByImageTypeAndEntityId(ImageType imageType, Long entityId) {
        return imageRepository.findByImageTypeAndEntityId(imageType, entityId);
    }

    private Optional<Image> findImageByImageTypeAndEntityId(ImageType imageType, Long entityId) {
        List<Image> images = imageRepository.findByImageTypeAndEntityId(imageType, entityId);

        if (images == null || images.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(imageRepository.findByImageTypeAndEntityId(imageType, entityId).get(0));
    }

    private List<ImageLinkDto> createImageDtos(ImageType imageType, HttpServletRequest request, List<Image> images) {
        List<ImageLinkDto> imageLinkDtos = new ArrayList<>();

        for (Image image : images) {
            ImageLinkDto imageLinkDto = createImageDto(imageType, request, image);
            imageLinkDtos.add(imageLinkDto);
        }

        return imageLinkDtos;
    }

    public ImageLinkDto createImageDto(ImageType imageType, HttpServletRequest request, Optional<Image> optionalImage) {
        if (isOptionalImageNull(optionalImage)) {
            return ImageLinkDto.ofNull();
        }
        Image image = optionalImage.get();

        File findImage = getImageInDirectory(image);

        checkImageAndDirectoryExists(image, findImage);

        String staticImageUrl = createImageUrl(imageType, request, image);

        String contentType = getImageContentType(findImage);

        String imageName = getImageName(image.getImagePath());

        return ImageLinkDto.of(imageName, staticImageUrl, contentType, findImage.length());
    }

    public ImageLinkDto createImageDto(ImageType imageType, HttpServletRequest request, Image image) {
        File findImage = getImageInDirectory(image);

        checkImageAndDirectoryExists(image, findImage);

        String staticImageUrl = createImageUrl(imageType, request, image);

        String contentType = getImageContentType(findImage);

        String imageName = getImageName(image.getImagePath());

        return ImageLinkDto.of(imageName, staticImageUrl, contentType, findImage.length());
    }

    private static boolean isOptionalImageNull(Optional<Image> optionalImage) {
        return !optionalImage.isPresent();
    }

    private String createImageUrl(ImageType imageType, HttpServletRequest request, Image image) {
        String appcenterHttpsUrl = getAppcenterHttpsUrl(request);
        String imagePath = (image != null) ? image.getImagePath() : null;
        String staticImageUrl = findStaticImageUrl(imageType, imagePath, appcenterHttpsUrl);
        return staticImageUrl != null ? staticImageUrl.replace("http", "https") : null;
    }

    private File getImageInDirectory(Image image) {
        File file = new File(image.getImagePath());
        return file;
    }

    private void checkImageAndDirectoryExists(Image image, File file) {
        if (!file.exists()) {
            log.error("이미지 파일이 존재하지 않습니다 : {}", image.getImagePath());

            // 디렉토리 존재 여부도 확인
            File directory = file.getParentFile();
            log.error("해당 이미지의 폴더가 존재한다에 대한 여부 : {}", directory.exists());
            if (directory.exists()) {
                log.error("폴더는 존재하는데 이미지 파일이 존재하지 않는다: {}", Arrays.toString(directory.listFiles()));
            }

            throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
        }
    }

    private String getAppcenterHttpsUrl(HttpServletRequest request) {
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

    private String getImageContentType(File file) {
        try {
            String fileName = file.getName().toLowerCase();

            if (fileName.endsWith(DEFAULT_IMAGE_EXTENSION) || fileName.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                return "image/png";
            } else if (fileName.endsWith(".gif")) {
                return "image/gif";
            } else if (fileName.endsWith(".webp")) {
                return "image/webp";
            } else if (fileName.endsWith(".svg")) {
                return "image/svg+xml";
            } else if (fileName.endsWith(".heic")) {
                return "image/heic";
            }

            try {
                String detectedType = Files.probeContentType(file.toPath());
                if (detectedType != null && detectedType.startsWith("image/")) {
                    return detectedType;
                }
            } catch (Exception e) {
                log.warn("파일의 콘텐츠 타입을 감지할 수 없습니다: {}", file.getName());
            }

            return "image/jpeg";
        } catch (Exception e) {
            log.error("파일의 콘텐츠 타입을 결정하는 중 오류가 발생했습니다: {}", file.getName(), e);
            return "image/jpeg";
        }
    }

    private String getImageName(String imagePath) {
        return Paths.get(imagePath).getFileName().toString();
    }

    private void deleteImageFromDirectory(Image image) {
        File findImage = new File(image.getImagePath());
        if (findImage.exists()) {
            if (canNotDeleteImage(findImage)) {
                log.warn("{} 이미지 삭제에 실패했습니다", image.getImagePath());
            }
        }
    }

    private boolean canNotDeleteImage(File findImage) {
        return !findImage.delete();
    }

    private void deleteImageFromDB(Image image) {
        imageRepository.delete(image);
    }
}