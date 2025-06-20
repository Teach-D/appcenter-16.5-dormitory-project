package com.example.appcenter_project.service.tip;

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

        saveImages(tip, images);
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
            String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/images/tip/";
            File directory = new File(projectPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            for (MultipartFile file : files) {
                String uuid = UUID.randomUUID().toString();
                String imageFileName = uuid + "_" + file.getOriginalFilename();
                File destinationFile = new File(projectPath + imageFileName);

                try {
                    file.transferTo(destinationFile);

                    Image image = Image.builder()
                            .filePath(destinationFile.getAbsolutePath())
                            .isDefault(false)
                            .imageType(ImageType.TIP)
                            .boardId(tip.getId())
                            .build();

                    imageRepository.save(image);
                    tip.getImageList().add(image);

                } catch (IOException e) {
                    throw new RuntimeException("Failed to save image", e);
                }
            }
        }
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

    public ResponseTipDetailDto findTip(Long tipId) {
        ResponseTipDetailDto flatDto = tipMapper.findTip(tipId);
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
        return tipMapper.findTips();
/*        List<ResponseTipDto> responseTipDtoList = new ArrayList<>();
        List<Tip> tips = tipRepository.findAll();
        for (Tip tip : tips) {
            ResponseTipDto responseTipDto = ResponseTipDto.entityToDto(tip);
            responseTipDtoList.add(responseTipDto);
        }

        return responseTipDtoList;*/
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

        List<Image> imageList = tip.getImageList();
        for (Image image : imageList) {
            File file = new File(image.getFilePath());
            if (file.exists()) {
                file.delete(); // resources에서 파일 삭제
            }
        }
        tip.getImageList().clear(); // Tip에서 이미지 목록 비우기

        saveImages(tip, images);
    }

    public void deleteTip(Long userId, Long tipId) {
        Tip tip = tipRepository.findByIdAndUserId(tipId, userId).orElseThrow(() -> new CustomException(TIP_NOT_OWNED_BY_USER));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        user.removeTip(tip);
        tipRepository.deleteById(tipId);
    }


}
