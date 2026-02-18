package com.example.appcenter_project.domain.tip.service;

import com.example.appcenter_project.domain.tip.dto.request.RequestTipCommentDto;
import com.example.appcenter_project.domain.tip.dto.response.ResponseTipCommentDto;
import com.example.appcenter_project.domain.tip.entity.Tip;
import com.example.appcenter_project.domain.tip.entity.TipComment;
import com.example.appcenter_project.domain.tip.repository.TipCommentRepository;
import com.example.appcenter_project.domain.tip.repository.TipRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TipCommentServiceTest {

    @Mock private TipCommentRepository tipCommentRepository;
    @Mock private UserRepository userRepository;
    @Mock private TipRepository tipRepository;

    @InjectMocks
    private TipCommentService tipCommentService;

    // ===== saveTipComment - 부모 댓글 =====

    @Test
    @DisplayName("부모 댓글 저장 - 정상 저장 및 팁 댓글 수 증가")
    void saveTipComment_부모댓글_저장_성공() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getName()).thenReturn("테스트유저");

        Tip mockTip = mock(Tip.class);

        RequestTipCommentDto dto = mock(RequestTipCommentDto.class);
        when(dto.getTipId()).thenReturn(1L);
        when(dto.getParentCommentId()).thenReturn(null);
        when(dto.getReply()).thenReturn("부모 댓글");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tipRepository.findById(1L)).thenReturn(Optional.of(mockTip));

        TipComment savedComment = TipComment.createParentComment("부모 댓글", mock(Tip.class), mockUser);
        when(tipCommentRepository.save(any(TipComment.class))).thenReturn(savedComment);

        ResponseTipCommentDto result = tipCommentService.saveTipComment(1L, dto);

        assertThat(result).isNotNull();
        verify(tipCommentRepository).save(any(TipComment.class));
        verify(mockTip).plusTipCommentCount();
    }

    @Test
    @DisplayName("부모 댓글 저장 - 유저 없으면 예외")
    void saveTipComment_유저_없으면_예외() {
        RequestTipCommentDto dto = mock(RequestTipCommentDto.class);
        when(dto.getTipId()).thenReturn(1L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipCommentService.saveTipComment(99L, dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
    }

    @Test
    @DisplayName("부모 댓글 저장 - 팁 없으면 예외")
    void saveTipComment_팁_없으면_예외() {
        User mockUser = mock(User.class);
        RequestTipCommentDto dto = mock(RequestTipCommentDto.class);
        when(dto.getTipId()).thenReturn(99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipCommentService.saveTipComment(1L, dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TIP_NOT_FOUND);
    }

    // ===== saveTipComment - 자식 댓글 =====

    @Test
    @DisplayName("자식 댓글 저장 - 부모 댓글과 연결하여 정상 저장")
    void saveTipComment_자식댓글_저장_성공() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getName()).thenReturn("테스트유저");

        Tip mockTip = mock(Tip.class);
        TipComment parentComment = mock(TipComment.class);

        RequestTipCommentDto childDto = mock(RequestTipCommentDto.class);
        when(childDto.getTipId()).thenReturn(1L);
        when(childDto.getParentCommentId()).thenReturn(10L);
        when(childDto.getReply()).thenReturn("자식 댓글");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tipRepository.findById(1L)).thenReturn(Optional.of(mockTip));
        when(tipCommentRepository.findById(10L)).thenReturn(Optional.of(parentComment));

        TipComment savedChild = TipComment.createChildComment("자식 댓글", mock(Tip.class), mockUser, parentComment);
        when(tipCommentRepository.save(any(TipComment.class))).thenReturn(savedChild);

        ResponseTipCommentDto result = tipCommentService.saveTipComment(1L, childDto);

        assertThat(result).isNotNull();
        verify(tipCommentRepository).save(any(TipComment.class));
        verify(mockTip).plusTipCommentCount();
    }

    @Test
    @DisplayName("자식 댓글 저장 - 부모 댓글 없으면 예외")
    void saveTipComment_부모댓글_없으면_예외() {
        User mockUser = mock(User.class);
        Tip mockTip = mock(Tip.class);

        RequestTipCommentDto childDto = mock(RequestTipCommentDto.class);
        when(childDto.getTipId()).thenReturn(1L);
        when(childDto.getParentCommentId()).thenReturn(99L);
        when(childDto.getReply()).thenReturn("자식 댓글");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tipRepository.findById(1L)).thenReturn(Optional.of(mockTip));
        when(tipCommentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipCommentService.saveTipComment(1L, childDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TIP_COMMENT_NOT_FOUND);
    }

    // ===== deleteTipComment =====

    @Test
    @DisplayName("댓글 삭제 - 소프트 삭제 및 팁 댓글 수 감소")
    void deleteTipComment_소프트_삭제() {
        TipComment mockComment = mock(TipComment.class);
        Tip commentTip = mock(Tip.class);
        when(mockComment.getTip()).thenReturn(commentTip);
        when(tipCommentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(mockComment));

        tipCommentService.deleteTipComment(1L, 1L);

        verify(mockComment).changeAsDeleted();
        verify(commentTip).minusTipCommentCount();
    }

    @Test
    @DisplayName("댓글 삭제 - 본인 댓글 아니면 예외")
    void deleteTipComment_본인_댓글_아니면_예외() {
        when(tipCommentRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipCommentService.deleteTipComment(2L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TIP_COMMENT_NOT_OWNED_BY_USER);
    }
}
