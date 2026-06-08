package com.example.appcenter_project.domain.openChat.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * OpenChatInvitationService — 파생 톡방 생성 TDD Red Phase
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
 *
 * 테스트 커버 목록 (주석 처리됨):
 *
 * [Happy Path]
 * S-HP-01: 파생 톡방 생성 성공 → roomId 반환
 *
 * [Validation]
 * S-VL-01: parentRoomId 방이 DERIVED 타입 → CustomException(VALIDATION_ERROR)
 *
 * [Business Rule]
 * S-BR-01: 요청자가 parentRoomId 방 비참여자 → CustomException(OPEN_CHAT_ROOM_FORBIDDEN)
 *
 * [Error Case]
 * S-ER-01: parentRoomId 방 없음 → CustomException(OPEN_CHAT_ROOM_NOT_FOUND)
 */
@ExtendWith(MockitoExtension.class)
class OpenChatDerivedRoomServiceTest {

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
    @DisplayName("파생 톡방 생성 성공 — 정상 요청")
    void should_return_roomId_when_valid_create_derived_room_request() {
        // given
        // OpenChatRoom parentRoom = OpenChatRoom 기존 팩토리로 생성 (roomType=OPEN, id=PARENT_ROOM_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.PARENT_ROOM_ID))
        //     .willReturn(Optional.of(parentRoom));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.PARENT_ROOM_ID, OpenChatDerivedRoomFixture.HOST_USER_ID))
        //     .willReturn(true);
        // OpenChatRoom derivedRoom = 팩토리 (roomType=DERIVED, id=DERIVED_ROOM_ID)
        // given(openChatRoomRepository.save(any())).willReturn(derivedRoom);

        // when
        // RequestCreateDerivedRoomDto dto = RequestCreateDerivedRoomDto.builder()
        //     .parentRoomId(OpenChatDerivedRoomFixture.PARENT_ROOM_ID)
        //     .name(OpenChatDerivedRoomFixture.ROOM_NAME)
        //     .maxParticipants(OpenChatDerivedRoomFixture.MAX_PARTICIPANTS)
        //     .build();
        // ResponseDerivedRoomCreatedDto result =
        //     openChatInvitationService.createDerivedRoom(OpenChatDerivedRoomFixture.HOST_USER_ID, dto);

        // then
        // assertThat(result.getRoomId()).isEqualTo(OpenChatDerivedRoomFixture.DERIVED_ROOM_ID);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-VL-01 parentRoomId 방이 DERIVED 타입")
    void should_throw_CustomException_when_parentRoom_is_DERIVED_type() {
        // given
        // OpenChatRoom derivedParent = 팩토리 (roomType=DERIVED, id=PARENT_ROOM_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.PARENT_ROOM_ID))
        //     .willReturn(Optional.of(derivedParent));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.PARENT_ROOM_ID, OpenChatDerivedRoomFixture.HOST_USER_ID))
        //     .willReturn(true);

        // when
        // RequestCreateDerivedRoomDto dto = RequestCreateDerivedRoomDto.builder()
        //     .parentRoomId(OpenChatDerivedRoomFixture.PARENT_ROOM_ID)
        //     .name(OpenChatDerivedRoomFixture.ROOM_NAME)
        //     .maxParticipants(OpenChatDerivedRoomFixture.MAX_PARTICIPANTS)
        //     .build();
        // ThrowingCallable action = () ->
        //     openChatInvitationService.createDerivedRoom(OpenChatDerivedRoomFixture.HOST_USER_ID, dto);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-BR-01 요청자가 parentRoomId 방 비참여자")
    void should_throw_CustomException_when_requester_is_not_participant_of_parentRoom() {
        // given
        // OpenChatRoom parentRoom = 팩토리 (roomType=OPEN, id=PARENT_ROOM_ID)
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.PARENT_ROOM_ID))
        //     .willReturn(Optional.of(parentRoom));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(
        //         OpenChatDerivedRoomFixture.PARENT_ROOM_ID, OpenChatDerivedRoomFixture.HOST_USER_ID))
        //     .willReturn(false);

        // when
        // RequestCreateDerivedRoomDto dto = RequestCreateDerivedRoomDto.builder()
        //     .parentRoomId(OpenChatDerivedRoomFixture.PARENT_ROOM_ID)
        //     .name(OpenChatDerivedRoomFixture.ROOM_NAME)
        //     .maxParticipants(OpenChatDerivedRoomFixture.MAX_PARTICIPANTS)
        //     .build();
        // ThrowingCallable action = () ->
        //     openChatInvitationService.createDerivedRoom(OpenChatDerivedRoomFixture.HOST_USER_ID, dto);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
    }
    */

    /*
    @Test
    @DisplayName("CustomException 발생 — S-ER-01 parentRoomId 방 없음")
    void should_throw_CustomException_when_parentRoom_not_found() {
        // given
        // given(openChatRoomRepository.findById(OpenChatDerivedRoomFixture.PARENT_ROOM_ID))
        //     .willReturn(Optional.empty());

        // when
        // RequestCreateDerivedRoomDto dto = RequestCreateDerivedRoomDto.builder()
        //     .parentRoomId(OpenChatDerivedRoomFixture.PARENT_ROOM_ID)
        //     .name(OpenChatDerivedRoomFixture.ROOM_NAME)
        //     .maxParticipants(OpenChatDerivedRoomFixture.MAX_PARTICIPANTS)
        //     .build();
        // ThrowingCallable action = () ->
        //     openChatInvitationService.createDerivedRoom(OpenChatDerivedRoomFixture.HOST_USER_ID, dto);

        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
    }
    */
}
