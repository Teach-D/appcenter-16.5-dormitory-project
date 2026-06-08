package com.example.appcenter_project.domain.openChat.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * OpenChatInvitationService — 초대 발송·수락·거절·참여자 목록 TDD Red Phase
 *
 * 구현 에이전트: 아래 주석 처리된 테스트를 활성화하려면 다음 클래스가 필요합니다.
 *
 * @InjectMocks 대상:
 * - OpenChatInvitationService openChatInvitationService
 *
 * @Mock 대상:
 * - OpenChatRoomRepository openChatRoomRepository
 * - OpenChatParticipantRepository openChatParticipantRepository
 * - OpenChatInvitationRepository openChatInvitationRepository
 * - UserRepository userRepository  (참여자 목록 닉네임 조회용)
 *
 * 테스트 커버 목록 (주석 처리됨):
 *
 * [Happy Path]
 * S-HP-02: 초대 발송 성공 → invitationId 반환
 * S-HP-03: 초대 수락 성공 → ResponseInvitationAcceptDto 반환 + OpenChatParticipant 저장 확인
 * S-HP-04: 초대 거절 성공 → REJECTED 전이 + void
 * S-HP-05: 참여자 목록 조회 → joinedAt 오름차순, isHost 정확히 세팅
 *
 * [Business Rule]
 * S-BR-02: inviter가 파생 톡방 비참여자 → OPEN_CHAT_ROOM_FORBIDDEN
 * S-BR-03: invitee가 부모 방 비참여자 → OPEN_CHAT_INVITATION_INVALID_TARGET
 * S-BR-04: PENDING 중복 초대 → OPEN_CHAT_INVITATION_ALREADY_EXISTS
 * S-BR-05: invitee가 이미 파생 톡방 참여자 (초대 발송 시) → OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS
 * S-BR-06: 거절 후 재초대 허용 (REJECTED 존재해도 신규 PENDING 생성 가능)
 * S-BR-07: 초대 수락 시 본인 아님 → OPEN_CHAT_ROOM_FORBIDDEN
 * S-BR-08: 초대 거절 시 본인 아님 → OPEN_CHAT_ROOM_FORBIDDEN
 * S-BR-09: 참여자 목록 조회 시 비참여자 → OPEN_CHAT_ROOM_FORBIDDEN
 * S-BR-10: 수락 시 정원 초과 → OPEN_CHAT_ROOM_FULL
 *
 * [Edge Case]
 * S-EC-01: 이미 ACCEPTED 상태 초대 수락 재시도 → VALIDATION_ERROR
 * S-EC-02: 이미 REJECTED 상태 초대 수락 시도 → VALIDATION_ERROR
 * S-EC-03: 이미 ACCEPTED 상태 초대 거절 시도 → VALIDATION_ERROR
 * S-EC-04: 이미 REJECTED 상태 초대 거절 재시도 → VALIDATION_ERROR
 * S-EC-05: 수락 시 이미 참여자 → OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS
 * S-EC-06: 참여자 1명(호스트만) 목록 조회 → isHost=true 정상 반환
 *
 * [Error Case]
 * S-ER-02: 초대 발송 시 roomId 방 없음 → OPEN_CHAT_ROOM_NOT_FOUND
 * S-ER-03: 초대 수락 시 invitationId 없음 → OPEN_CHAT_INVITATION_NOT_FOUND
 * S-ER-04: 초대 거절 시 invitationId 없음 → OPEN_CHAT_INVITATION_NOT_FOUND
 * S-ER-05: 참여자 목록 조회 시 roomId 없음 → OPEN_CHAT_ROOM_NOT_FOUND
 */
@ExtendWith(MockitoExtension.class)
class OpenChatInvitationServiceTest {

    /*
     * 구현 클래스가 존재하지 않아 @InjectMocks 적용 불가.
     * 구현 에이전트가 아래 클래스들을 생성한 후 이 파일을 수정하십시오:
     *
     * @Mock
     * private OpenChatRoomRepository openChatRoomRepository;
     *
     * @Mock
     * private OpenChatParticipantRepository openChatParticipantRepository;
     *
     * @Mock
     * private OpenChatInvitationRepository openChatInvitationRepository;
     *
     * @Mock
     * private UserRepository userRepository;
     *
     * @InjectMocks
     * private OpenChatInvitationService openChatInvitationService;
     */

    @Test
    @DisplayName("placeholder — 구현 후 아래 주석을 해제하십시오")
    void placeholder() {
        assertThat(true).isTrue();
    }

    /*
    @Test
    @DisplayName("초대 발송 성공 — 정상 요청")
    void should_return_invitationId_when_valid_send_invitation() {
        // given
        // OpenChatRoom derivedRoom = 팩토리 (roomType=DERIVED, id=DERIVED_ROOM_ID, parentRoomId=PARENT_ROOM_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(derivedRoom));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITER_USER_ID))
        //     .willReturn(true);
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.PARENT_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(true);
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(false);
        // given(openChatInvitationRepository.existsByRoomIdAndInviteeUserIdAndStatus(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //         OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //         OpenChatInvitationStatus.PENDING))
        //     .willReturn(false);
        // OpenChatInvitation savedInvitation = 팩토리 (id=INVITATION_ID)
        // given(openChatInvitationRepository.save(any())).willReturn(savedInvitation);

        // when
        // RequestSendInvitationDto dto = RequestSendInvitationDto.builder()
        //     .inviteeUserId(OpenChatDerivedRoomFixture.INVITEE_USER_ID)
        //     .build();
        // ResponseInvitationCreatedDto result = openChatInvitationService.sendInvitation(
        //     OpenChatDerivedRoomFixture.INVITER_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     dto);

        // then
        // assertThat(result.getInvitationId()).isEqualTo(OpenChatDerivedRoomFixture.INVITATION_ID);
    }
    */

    /*
    @Test
    @DisplayName("초대 수락 성공 — Participant 저장 및 방 정보 반환")
    void should_save_participant_and_return_room_detail_when_accept_invitation() {
        // given
        // OpenChatInvitation invitation = 팩토리 (id=INVITATION_ID, status=PENDING,
        //     inviteeUserId=INVITEE_USER_ID, roomId=DERIVED_ROOM_ID)
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.of(invitation));
        // OpenChatRoom derivedRoom = 팩토리 (id=DERIVED_ROOM_ID, maxParticipants=5)
        // given(openChatRoomRepository.findByIdWithLock(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(derivedRoom));
        // given(openChatParticipantRepository.countByRoomId(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(1L);
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(false);

        // when
        // ResponseInvitationAcceptDto result = openChatInvitationService.acceptInvitation(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThat(result.getRoomId()).isEqualTo(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID);
        // then(openChatParticipantRepository).should(times(1)).save(any(OpenChatParticipant.class));
    }
    */

    /*
    @Test
    @DisplayName("초대 거절 성공 — REJECTED 상태 전이")
    void should_change_status_to_REJECTED_when_reject_invitation() {
        // given
        // OpenChatInvitation invitation = 팩토리 (id=INVITATION_ID, status=PENDING,
        //     inviteeUserId=INVITEE_USER_ID, roomId=DERIVED_ROOM_ID)
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.of(invitation));

        // when
        // ThrowingCallable action = () -> openChatInvitationService.rejectInvitation(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatCode(action).doesNotThrowAnyException();
        // assertThat(invitation.getStatus()).isEqualTo(OpenChatInvitationStatus.REJECTED);
    }
    */

    /*
    @Test
    @DisplayName("참여자 목록 조회 성공 — isHost 필드 정확히 세팅")
    void should_return_participant_list_with_isHost_correctly_set() {
        // given
        // OpenChatRoom room = 팩토리 (id=DERIVED_ROOM_ID, hostUserId=HOST_USER_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(room));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.HOST_USER_ID))
        //     .willReturn(true);
        // List<OpenChatParticipant> participants = List.of(
        //     팩토리(userId=HOST_USER_ID, joinedAt=now-1hour),
        //     팩토리(userId=INVITEE_USER_ID, joinedAt=now)
        // );
        // given(openChatParticipantRepository.findByRoomId(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(participants);

        // when
        // ResponseOpenChatParticipantListDto result = openChatInvitationService.getParticipants(
        //     OpenChatDerivedRoomFixture.HOST_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID);

        // then
        // assertThat(result.getParticipants()).hasSize(2);
        // assertThat(result.getParticipants().get(0).getIsHost()).isTrue();
        // assertThat(result.getParticipants().get(1).getIsHost()).isFalse();
        // assertThat(result.getTotalCount()).isEqualTo(2);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-BR-02 inviter가 파생 톡방 비참여자")
    void should_throw_CustomException_when_inviter_is_not_participant_of_derived_room() {
        // given
        // OpenChatRoom derivedRoom = 팩토리 (roomType=DERIVED, id=DERIVED_ROOM_ID, parentRoomId=PARENT_ROOM_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(derivedRoom));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITER_USER_ID))
        //     .willReturn(false);

        // when
        // RequestSendInvitationDto dto = RequestSendInvitationDto.builder()
        //     .inviteeUserId(OpenChatDerivedRoomFixture.INVITEE_USER_ID)
        //     .build();
        // ThrowingCallable action = () -> openChatInvitationService.sendInvitation(
        //     OpenChatDerivedRoomFixture.INVITER_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     dto);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-BR-03 invitee가 부모 방 비참여자")
    void should_throw_CustomException_when_invitee_is_not_participant_of_parent_room() {
        // given
        // OpenChatRoom derivedRoom = 팩토리 (roomType=DERIVED, id=DERIVED_ROOM_ID, parentRoomId=PARENT_ROOM_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(derivedRoom));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITER_USER_ID))
        //     .willReturn(true);
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.PARENT_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(false);

        // when
        // RequestSendInvitationDto dto = RequestSendInvitationDto.builder()
        //     .inviteeUserId(OpenChatDerivedRoomFixture.INVITEE_USER_ID)
        //     .build();
        // ThrowingCallable action = () -> openChatInvitationService.sendInvitation(
        //     OpenChatDerivedRoomFixture.INVITER_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     dto);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_INVITATION_INVALID_TARGET);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-BR-04 PENDING 중복 초대")
    void should_throw_CustomException_when_pending_invitation_already_exists() {
        // given
        // OpenChatRoom derivedRoom = 팩토리 (roomType=DERIVED, id=DERIVED_ROOM_ID, parentRoomId=PARENT_ROOM_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(derivedRoom));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITER_USER_ID))
        //     .willReturn(true);
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.PARENT_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(true);
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(false);
        // given(openChatInvitationRepository.existsByRoomIdAndInviteeUserIdAndStatus(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //         OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //         OpenChatInvitationStatus.PENDING))
        //     .willReturn(true);

        // when
        // RequestSendInvitationDto dto = RequestSendInvitationDto.builder()
        //     .inviteeUserId(OpenChatDerivedRoomFixture.INVITEE_USER_ID)
        //     .build();
        // ThrowingCallable action = () -> openChatInvitationService.sendInvitation(
        //     OpenChatDerivedRoomFixture.INVITER_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     dto);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_INVITATION_ALREADY_EXISTS);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-BR-05 invitee가 이미 파생 톡방 참여자 (초대 발송 시)")
    void should_throw_CustomException_when_invitee_is_already_participant_of_derived_room() {
        // given
        // OpenChatRoom derivedRoom = 팩토리 (roomType=DERIVED, id=DERIVED_ROOM_ID, parentRoomId=PARENT_ROOM_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(derivedRoom));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITER_USER_ID))
        //     .willReturn(true);
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.PARENT_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(true);
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(true);

        // when
        // RequestSendInvitationDto dto = RequestSendInvitationDto.builder()
        //     .inviteeUserId(OpenChatDerivedRoomFixture.INVITEE_USER_ID)
        //     .build();
        // ThrowingCallable action = () -> openChatInvitationService.sendInvitation(
        //     OpenChatDerivedRoomFixture.INVITER_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     dto);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS);
    }
    */

    /*
    @Test
    @DisplayName("재초대 허용 — S-BR-06 REJECTED 초대 존재 시 신규 PENDING 생성 가능")
    void should_allow_new_invitation_when_previous_invitation_was_rejected() {
        // given
        // OpenChatRoom derivedRoom = 팩토리 (roomType=DERIVED, id=DERIVED_ROOM_ID, parentRoomId=PARENT_ROOM_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(derivedRoom));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITER_USER_ID))
        //     .willReturn(true);
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.PARENT_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(true);
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(false);
        // REJECTED 존재하지만 PENDING은 없음
        // given(openChatInvitationRepository.existsByRoomIdAndInviteeUserIdAndStatus(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //         OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //         OpenChatInvitationStatus.PENDING))
        //     .willReturn(false);
        // OpenChatInvitation newInvitation = 팩토리 (id=INVITATION_ID, status=PENDING)
        // given(openChatInvitationRepository.save(any())).willReturn(newInvitation);

        // when
        // RequestSendInvitationDto dto = RequestSendInvitationDto.builder()
        //     .inviteeUserId(OpenChatDerivedRoomFixture.INVITEE_USER_ID)
        //     .build();
        // ThrowingCallable action = () -> openChatInvitationService.sendInvitation(
        //     OpenChatDerivedRoomFixture.INVITER_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     dto);

        // then
        // assertThatCode(action).doesNotThrowAnyException();
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-BR-07 초대 수락 시 본인 아님")
    void should_throw_CustomException_when_acceptor_is_not_invitee() {
        // given
        // OpenChatInvitation invitation = 팩토리 (id=INVITATION_ID, status=PENDING,
        //     inviteeUserId=INVITEE_USER_ID, roomId=DERIVED_ROOM_ID)
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.of(invitation));

        // when
        // Long differentUserId = 999L;
        // ThrowingCallable action = () -> openChatInvitationService.acceptInvitation(
        //     differentUserId,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-BR-08 초대 거절 시 본인 아님")
    void should_throw_CustomException_when_rejector_is_not_invitee() {
        // given
        // OpenChatInvitation invitation = 팩토리 (id=INVITATION_ID, status=PENDING,
        //     inviteeUserId=INVITEE_USER_ID, roomId=DERIVED_ROOM_ID)
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.of(invitation));

        // when
        // Long differentUserId = 999L;
        // ThrowingCallable action = () -> openChatInvitationService.rejectInvitation(
        //     differentUserId,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-BR-09 참여자 목록 조회 시 비참여자")
    void should_throw_CustomException_when_requester_is_not_participant_for_list() {
        // given
        // OpenChatRoom room = 팩토리 (id=DERIVED_ROOM_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(room));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(false);

        // when
        // ThrowingCallable action = () -> openChatInvitationService.getParticipants(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-BR-10 수락 시 정원 초과")
    void should_throw_CustomException_when_room_is_full_on_accept() {
        // given
        // OpenChatInvitation invitation = 팩토리 (id=INVITATION_ID, status=PENDING,
        //     inviteeUserId=INVITEE_USER_ID, roomId=DERIVED_ROOM_ID)
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.of(invitation));
        // OpenChatRoom derivedRoom = 팩토리 (id=DERIVED_ROOM_ID, maxParticipants=2)
        // given(openChatRoomRepository.findByIdWithLock(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(derivedRoom));
        // 현재 참여자 수 = maxParticipants (정원 꽉 참)
        // given(openChatParticipantRepository.countByRoomId(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(2L);

        // when
        // ThrowingCallable action = () -> openChatInvitationService.acceptInvitation(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FULL);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-EC-01 이미 ACCEPTED 상태 초대 수락 재시도")
    void should_throw_CustomException_when_invitation_already_accepted_on_accept() {
        // given
        // OpenChatInvitation invitation = 팩토리 (id=INVITATION_ID, status=ACCEPTED,
        //     inviteeUserId=INVITEE_USER_ID)
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.of(invitation));

        // when
        // ThrowingCallable action = () -> openChatInvitationService.acceptInvitation(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-EC-02 REJECTED 상태 초대 수락 시도")
    void should_throw_CustomException_when_invitation_already_rejected_on_accept() {
        // given
        // OpenChatInvitation invitation = 팩토리 (id=INVITATION_ID, status=REJECTED,
        //     inviteeUserId=INVITEE_USER_ID)
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.of(invitation));

        // when
        // ThrowingCallable action = () -> openChatInvitationService.acceptInvitation(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-EC-03 이미 ACCEPTED 상태 초대 거절 시도")
    void should_throw_CustomException_when_invitation_already_accepted_on_reject() {
        // given
        // OpenChatInvitation invitation = 팩토리 (id=INVITATION_ID, status=ACCEPTED,
        //     inviteeUserId=INVITEE_USER_ID)
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.of(invitation));

        // when
        // ThrowingCallable action = () -> openChatInvitationService.rejectInvitation(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-EC-04 이미 REJECTED 상태 초대 거절 재시도")
    void should_throw_CustomException_when_invitation_already_rejected_on_reject() {
        // given
        // OpenChatInvitation invitation = 팩토리 (id=INVITATION_ID, status=REJECTED,
        //     inviteeUserId=INVITEE_USER_ID)
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.of(invitation));

        // when
        // ThrowingCallable action = () -> openChatInvitationService.rejectInvitation(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-EC-05 수락 시 이미 참여자")
    void should_throw_CustomException_when_invitee_is_already_participant_on_accept() {
        // given
        // OpenChatInvitation invitation = 팩토리 (id=INVITATION_ID, status=PENDING,
        //     inviteeUserId=INVITEE_USER_ID, roomId=DERIVED_ROOM_ID)
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.of(invitation));
        // OpenChatRoom derivedRoom = 팩토리 (id=DERIVED_ROOM_ID, maxParticipants=5)
        // given(openChatRoomRepository.findByIdWithLock(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(derivedRoom));
        // given(openChatParticipantRepository.countByRoomId(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(1L);
        // invitee가 이미 참여자
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.INVITEE_USER_ID))
        //     .willReturn(true);

        // when
        // ThrowingCallable action = () -> openChatInvitationService.acceptInvitation(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS);
    }
    */

    /*
    @Test
    @DisplayName("참여자 1명(호스트만) 목록 조회 성공 — S-EC-06 isHost=true 정상 반환")
    void should_return_host_only_participant_list_when_one_participant() {
        // given
        // OpenChatRoom room = 팩토리 (id=DERIVED_ROOM_ID, hostUserId=HOST_USER_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.of(room));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.DERIVED_ROOM_ID, OpenChatDerivedRoomFixture.HOST_USER_ID))
        //     .willReturn(true);
        // List<OpenChatParticipant> participants = List.of(
        //     팩토리(userId=HOST_USER_ID, joinedAt=now)
        // );
        // given(openChatParticipantRepository.findByRoomId(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(participants);

        // when
        // ResponseOpenChatParticipantListDto result = openChatInvitationService.getParticipants(
        //     OpenChatDerivedRoomFixture.HOST_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID);

        // then
        // assertThat(result.getParticipants()).hasSize(1);
        // assertThat(result.getParticipants().get(0).getIsHost()).isTrue();
        // assertThat(result.getTotalCount()).isEqualTo(1);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-ER-02 초대 발송 시 roomId 방 없음")
    void should_throw_CustomException_when_room_not_found_on_send_invitation() {
        // given
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.empty());

        // when
        // RequestSendInvitationDto dto = RequestSendInvitationDto.builder()
        //     .inviteeUserId(OpenChatDerivedRoomFixture.INVITEE_USER_ID)
        //     .build();
        // ThrowingCallable action = () -> openChatInvitationService.sendInvitation(
        //     OpenChatDerivedRoomFixture.INVITER_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     dto);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-ER-03 초대 수락 시 invitationId 없음")
    void should_throw_CustomException_when_invitation_not_found_on_accept() {
        // given
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.empty());

        // when
        // ThrowingCallable action = () -> openChatInvitationService.acceptInvitation(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_INVITATION_NOT_FOUND);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-ER-04 초대 거절 시 invitationId 없음")
    void should_throw_CustomException_when_invitation_not_found_on_reject() {
        // given
        // given(openChatInvitationRepository.findById(OpenChatDerivedRoomFixture.INVITATION_ID))
        //     .willReturn(Optional.empty());

        // when
        // ThrowingCallable action = () -> openChatInvitationService.rejectInvitation(
        //     OpenChatDerivedRoomFixture.INVITEE_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID,
        //     OpenChatDerivedRoomFixture.INVITATION_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_INVITATION_NOT_FOUND);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-ER-05 참여자 목록 조회 시 roomId 없음")
    void should_throw_CustomException_when_room_not_found_on_get_participants() {
        // given
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID))
        //     .willReturn(Optional.empty());

        // when
        // ThrowingCallable action = () -> openChatInvitationService.getParticipants(
        //     OpenChatDerivedRoomFixture.HOST_USER_ID,
        //     OpenChatDerivedRoomFixture.DERIVED_ROOM_ID);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
    }
    */
}
