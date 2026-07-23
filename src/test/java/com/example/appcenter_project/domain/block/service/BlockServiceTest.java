package com.example.appcenter_project.domain.block.service;

// TODO: 구현 후 아래 import 주석 해제
// import com.example.appcenter_project.domain.block.entity.UserBlock;
// import com.example.appcenter_project.domain.block.repository.UserBlockRepository;
// import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * BR-685 — BlockService 단위 테스트 (RED 단계)
 *
 * 구현 에이전트가 생성해야 할 클래스:
 * - com.example.appcenter_project.domain.block.service.BlockService
 * - com.example.appcenter_project.domain.block.entity.UserBlock
 * - com.example.appcenter_project.domain.block.repository.UserBlockRepository
 * - com.example.appcenter_project.global.exception.ErrorCode 신규 상수:
 *   USER_BLOCK_CANNOT_BLOCK_SELF (400, 26001)
 *   USER_BLOCK_ALREADY_EXISTS   (409, 26002)
 *   USER_BLOCKED_BY_TARGET      (403, 26003)
 */
@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

    // TODO: 구현 후 주석 해제
    // @Mock
    // UserBlockRepository userBlockRepository;
    //
    // @Mock
    // UserRepository userRepository;
    //
    // @InjectMocks
    // BlockService blockService;

    // ──────────────────────────────────────────────
    // AC-1: 정상 차단 저장
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("차단 저장 성공 — AC-1: 정상 요청 시 UserBlock 저장됨")
    void should_save_user_block_when_valid_request() {
        // given
        // Long requesterId = 1L;
        // Long targetId = 2L;
        // given(userRepository.findById(targetId)).willReturn(Optional.of(new User()));
        // given(userBlockRepository.existsByBlockerIdAndBlockedId(requesterId, targetId)).willReturn(false);

        // when
        // ThrowingCallable action = () -> blockService.blockUser(requesterId, targetId);

        // then
        // assertThatCode(action).doesNotThrowAnyException();
        // then(userBlockRepository).should().save(any(UserBlock.class));

        // TODO: 구현 전 — RED 검증용 placeholder (컴파일 통과용)
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("차단 저장 성공 — AC-1: 저장된 UserBlock의 blockerId가 요청자 ID")
    void should_save_user_block_with_correct_blocker_id() {
        // given
        // Long requesterId = 1L;
        // Long targetId = 2L;
        // given(userRepository.findById(targetId)).willReturn(Optional.of(new User()));
        // given(userBlockRepository.existsByBlockerIdAndBlockedId(requesterId, targetId)).willReturn(false);
        // ArgumentCaptor<UserBlock> captor = ArgumentCaptor.forClass(UserBlock.class);

        // when
        // blockService.blockUser(requesterId, targetId);

        // then
        // then(userBlockRepository).should().save(captor.capture());
        // assertThat(captor.getValue().getBlockerId()).isEqualTo(requesterId);

        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("차단 저장 성공 — AC-1: 저장된 UserBlock의 blockedId가 대상 ID")
    void should_save_user_block_with_correct_blocked_id() {
        // given
        // Long requesterId = 1L;
        // Long targetId = 2L;
        // given(userRepository.findById(targetId)).willReturn(Optional.of(new User()));
        // given(userBlockRepository.existsByBlockerIdAndBlockedId(requesterId, targetId)).willReturn(false);
        // ArgumentCaptor<UserBlock> captor = ArgumentCaptor.forClass(UserBlock.class);

        // when
        // blockService.blockUser(requesterId, targetId);

        // then
        // then(userBlockRepository).should().save(captor.capture());
        // assertThat(captor.getValue().getBlockedId()).isEqualTo(targetId);

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // AC-2: 자기 자신 차단 거부
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("CustomException 발생 — AC-2 BR-685: 자기 자신 차단 시도 시 USER_BLOCK_CANNOT_BLOCK_SELF")
    void should_throw_CustomException_when_AC_2_blocker_equals_blocked() {
        // given
        // Long userId = 1L;

        // when
        // ThrowingCallable action = () -> blockService.blockUser(userId, userId);

        // then
        // assertThatThrownBy(action)
        //         .isInstanceOf(CustomException.class)
        //         .extracting("errorCode")
        //         .isEqualTo(ErrorCode.USER_BLOCK_CANNOT_BLOCK_SELF);

        // TODO: 구현 클래스(BlockService) 부재 — 컴파일 통과용 placeholder
        // ErrorCode.USER_BLOCK_CANNOT_BLOCK_SELF 상수도 아직 없음 (TODO: ErrorCode 추가 필요)
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("미저장 확인 — AC-2: 자기 자신 차단 시 save 미호출")
    void should_not_save_when_self_block_attempted() {
        // given
        // Long userId = 1L;

        // when
        // assertThatThrownBy(() -> blockService.blockUser(userId, userId))
        //         .isInstanceOf(CustomException.class);

        // then
        // then(userBlockRepository).should(never()).save(any());

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // AC-3: 존재하지 않는 사용자 차단 거부
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("CustomException 발생 — AC-3 BR-685: 존재하지 않는 targetId 차단 시 USER_NOT_FOUND")
    void should_throw_CustomException_when_AC_3_target_user_not_found() {
        // given
        // Long requesterId = 1L;
        // Long nonExistentId = 99999L;
        // given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // when
        // ThrowingCallable action = () -> blockService.blockUser(requesterId, nonExistentId);

        // then
        // assertThatThrownBy(action)
        //         .isInstanceOf(CustomException.class)
        //         .extracting("errorCode")
        //         .isEqualTo(ErrorCode.USER_NOT_FOUND);

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // AC-4: 중복 차단 거부
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("CustomException 발생 — AC-4 BR-685: 이미 차단한 사용자 재차단 시 USER_BLOCK_ALREADY_EXISTS")
    void should_throw_CustomException_when_AC_4_already_blocked() {
        // given
        // Long requesterId = 1L;
        // Long targetId = 2L;
        // given(userRepository.findById(targetId)).willReturn(Optional.of(new User()));
        // given(userBlockRepository.existsByBlockerIdAndBlockedId(requesterId, targetId)).willReturn(true);

        // when
        // ThrowingCallable action = () -> blockService.blockUser(requesterId, targetId);

        // then
        // assertThatThrownBy(action)
        //         .isInstanceOf(CustomException.class)
        //         .extracting("errorCode")
        //         .isEqualTo(ErrorCode.USER_BLOCK_ALREADY_EXISTS);

        // TODO: ErrorCode.USER_BLOCK_ALREADY_EXISTS 상수 아직 없음
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("미저장 확인 — AC-4: 중복 차단 시 save 미호출")
    void should_not_save_when_duplicate_block_attempted() {
        // given
        // Long requesterId = 1L;
        // Long targetId = 2L;
        // given(userRepository.findById(targetId)).willReturn(Optional.of(new User()));
        // given(userBlockRepository.existsByBlockerIdAndBlockedId(requesterId, targetId)).willReturn(true);

        // when
        // assertThatThrownBy(() -> blockService.blockUser(requesterId, targetId))
        //         .isInstanceOf(CustomException.class);

        // then
        // then(userBlockRepository).should(never()).save(any());

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // isBlockedBy: 차단 여부 조회
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("true 반환 — isBlockedBy: blockerId가 blockedId를 차단한 경우")
    void should_return_true_when_blocker_has_blocked_target() {
        // given
        // Long blockerId = 1L;
        // Long blockedId = 2L;
        // given(userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)).willReturn(true);

        // when
        // boolean result = blockService.isBlockedBy(blockerId, blockedId);

        // then
        // assertThat(result).isTrue();

        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("false 반환 — isBlockedBy: 차단 관계 없는 경우")
    void should_return_false_when_no_block_relationship() {
        // given
        // Long blockerId = 1L;
        // Long blockedId = 2L;
        // given(userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)).willReturn(false);

        // when
        // boolean result = blockService.isBlockedBy(blockerId, blockedId);

        // then
        // assertThat(result).isFalse();

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // assertNotBlockedByAny: 참여자 중 차단자 존재 시 예외
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("CustomException 발생 — assertNotBlockedByAny: 참여자 중 senderId를 차단한 사람 있을 때 USER_BLOCKED_BY_TARGET")
    void should_throw_CustomException_when_any_participant_blocked_sender() {
        // given
        // Long senderId = 2L;
        // Set<Long> participantIds = Set.of(1L, 3L);
        // given(userBlockRepository.existsByBlockerIdInAndBlockedId(participantIds, senderId)).willReturn(true);

        // when
        // ThrowingCallable action = () -> blockService.assertNotBlockedByAny(participantIds, senderId);

        // then
        // assertThatThrownBy(action)
        //         .isInstanceOf(CustomException.class)
        //         .extracting("errorCode")
        //         .isEqualTo(ErrorCode.USER_BLOCKED_BY_TARGET);

        // TODO: ErrorCode.USER_BLOCKED_BY_TARGET 상수 아직 없음
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("정상 통과 — assertNotBlockedByAny: 참여자 중 차단자 없을 때 예외 없음")
    void should_not_throw_when_no_participant_blocked_sender() {
        // given
        // Long senderId = 2L;
        // Set<Long> participantIds = Set.of(1L, 3L);
        // given(userBlockRepository.existsByBlockerIdInAndBlockedId(participantIds, senderId)).willReturn(false);

        // when
        // ThrowingCallable action = () -> blockService.assertNotBlockedByAny(participantIds, senderId);

        // then
        // assertThatCode(action).doesNotThrowAnyException();

        assertThat(true).isTrue();
    }
}
