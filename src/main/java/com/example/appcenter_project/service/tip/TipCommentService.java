package com.example.appcenter_project.service.tip;

import com.example.appcenter_project.dto.request.tip.RequestTipCommentDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipCommentDto;

import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.entity.tip.TipComment;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.repository.tip.TipCommentRepository;
import com.example.appcenter_project.repository.tip.TipRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TipCommentService {
    
    private final TipCommentRepository tipCommentRepository;
    private final UserRepository userRepository;
    private final TipRepository tipRepository;

    public ResponseTipCommentDto saveTipComment(Long userId, RequestTipCommentDto requestTipCommentDto) {
        User user = userRepository.findById(userId).orElseThrow();
        Tip tip = tipRepository.findById(requestTipCommentDto.getTipId()).orElseThrow();
        TipComment tipComment;
        
        // 부모 댓글이 없을 때
        if (requestTipCommentDto.getParentCommentId() == null) {
            tipComment = TipComment.builder()
                    .reply(requestTipCommentDto.getReply())
                    .tip(tip)
                    .user(user)
                    .build();

            // 부모 댓글이 없으므로 자신이 부모 댓글이 된다.
            tipComment.setParentTipCommentNull();
        }
        // 부모 댓글이 있을 때
        else {
            TipComment parentTipComment = tipCommentRepository.findById(requestTipCommentDto.getParentCommentId()).orElseThrow();
            tipComment = TipComment.builder()
                    .reply(requestTipCommentDto.getReply())
                    .tip(tip)
                    .user(user)
                    .parentTipComment(parentTipComment)
                    .build();
            parentTipComment.addChildTipComments(tipComment);
        }
        tipCommentRepository.save(tipComment);
        return ResponseTipCommentDto.entityToDto(tipComment, user);    
    }

    // 하나의 tip 게시판에 있는 모든 tip 댓글 조회
    public List<ResponseTipCommentDto> findTipComment(Long userId, Long tipId) {
        List<ResponseTipCommentDto> responseTipCommentDtoList = new ArrayList<>();
        List<TipComment> tipCommentList = tipCommentRepository.findByTip_IdAndParentTipCommentIsNull(tipId);
        for (TipComment tipComment : tipCommentList) {
            List<ResponseTipCommentDto> childResponseComments = new ArrayList<>();
            List<TipComment> childTipComments = tipComment.getChildTipComments();
            for (TipComment childGroupOrderComment : childTipComments) {
                ResponseTipCommentDto build = ResponseTipCommentDto.builder()
                        .tipCommentId(childGroupOrderComment.getId())
                        .userId(userId)
                        .reply(childGroupOrderComment.getReply())
                        .build();

                childResponseComments.add(build);
            }
            ResponseTipCommentDto responseTipCommentDto = ResponseTipCommentDto.builder()
                    .tipCommentId(tipComment.getId())
                    .userId(userId)
                    .reply(tipComment.getReply())
                    .childTipCommentList(childResponseComments)
                    .build();
            responseTipCommentDtoList.add(responseTipCommentDto);

        }
        return responseTipCommentDtoList;
    }
}
