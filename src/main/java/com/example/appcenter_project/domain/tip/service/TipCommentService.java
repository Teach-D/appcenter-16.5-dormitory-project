package com.example.appcenter_project.domain.tip.service;

import com.example.appcenter_project.domain.tip.dto.request.RequestTipCommentDto;
import com.example.appcenter_project.domain.tip.dto.response.ResponseTipCommentDto;

import com.example.appcenter_project.domain.tip.entity.Tip;
import com.example.appcenter_project.domain.tip.entity.TipComment;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.tip.repository.TipCommentRepository;
import com.example.appcenter_project.domain.tip.repository.TipRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TipCommentService {

    private final TipCommentRepository tipCommentRepository;
    private final UserRepository userRepository;
    private final TipRepository tipRepository;

    // ========== Public Methods ========== //

    public ResponseTipCommentDto saveTipComment(Long userId, RequestTipCommentDto requestTipCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Tip tip = tipRepository.findById(requestTipCommentDto.getTipId())
                .orElseThrow(() -> new CustomException(TIP_NOT_FOUND));

        TipComment tipComment = createTimeComment(requestTipCommentDto, tip, user);
        tipCommentRepository.save(tipComment);

        tip.plusTipCommentCount();

        return ResponseTipCommentDto.entityToDto(tipComment, user);
    }

    public void deleteTipComment(Long userId, Long tipCommentId) {
        TipComment tipComment = tipCommentRepository.findByIdAndUserId(tipCommentId, userId).orElseThrow(() -> new CustomException(TIP_COMMENT_NOT_OWNED_BY_USER));

        tipComment.changeAsDeleted();
        tipComment.getTip().minusTipCommentCount();
    }

    // ========== Private Methods ========== //

    private TipComment createTimeComment(RequestTipCommentDto requestTipCommentDto, Tip tip, User user) {
        if (isParentComment(requestTipCommentDto)) {
            return TipComment.createParentComment(requestTipCommentDto.getReply(), tip, user);
        }

        TipComment parentTipComment = findParentComment(requestTipCommentDto);
        return TipComment.createChildComment(requestTipCommentDto.getReply(), tip, user, parentTipComment);
    }

    private static boolean isParentComment(RequestTipCommentDto requestTipCommentDto) {
        return requestTipCommentDto.getParentCommentId() == null;
    }

    private TipComment findParentComment(RequestTipCommentDto requestTipCommentDto) {
        return tipCommentRepository.findById(requestTipCommentDto.getParentCommentId())
                .orElseThrow(() -> new CustomException(TIP_COMMENT_NOT_FOUND));
    }
}