package com.example.appcenter_project.service.tip;

import com.example.appcenter_project.dto.request.tip.RequestTipCommentDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipCommentDto;

import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.entity.tip.TipComment;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.tip.TipCommentRepository;
import com.example.appcenter_project.repository.tip.TipRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TipCommentService {

    private final TipCommentRepository tipCommentRepository;
    private final UserRepository userRepository;
    private final TipRepository tipRepository;

    public ResponseTipCommentDto saveTipComment(Long userId, RequestTipCommentDto requestTipCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Tip tip = tipRepository.findById(requestTipCommentDto.getTipId())
                .orElseThrow(() -> new CustomException(TIP_NOT_FOUND));

        TipComment tipComment;

        if (requestTipCommentDto.getParentCommentId() == null) {
            tipComment = TipComment.builder()
                    .reply(requestTipCommentDto.getReply())
                    .tip(tip)
                    .user(user)
                    .build();
            tipComment.setParentTipCommentNull();
        } else {
            TipComment parentTipComment = tipCommentRepository.findById(requestTipCommentDto.getParentCommentId())
                    .orElseThrow(() -> new CustomException(TIP_COMMENT_NOT_FOUND));
            tipComment = TipComment.builder()
                    .reply(requestTipCommentDto.getReply())
                    .tip(tip)
                    .user(user)
                    .parentTipComment(parentTipComment)
                    .build();
            parentTipComment.addChildTipComments(tipComment);
        }

        tipCommentRepository.save(tipComment);

        tip.plusTipCommentCount();

        return ResponseTipCommentDto.entityToDto(tipComment, user);
    }

    public List<ResponseTipCommentDto> findTipComment(Long userId, Long tipId) {
        List<ResponseTipCommentDto> responseTipCommentDtoList = new ArrayList<>();
        List<TipComment> tipCommentList = tipCommentRepository.findByTip_IdAndParentTipCommentIsNull(tipId);

        for (TipComment tipComment : tipCommentList) {
            List<ResponseTipCommentDto> childResponseComments = new ArrayList<>();
            for (TipComment child : tipComment.getChildTipComments()) {
                childResponseComments.add(ResponseTipCommentDto.builder()
                        .tipCommentId(child.getId())
                        .userId(userId)
                        .reply(child.getReply())
                        .build());
            }
            responseTipCommentDtoList.add(ResponseTipCommentDto.builder()
                    .tipCommentId(tipComment.getId())
                    .userId(userId)
                    .reply(tipComment.getReply())
                    .childTipCommentList(childResponseComments)
                    .build());
        }

        return responseTipCommentDtoList;
    }

    public void deleteTipComment(Long userId, Long tipCommentId) {
        TipComment tipComment = tipCommentRepository.findByIdAndUserId(tipCommentId, userId).orElseThrow(() -> new CustomException(TIP_COMMENT_NOT_OWNED_BY_USER));
        Tip tip = tipComment.getTip();

        tipComment.updateIsDeleted();
        tip.minusTipCommentCount();
    }
}
