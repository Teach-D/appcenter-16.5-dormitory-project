package com.example.appcenter_project.service.tip;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.tip.RequestTipDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipCommentDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDetailDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
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
import com.example.appcenter_project.service.image.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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
    private final ImageService imageService;

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
            imageService.saveImages(ImageType.TIP, tip.getId(), images);
        }

        log.info("Tip 저장 성공 userId : {}, tipId : {}", userId, tip.getId());
    }

    public ResponseTipDetailDto findTip(CustomUserDetails user, Long tipId, HttpServletRequest request) {
        log.info("[findTip] tipId={} 조회 시작", tipId);

        ResponseTipDetailDto flatDto = tipMapper.findTip(tipId);
        Tip tip = tipRepository.findById(tipId).orElseThrow(() -> new CustomException(TIP_NOT_FOUND));
        Long tipWriterId = tip.getUser().getId();

        flatDto.updateWriterName(tip.getUser().getName());

        // 작성자 이미지 추가
        String writerImageUrl = imageService.findStaticImageUrl(ImageType.USER, tipWriterId, request);
        flatDto.updateWriterImageUrl(writerImageUrl);

        log.debug("[findTip] Tip 작성자 ID: {}", tipWriterId);

        // 현재 유저가 해당 팁 게시글의 좋아요를 누른 유저인지 확인
        // 로그인 한 경우일 때
        if (user != null) {
            User loginUser = userRepository.findById(user.getId()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

            // 로그인한 유저가 해당 게시글의 좋아요를 누른 경우 true 반환
            if(tipLikeRepository.existsByUserIdAndTipId(loginUser.getId(), tipId) == true) {
                log.info("[findTip] 로그인 유저가 해당 Tip에 좋아요를 누른 경우");
                flatDto.updateIsCheckLikeCurrentUser(true);
            }
            // 로그인한 유저가 해당 게시글의 좋아요를 누르지 않은 경우 false 반환
            else {
                log.info("[findTip] 로그인 유저가 해당 Tip에 좋아요를 누르지 않은 경우");
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

                // 대댓글 작성자 이미지 url
                Long userId = comment.getUserId();
                String commentWriterImageUrl = imageService.findStaticImageUrl(ImageType.USER, userId, request);

                comment.updateWriterImageFile(commentWriterImageUrl);

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
                Long userId = comment.getUserId();
                String commentWriterImageUrl = imageService.findStaticImageUrl(ImageType.USER, userId, request);

                comment.updateWriterImageFile(commentWriterImageUrl);
            }
        }

        flatDto.updateTipCommentDtoList(topLevelComments);

        log.info("[findTip] tipId={} 조회 완료", tipId);

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

        log.info("모든 Tip 조회 성공");

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
            log.info("사용 가능한 팁 수: {}개. 일일 무작위 선정을 위해 최소 3개 이상의 팁이 필요합니다.", allTips.size());
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

        log.info("{} 날짜에 대해 일일 무작위 팁이 선택되었습니다: 전체 {}개 중 {}개가 선택되었습니다.",
                today, allTips.size(), dailyRandomTips.size());

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
        log.info("[likePlusTip] userId={}가 tipId={}에 좋아요 요청", userId, tipId);

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

        Integer likeCount = tip.plusLike();
        log.info("[likePlusTip] tipId={}의 좋아요 수 증가: {}", tipId, likeCount);

        return likeCount;
    }

    public Integer unlikePlusTip(Long userId, Long tipId) {
        log.info("[unlikePlusTip] userId={}가 tipId={}에 대한 좋아요 취소 요청", userId, tipId);

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

        log.info("[unlikePlusTip] userId={}가 tipId={}에 대한 좋아요 취소 성공", userId, tipId);

        return tip.minusLike();
    }

    public void updateTip(Long userId, RequestTipDto requestTipDto, List<MultipartFile> images, Long tipId) {
        log.info("[updateTip] userId={}가 tipId={}에 대한 팁 수정 요청", userId, tipId);

        Tip tip = tipRepository.findByIdAndUserId(tipId, userId).orElseThrow(() -> new CustomException(TIP_NOT_OWNED_BY_USER));

        tip.update(requestTipDto);
        log.debug("[updateTip] tipId={} 내용 업데이트 성공", tipId);

        imageService.updateImages(ImageType.TIP, tipId, images);
    }

    public void deleteTip(Long userId, Long tipId) {
        log.info("[deleteTip] userId={}가 tipId={} 삭제 요청", userId, tipId);

        Tip tip = tipRepository.findByIdAndUserId(tipId, userId).orElseThrow(() -> new CustomException(TIP_NOT_OWNED_BY_USER));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        tipRepository.deleteById(tipId);
        imageService.deleteImages(ImageType.TIP, tipId);
        log.info("[deleteTip] tipId={} 삭제 완료", tipId);
    }

    // Tip 이미지 URL 목록 조회
    public List<ImageLinkDto> findTipImageUrlsByTipId(Long tipId, HttpServletRequest request) {
        log.info("[findTipImageUrlsByTipId] tipId={}에 대한 이미지 URL 목록 조회 요청", tipId);

        // 팁 존재 확인
        Tip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(TIP_NOT_FOUND));

        return imageService.findImages(ImageType.TIP, tipId, request);
    }

    // Tip의 모든 이미지 삭제
    public void deleteTipImages(Long userId, Long tipId) {
        log.info("[deleteTipImages] tipId={}에 대한 이미지 삭제 요청 by userId={}", tipId, userId);

        // 팁 소유자 확인
        Tip tip = tipRepository.findByIdAndUserId(tipId, userId)
                .orElseThrow(() -> new CustomException(TIP_NOT_OWNED_BY_USER));

        imageService.deleteImages(ImageType.TIP, tipId);
    }
}
