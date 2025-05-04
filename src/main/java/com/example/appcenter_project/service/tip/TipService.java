package com.example.appcenter_project.service.tip;

import com.example.appcenter_project.dto.request.tip.RequestTipDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipCommentDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.dto.response.tip.TipImageDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChatRoom;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.like.TipLike;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.entity.tip.TipComment;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.image.ImageType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public void saveTip(Long userId, RequestTipDto requestTipDto, List<MultipartFile> images) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Tip tip = Tip.builder()
                .title(requestTipDto.getTitle())
                .content(requestTipDto.getContent())
                .user(user)
                .build();

        saveImages(tip, images);
        tipRepository.save(tip);
    }

    public List<TipImageDto> findTipImages(Long tipId) {
        Tip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new RuntimeException("Tip not found with ID: " + tipId));

        List<Image> imageList = tip.getImageList();
        List<TipImageDto> TipImageDtoList = new ArrayList<>();

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
                throw new RuntimeException("Could not determine file type.", e);
            }

            String filename = file.getName();
            String url = "/api/images/view?filename=" + filename;

            TipImageDto tipImageDto = TipImageDto.builder()
                    .filename(filename)
                    .contentType(contentType)
                    .build();

            TipImageDtoList.add(tipImageDto);
        }

        return TipImageDtoList;
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
            throw new RuntimeException("File not found: " + filename);
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

    public ResponseTipDto findTip(Long tipId) {
        Tip tip = tipRepository.findById(tipId).orElseThrow();
        List<ResponseTipCommentDto> responseTipCommentDtoList = findTipComment(tip);

        return ResponseTipDto.builder()
                .title(tip.getTitle())
                .content(tip.getContent())
                .tipLike(tip.getTipLike())
                .tipCommentDtoList(responseTipCommentDtoList)
                .build();
    }

    public List<ResponseTipDto> findAllTips() {
        List<ResponseTipDto> responseTipDtoList = new ArrayList<>();
        List<Tip> tips = tipRepository.findAll();
        for (Tip tip : tips) {
            ResponseTipDto responseTipDto = ResponseTipDto.entityToDtoList(tip);
            responseTipDtoList.add(responseTipDto);
        }

        return responseTipDtoList;
    }

    // 하나의 tip 게시판에 있는 모든 tip 댓글 조회
    private List<ResponseTipCommentDto> findTipComment(Tip tip) {
        List<ResponseTipCommentDto> responseTipCommentDtoList = new ArrayList<>();
        List<TipComment> tipCommentList = tipCommentRepository.findByTip_IdAndParentTipCommentIsNull(tip.getId());
        for (TipComment tipComment : tipCommentList) {
            List<ResponseTipCommentDto> childResponseComments = new ArrayList<>();
            List<TipComment> childTipComments = tipComment.getChildTipComments();
            for (TipComment childGroupOrderComment : childTipComments) {
                ResponseTipCommentDto build = ResponseTipCommentDto.builder()
                        .tipCommentId(childGroupOrderComment.getId())
                        .userId(tip.getUser().getId())
                        .reply(childGroupOrderComment.getReply())
                        .build();

                childResponseComments.add(build);
            }
            ResponseTipCommentDto responseTipCommentDto = ResponseTipCommentDto.builder()
                    .tipCommentId(tipComment.getId())
                    .userId(tip.getUser().getId())
                    .reply(tipComment.getReply())
                    .childTipCommentList(childResponseComments)
                    .build();
            responseTipCommentDtoList.add(responseTipCommentDto);

        }
        return responseTipCommentDtoList;
    }

    public Integer likePlusTip(Long userId, Long tipId) {
        Tip tip = tipRepository.findById(tipId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

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

    public void updateTip(Long userId, RequestTipDto requestTipDto, List<MultipartFile> images, Long tipId) {
        Tip tip = tipRepository.findById(tipId).orElseThrow();
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

    public void deleteTip(Long tipId) {
        tipRepository.deleteById(tipId);
    }
}
