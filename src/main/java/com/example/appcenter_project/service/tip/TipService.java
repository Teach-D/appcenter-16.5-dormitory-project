package com.example.appcenter_project.service.tip;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.tip.RequestTipDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipCommentDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDetailDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.dto.response.tip.TipImageDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.like.TipLike;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.entity.tip.TipComment;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.mapper.TipMapper;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.like.TipLikeRepository;
import com.example.appcenter_project.repository.tip.TipCommentRepository;
import com.example.appcenter_project.repository.tip.TipRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TipService {

    private final TipRepository tipRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final TipCommentRepository tipCommentRepository;
    private final TipLikeRepository tipLikeRepository;
    private final TipMapper tipMapper;

    public void saveTip(Long userId, RequestTipDto requestTipDto, List<MultipartFile> images) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Tip tip = Tip.builder()
                .title(requestTipDto.getTitle())
                .content(requestTipDto.getContent())
                .user(user)
                .build();

        // 양방향 매핑
        user.addTip(tip);

        tipRepository.save(tip);

        if (images != null) {
            saveImages(tip, images);
        }
    }

    public List<TipImageDto> findTipImages(Long tipId) {
        Tip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(TIP_NOT_FOUND));

        List<Image> imageList = tip.getImageList();
        List<TipImageDto> tipImageDtoList = new ArrayList<>();

        for (Image image : imageList) {
            File file = new File(image.getFilePath());
            if (!file.exists()) {
                throw new RuntimeException("Image file not found at path: " + image.getFilePath());
            }

            String contentType;
            try {
                contentType = Files.probeContentType(file.toPath());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            } catch (IOException e) {
                throw new CustomException(IMAGE_NOT_FOUND);
            }

            String filename = file.getName();
            String url = "/api/images/view?filename=" + filename;

            TipImageDto tipImageDto = TipImageDto.builder()
                    .filename(filename)
                    .contentType(contentType)
                    .build();

            tipImageDtoList.add(tipImageDto);
        }

        return tipImageDtoList;
    }

    private void saveImages(Tip tip, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            // 개발 환경에 맞는 경로 설정
            String basePath = System.getProperty("user.dir");
            String imagePath = basePath + "/images/tip/";

            // 디렉토리 생성 (존재하지 않으면)
            File directory = new File(imagePath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    log.error("Failed to create tip directory: {}", imagePath);
                    throw new CustomException(IMAGE_NOT_FOUND);
                }
            }

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    log.warn("Empty file skipped during tip image save");
                    continue;
                }

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String fileExtension = getFileExtension(file.getOriginalFilename());
                String uuid = UUID.randomUUID().toString();
                String imageFileName = "tip_" + tip.getId() + "_" + uuid + timestamp + fileExtension;
                File destinationFile = new File(imagePath + imageFileName);

                try {
                    file.transferTo(destinationFile);
                    log.info("Tip image saved successfully: {}", destinationFile.getAbsolutePath());

                    Image image = Image.builder()
                            .filePath(destinationFile.getAbsolutePath())
                            .isDefault(false)
                            .imageType(ImageType.TIP)
                            .boardId(tip.getId())
                            .build();

                    imageRepository.save(image);
                    tip.getImageList().add(image);

                } catch (IOException e) {
                    log.error("Failed to save tip image file for tip {}: ", tip.getId(), e);
                    throw new CustomException(IMAGE_NOT_FOUND);
                }
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

    public Resource loadImageAsResource(String filename) {
        String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/images/tip/";
        File file = new File(projectPath + filename);

        if (!file.exists()) {
            throw new CustomException(IMAGE_NOT_FOUND);
        }

        return new FileSystemResource(file);
    }

    public String getImageContentType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            return (contentType != null) ? contentType : "application/octet-stream";
        } catch (IOException e) {
            throw new RuntimeException("Could not determine file type.", e);
        }
    }

    public ResponseTipDetailDto findTip(CustomUserDetails user, Long tipId) {
        ResponseTipDetailDto flatDto = tipMapper.findTip(tipId);

        // 현재 유저가 해당 팁 게시글의 좋아요를 누른 유저인지 확인
        // 로그인 한 경우일 때
        if (user != null) {
            User loginUser = userRepository.findById(user.getId()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

            // 로그인한 유저가 해당 게시글의 좋아요를 누른 경우 true 반환
            if(tipLikeRepository.existsByUserIdAndTipId(loginUser.getId(), tipId) == true) {
                flatDto.updateIsCheckLikeCurrentUser(true);
            }
            // 로그인한 유저가 해당 게시글의 좋아요를 누르지 않은 경우 false 반환
            else {
                flatDto.updateIsCheckLikeCurrentUser(false);
            }
        }


        if (flatDto == null) {
            throw new CustomException(TIP_NOT_FOUND);
        }

        List<ResponseTipCommentDto> flatComments = flatDto.getTipCommentDtoList();
        Map<Long, ResponseTipCommentDto> parentMap = new LinkedHashMap<>();
        List<ResponseTipCommentDto> topLevelComments = new ArrayList<>();

        for (ResponseTipCommentDto comment : flatComments) {
            // 삭제된 댓글 내용 처리
            if (Boolean.TRUE.equals(comment.getIsDeleted())) {
                comment.updateReply("삭제된 메시지입니다.");
            }

            // 댓글 계층 구조 구성
            if (comment.getParentId() == null) {
                comment.updateChildTipCommentList(new ArrayList<>());
                parentMap.put(comment.getTipCommentId(), comment);
                topLevelComments.add(comment);
            } else {
                ResponseTipCommentDto parent = parentMap.get(comment.getParentId());
                if (parent != null) {
                    if (parent.getChildTipCommentList() == null) {
                        parent.updateChildTipCommentList(new ArrayList<>());
                    }
                    parent.getChildTipCommentList().add(comment);
                }
            }
        }

        flatDto.updateTipCommentDtoList(topLevelComments);
        return flatDto;
    }




    /*public ResponseTipDetailDto findTip(Long tipId) {
        ResponseTipDetailDto tip1 = tipMapper.findTip(tipId);
        Tip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(TIP_NOT_FOUND));
        List<ResponseTipCommentDto> responseTipCommentDtoList = findTipComment(tip);
        List<Long> tipLikeUserList = new ArrayList<>();

        List<TipLike> tipLikeList = tip.getTipLikeList();
        for (TipLike tipLike : tipLikeList) {
            Long tipLikeUserId = tipLike.getUser().getId();
            tipLikeUserList.add(tipLikeUserId);
        }
        return ResponseTipDetailDto.entityToDto(tip, responseTipCommentDtoList, tipLikeUserList);
    }*/

    public List<ResponseTipDto> findAllTips() {
        List<ResponseTipDto> tips = tipMapper.findTips();
        Collections.reverse(tips);
        return tips;
/*        List<ResponseTipDto> responseTipDtoList = new ArrayList<>();
        List<Tip> tips = tipRepository.findAll();
        for (Tip tip : tips) {
            ResponseTipDto responseTipDto = ResponseTipDto.entityToDto(tip);
            responseTipDtoList.add(responseTipDto);
        }

        return responseTipDtoList;*/
    }

    /**
     * 하루 동안 고정된 랜덤 Tip 3개를 조회합니다.
     * 날짜를 기준으로 시드값을 생성하여 같은 날에는 항상 같은 3개의 Tip이 반환됩니다.
     *
     * @return 일일 랜덤 Tip 3개 목록
     */
    public List<ResponseTipDto> findDailyRandomTips() {
        List<ResponseTipDto> allTips = tipMapper.findTips();

        // Tip이 3개 미만인 경우 빈 리스트 반환
        if (allTips.size() < 3) {
            log.info("Available tips count: {}. Need at least 3 tips for daily random selection.", allTips.size());
            return new ArrayList<>();
        }

        // 현재 날짜를 기준으로 시드값 생성 (YYYY-MM-DD 형태)
        java.time.LocalDate today = java.time.LocalDate.now();
        long seed = today.toEpochDay(); // 1970-01-01부터의 일수를 시드로 사용

        Random random = new Random(seed);

        // 전체 Tip 목록을 복사하여 섞기
        List<ResponseTipDto> shuffledTips = new ArrayList<>(allTips);
        Collections.shuffle(shuffledTips, random);

        // 첫 3개 선택
        List<ResponseTipDto> dailyRandomTips = shuffledTips.subList(0, 3);

        log.info("Daily random tips selected for date {}: {} tips selected from {} total tips",
                today, dailyRandomTips.size(), allTips.size());

        return dailyRandomTips;
    }

    // 하나의 tip 게시판에 있는 모든 tip 댓글 조회
    private List<ResponseTipCommentDto> findTipComment(Tip tip) {
        List<ResponseTipCommentDto> responseTipCommentDtoList = new ArrayList<>();
        List<TipComment> tipCommentList = tipCommentRepository.findByTip_IdAndParentTipCommentIsNull(tip.getId());
        for (TipComment tipComment : tipCommentList) {
            List<ResponseTipCommentDto> childResponseComments = new ArrayList<>();
            List<TipComment> childTipComments = tipComment.getChildTipComments();
            for (TipComment childTipComment : childTipComments) {
                ResponseTipCommentDto build = ResponseTipCommentDto.builder()
                        .tipCommentId(childTipComment.getId())
                        .userId(childTipComment.getUser().getId())
                        .reply(childTipComment.isDeleted() ? "삭제된 메시지입니다." : childTipComment.getReply())
                        .build();

                childResponseComments.add(build);
            }
            ResponseTipCommentDto responseTipCommentDto = ResponseTipCommentDto.builder()
                    .tipCommentId(tipComment.getId())
                    .userId(tipComment.getUser().getId())
                    .reply(tipComment.isDeleted() ? "삭제된 메시지입니다." : tipComment.getReply())
                    .childTipCommentList(childResponseComments)
                    .build();
            responseTipCommentDtoList.add(responseTipCommentDto);

        }
        return responseTipCommentDtoList;
    }

    public Integer likePlusTip(Long userId, Long tipId) {
        Tip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(TIP_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 좋아요를 누른 유저가 또 좋아요를 할려는 경우 예외처리
        if (tipLikeRepository.existsByUserAndTip(user, tip)) {
            throw new CustomException(ALREADY_TIP_LIKE_USER);
        }

        TipLike tipLike = TipLike.builder()
                .user(user)
                .tip(tip)
                .build();

        tipLikeRepository.save(tipLike);

        // user에 좋아요 정보 추가
        user.addLike(tipLike);

        // tip에 좋아요 정보 추가 orphanRemoval 위한 설정
        tip.getTipLikeList().add(tipLike);

        return tip.plusLike();
    }

    public Integer unlikePlusTip(Long userId, Long tipId) {
        Tip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(TIP_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 좋아요를 누르지 않은 유저가 좋아요 취소를 할려는 경우 예외처리
        if (!tipLikeRepository.existsByUserAndTip(user, tip)) {
            throw new CustomException(NOT_LIKED_TIP);
        }

        TipLike tipLike = tipLikeRepository.findByUserAndTip(user, tip)
                .orElseThrow(() -> new CustomException(TIP_LIKE_NOT_FOUND));

        // user에서 좋아요 정보 제거
        user.removeLike(tipLike);

        // tip에서 좋아요 정보 제거 (orphanRemoval)
        tip.getTipLikeList().remove(tipLike);

        tipLikeRepository.delete(tipLike);

        return tip.minusLike();
    }

    public void updateTip(Long userId, RequestTipDto requestTipDto, List<MultipartFile> images, Long tipId) {
        Tip tip = tipRepository.findByIdAndUserId(tipId, userId).orElseThrow(() -> new CustomException(TIP_NOT_OWNED_BY_USER));

        tip.update(requestTipDto);

        // 이미지가 제공된 경우에만 기존 이미지를 삭제하고 새로운 이미지를 저장
        if (images != null && !images.isEmpty()) {
            // 기존 이미지들이 있다면 파일 및 DB에서 삭제
            List<Image> existingImages = tip.getImageList();
            for (Image existingImage : existingImages) {
                File oldFile = new File(existingImage.getFilePath());
                if (oldFile.exists()) {
                    boolean deleted = oldFile.delete();
                    if (!deleted) {
                        log.warn("Failed to delete old tip image file: {}", existingImage.getFilePath());
                    }
                }
                // 기존 이미지 엔티티 삭제
                imageRepository.delete(existingImage);
            }
            tip.getImageList().clear(); // Tip에서 이미지 목록 비우기

            // 새로운 이미지들 저장
            saveImages(tip, images);
        }
    }

    public void deleteTip(Long userId, Long tipId) {
        Tip tip = tipRepository.findByIdAndUserId(tipId, userId).orElseThrow(() -> new CustomException(TIP_NOT_OWNED_BY_USER));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        user.removeTip(tip);
        tipRepository.deleteById(tipId);
    }

    // Tip 이미지 URL 목록 조회
    public List<ImageLinkDto> findTipImageUrlsByTipId(Long tipId, HttpServletRequest request) {
        // 팁 존재 확인
        Tip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(TIP_NOT_FOUND));

        List<Image> tipImages = imageRepository.findAllByBoardIdAndImageType(tipId, ImageType.TIP);

        if (tipImages.isEmpty()) {
            log.info("No images found for tip {}", tipId);
            return new ArrayList<>(); // 빈 리스트 반환
        }

        // BaseURL 생성
        String baseUrl = getBaseUrl(request);
        List<ImageLinkDto> imageLinkDtos = new ArrayList<>();

        for (Image image : tipImages) {
            File file = new File(image.getFilePath());
            if (file.exists()) {
                String imageUrl = baseUrl + "/api/images/tip/" + image.getId();

                // 정적 리소스 URL 생성 (User와 동일한 방식)
                String staticImageUrl = getStaticTipImageUrl(image.getFilePath(), baseUrl);
                String changeUrl = staticImageUrl.replace("http", "https");

                String contentType = getSafeContentType(file);

                ImageLinkDto imageLinkDto = ImageLinkDto.builder()
                        .imageUrl(imageUrl)
                        .fileName(changeUrl)  // 정적 리소스로 직접 접근 가능한 URL
                        .contentType(contentType)
                        .fileSize(file.length())
                        .build();

                imageLinkDtos.add(imageLinkDto);
            } else {
                log.warn("Tip image file not found: {}", image.getFilePath());
            }
        }

        log.info("Found {} valid images for tip {}", imageLinkDtos.size(), tipId);
        return imageLinkDtos;
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

    // BaseURL 생성 헬퍼 메서드
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }
        baseUrl.append(contextPath);

        return baseUrl.toString();
    }

    // 안전한 Content Type 확인 헬퍼 메서드
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

    // Tip의 모든 이미지 삭제
    public void deleteTipImages(Long userId, Long tipId) {
        // 팁 소유자 확인
        Tip tip = tipRepository.findByIdAndUserId(tipId, userId)
                .orElseThrow(() -> new CustomException(TIP_NOT_OWNED_BY_USER));

        List<Image> tipImages = imageRepository.findAllByBoardIdAndImageType(tipId, ImageType.TIP);

        for (Image image : tipImages) {
            // 파일 삭제
            File file = new File(image.getFilePath());
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    log.warn("Failed to delete tip image file: {}", image.getFilePath());
                }
            } else {
                log.warn("Tip image file not found: {}", image.getFilePath());
            }

            // 데이터베이스에서 이미지 삭제
            imageRepository.delete(image);
        }

        // Tip 엔티티에서도 이미지 목록 정리
        tip.getImageList().clear();

        log.info("Successfully deleted {} images for tip {}", tipImages.size(), tipId);
    }
}
