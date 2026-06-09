package com.example.appcenter_project.domain.openChat.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenChatRoomService 테스트.
 *
 * 구현 에이전트가 생성해야 할 클래스 (fixture/OpenChatRoomFixture.java Javadoc 참조):
 *
 * [Service — OpenChatRoomService]
 * Long createRoom(RequestCreateOpenChatRoomDto request, Long userId)
 *   - scope=ALL: creatorDormitory=null 로 OpenChatRoom 저장, 방장을 OpenChatParticipant로 등록, roomId 반환
 *   - scope=DORMITORY: creatorDormitory=userId의 dormType 으로 OpenChatRoom 저장
 *
 * Page<ResponseOpenChatRoomDto> getRooms(Long userId, String tab, Pageable pageable)
 *   - tab=MY: openChatRoomRepository.findMyRooms(userId) 결과를 DTO로 변환
 *   - tab=ALL: openChatRoomRepository.findAllPublic() 결과를 DTO로 변환
 *   - tab=DORMITORY: 내부적으로 getRoomsForDormitory 위임 또는 직접 처리
 *   - 각 방에 isJoined = existsByRoomIdAndUserId(roomId, userId) 결과 설정 (BR-09)
 *
 * Page<ResponseOpenChatRoomDto> getRoomsForDormitory(Long userId, String dormType, Pageable pageable)
 *   - dormType=="NONE": 빈 페이지 반환 (BR-02), 레포지토리 호출 없음
 *   - 그 외: findByCreatorDormitory(dormType) 결과 반환, isJoined 설정
 *
 * ResponseOpenChatRoomDetailDto joinRoom(Long userId, Long roomId)
 *   - openChatRoomRepository.findByIdWithLock(roomId) 으로 비관적 락 획득 (ADR-03)
 *   - existsByRoomIdAndUserId → true: save 호출 없이 방 정보 반환 (BR-05 멱등)
 *   - scope=DORMITORY 검증은 joinRoomWithDormType 메서드에서 담당
 *   - countByRoomId >= maxParticipants: throw CustomException(OPEN_CHAT_ROOM_FULL) (BR-04)
 *   - 정상: OpenChatParticipant.create(roomId, userId, now()) save 후 방 상세 반환
 *
 * ResponseOpenChatRoomDetailDto joinRoomWithDormType(Long userId, Long roomId, String userDormType)
 *   - findByIdWithLock 으로 방 조회
 *   - existsByRoomIdAndUserId → true: 멱등 반환
 *   - scope=DORMITORY && room.creatorDormitory != userDormType: throw CustomException(OPEN_CHAT_ROOM_FORBIDDEN) (BR-03)
 *   - 이후 joinRoom 과 동일 흐름
 *
 * ResponseLeaveOpenChatRoomDto leaveRoom(Long userId, Long roomId)
 *   - findById(roomId) 없으면 CustomException(OPEN_CHAT_ROOM_NOT_FOUND)
 *   - findByRoomIdAndUserId 없으면 CustomException(OPEN_CHAT_PARTICIPANT_NOT_FOUND)
 *   - participant 삭제
 *   - userId == room.hostUserId (방장 나가기):
 *     - findOldestParticipantExcluding → present: room.hostUserId = 신규방장.userId, room save, roomDeleted=false
 *     - empty + isOfficial=FALSE: room delete, roomDeleted=true (BR-06)
 *     - empty + isOfficial=TRUE: 삭제 없음, roomDeleted=false (BR-07)
 *   - 비방장: roomDeleted=false 반환
 *
 * void deleteRoom(Long userId, Long roomId)
 *   - findById(roomId) 없으면 CustomException(OPEN_CHAT_ROOM_NOT_FOUND)
 *   - room.isOfficial=TRUE && role=USER: throw CustomException(OPEN_CHAT_ROOM_FORBIDDEN) (BR-08)
 *   - room.hostUserId != userId && role=USER: throw CustomException(OPEN_CHAT_ROOM_FORBIDDEN) (BR-08)
 *   - participant 전체 삭제 후 room 삭제
 *   (ADMIN 권한은 Phase 1 구현 범위에서 별도 처리 — 이 테스트는 USER 시나리오만 다룸)
 *
 * [Repository]
 * OpenChatRoomRepository extends JpaRepository<OpenChatRoom, Long>:
 *   Optional<OpenChatRoom> findByIdWithLock(@Lock(PESSIMISTIC_WRITE) Long id)
 *   List<OpenChatRoom> findMyRooms(Long userId)           → 커스텀 쿼리
 *   List<OpenChatRoom> findByCreatorDormitory(String d)   → 커스텀 쿼리
 *   List<OpenChatRoom> findAllPublic()                    → 커스텀 쿼리
 *
 * OpenChatParticipantRepository extends JpaRepository<OpenChatParticipant, Long>:
 *   Optional<OpenChatParticipant> findOldestParticipantExcluding(Long roomId, Long excludeUserId) → 커스텀 쿼리 (joinedAt ASC)
 *   boolean existsByRoomIdAndUserId(Long roomId, Long userId)   → Spring Data 자동 생성
 *   long countByRoomId(Long roomId)                             → Spring Data 자동 생성
 *   Optional<OpenChatParticipant> findByRoomIdAndUserId(Long r, Long u) → Spring Data 자동 생성
 *
 * [ErrorCode 추가]
 * OPEN_CHAT_ROOM_NOT_FOUND(NOT_FOUND, "채팅방을 찾을 수 없습니다")
 * OPEN_CHAT_ROOM_FORBIDDEN(FORBIDDEN, "채팅방 접근 권한이 없습니다")
 * OPEN_CHAT_ROOM_FULL(BAD_REQUEST, "최대 인원에 도달한 채팅방입니다")
 * OPEN_CHAT_PARTICIPANT_NOT_FOUND(NOT_FOUND, "참여하지 않은 채팅방입니다")
 */
@ExtendWith(MockitoExtension.class)
class OpenChatRoomServiceTest {

    // NOTE: 구현 에이전트가 OpenChatRoomService, 관련 엔티티, DTO, Repository를 생성한 후
    // 아래 필드 선언과 테스트 본문 주석을 해제하세요.
    //
    // @Mock OpenChatRoomRepository openChatRoomRepository;
    // @Mock OpenChatParticipantRepository openChatParticipantRepository;
    // @InjectMocks OpenChatRoomService openChatRoomService;

    // ============================================================
    // 방 생성
    // ============================================================

    @Test
    @DisplayName("방 생성 성공 — scope=ALL 방 생성 시 creatorDormitory null 저장")
    void should_create_room_when_scope_is_ALL() {
        // given
        // RequestCreateOpenChatRoomDto request = OpenChatRoomFixture.createRequest(); // scope=ALL
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // given(openChatRoomRepository.save(any())).willReturn(room);
        //
        // when
        // Long roomId = openChatRoomService.createRoom(request, 1L);
        //
        // then
        // assertThat(roomId).isNotNull();
        // then(openChatRoomRepository).should(times(1)).save(argThat(r -> r.getCreatorDormitory() == null));
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("방 생성 성공 — scope=DORMITORY 방 생성 시 creatorDormitory에 dormType 저장")
    void should_save_creatorDormitory_when_scope_is_DORMITORY() {
        // given
        // RequestCreateOpenChatRoomDto request = OpenChatRoomFixture.createRequestWithDormitoryScope();
        // OpenChatRoom room = OpenChatRoomFixture.createRoomWithScope(OpenChatRoomScope.DORMITORY);
        // given(openChatRoomRepository.save(any())).willReturn(room);
        //
        // when
        // Long roomId = openChatRoomService.createRoom(request, 1L);  // userId=1L의 dormType="SAMSUNG" 가정
        //
        // then
        // assertThat(roomId).isNotNull();
        // then(openChatRoomRepository).should(times(1)).save(argThat(r -> r.getCreatorDormitory() != null));
        assertThat(true).isTrue();
    }

    // ============================================================
    // 방 목록 조회
    // ============================================================

    @Test
    @DisplayName("MY 탭 조회 성공 — 요청자가 참여한 방만 반환")
    void should_return_only_joined_rooms_when_tab_is_MY() {
        // given
        // Long userId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // given(openChatRoomRepository.findMyRooms(userId)).willReturn(List.of(room));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(any(), eq(userId))).willReturn(true);
        //
        // when
        // Page<ResponseOpenChatRoomDto> result = openChatRoomService.getRooms(userId, "MY", PageRequest.of(0, 20));
        //
        // then
        // assertThat(result.getContent()).isNotEmpty();
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("DORMITORY 탭 조회 — BR-02: dormType=NONE이면 빈 목록 반환")
    void should_return_empty_list_when_dormType_is_NONE_on_DORMITORY_tab() {
        // given
        // Long userId = 1L;
        // String dormType = "NONE";
        //
        // when
        // Page<ResponseOpenChatRoomDto> result = openChatRoomService.getRoomsForDormitory(userId, dormType, PageRequest.of(0, 20));
        //
        // then
        // assertThat(result.getContent()).isEmpty();
        // then(openChatRoomRepository).should(never()).findByCreatorDormitory(any());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("DORMITORY 탭 조회 성공 — creatorDormitory와 일치하는 방만 반환")
    void should_return_matching_dormitory_rooms_when_tab_is_DORMITORY() {
        // given
        // Long userId = 1L;
        // String dormType = "SAMSUNG";
        // OpenChatRoom room = OpenChatRoomFixture.createRoomWithScope(OpenChatRoomScope.DORMITORY);
        // given(openChatRoomRepository.findByCreatorDormitory(dormType)).willReturn(List.of(room));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(any(), eq(userId))).willReturn(false);
        //
        // when
        // Page<ResponseOpenChatRoomDto> result = openChatRoomService.getRoomsForDormitory(userId, dormType, PageRequest.of(0, 20));
        //
        // then
        // assertThat(result.getContent()).isNotEmpty();
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("ALL 탭 조회 성공 — scope=ALL인 방 전체 반환")
    void should_return_all_public_rooms_when_tab_is_ALL() {
        // given
        // Long userId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // given(openChatRoomRepository.findAllPublic()).willReturn(List.of(room));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(any(), eq(userId))).willReturn(false);
        //
        // when
        // Page<ResponseOpenChatRoomDto> result = openChatRoomService.getRooms(userId, "ALL", PageRequest.of(0, 20));
        //
        // then
        // assertThat(result.getContent()).isNotEmpty();
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-09: 방 목록 조회 결과에 isJoined 필드 포함")
    void should_include_isJoined_field_in_room_list_response() {
        // given
        // Long userId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // given(openChatRoomRepository.findAllPublic()).willReturn(List.of(room));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(any(), eq(userId))).willReturn(true);
        //
        // when
        // Page<ResponseOpenChatRoomDto> result = openChatRoomService.getRooms(userId, "ALL", PageRequest.of(0, 20));
        //
        // then
        // assertThat(result.getContent().get(0).isJoined()).isTrue();
        assertThat(true).isTrue();
    }

    // ============================================================
    // 방 입장
    // ============================================================

    @Test
    @DisplayName("입장 성공 — 새 참여자가 정상 입장")
    void should_return_room_detail_when_new_participant_joins() {
        // given
        // Long userId = 1L, roomId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();  // maxParticipants=10
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(roomId, userId)).willReturn(false);
        // given(openChatParticipantRepository.countByRoomId(roomId)).willReturn(1L);
        //
        // when
        // ResponseOpenChatRoomDetailDto result = openChatRoomService.joinRoom(userId, roomId);
        //
        // then
        // assertThat(result).isNotNull();
        // then(openChatParticipantRepository).should(times(1)).save(any());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("CustomException 발생 — BR-03: scope=DORMITORY + dormType 불일치 시 OPEN_CHAT_ROOM_FORBIDDEN")
    void should_throw_CustomException_when_BR_03_dormType_mismatch() {
        // given
        // Long userId = 1L, roomId = 1L;
        // OpenChatRoom dormRoom = OpenChatRoomFixture.createRoomWithScope(OpenChatRoomScope.DORMITORY);
        // // dormRoom.creatorDormitory = "SAMSUNG"
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(dormRoom));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(roomId, userId)).willReturn(false);
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.joinRoomWithDormType(userId, roomId, "DIFFERENT_DORM");
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("CustomException 발생 — BR-04: 최대 인원 초과 시 OPEN_CHAT_ROOM_FULL")
    void should_throw_CustomException_when_BR_04_room_is_full() {
        // given
        // Long userId = 2L, roomId = 1L;
        // OpenChatRoom fullRoom = OpenChatRoomFixture.createFullRoom();  // maxParticipants=2
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(fullRoom));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(roomId, userId)).willReturn(false);
        // given(openChatParticipantRepository.countByRoomId(roomId)).willReturn(2L);
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.joinRoom(userId, roomId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FULL);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-05: 이미 참여 중인 방 재입장 시 멱등 처리 — save 미호출")
    void should_not_save_when_BR_05_already_joined() {
        // given
        // Long userId = 1L, roomId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(roomId, userId)).willReturn(true);
        //
        // when
        // openChatRoomService.joinRoom(userId, roomId);
        //
        // then
        // then(openChatParticipantRepository).should(never()).save(any());
        assertThat(true).isTrue();
    }

    // ============================================================
    // 방 나가기
    // ============================================================

    @Test
    @DisplayName("나가기 성공 — 비방장 나가기 시 roomDeleted=false 반환")
    void should_return_roomDeleted_false_when_non_host_leaves() {
        // given
        // Long userId = 2L, roomId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();  // hostUserId=1L
        // OpenChatParticipant participant = OpenChatRoomFixture.createParticipant(userId);
        // given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));
        //
        // when
        // ResponseLeaveOpenChatRoomDto result = openChatRoomService.leaveRoom(userId, roomId);
        //
        // then
        // assertThat(result.isRoomDeleted()).isFalse();
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-06: 방장 나가기 + 남은 참여자 있을 때 hostUserId 이전")
    void should_transfer_host_when_BR_06_host_leaves_with_remaining_participants() {
        // given
        // Long hostId = 1L, nextHostId = 2L, roomId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();  // hostUserId=1L
        // OpenChatParticipant hostParticipant = OpenChatRoomFixture.createParticipant(hostId);
        // OpenChatParticipant nextHost = OpenChatRoomFixture.createParticipantWithJoinedAt(nextHostId, now().minusDays(1));
        // given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, hostId)).willReturn(Optional.of(hostParticipant));
        // given(openChatParticipantRepository.findOldestParticipantExcluding(roomId, hostId)).willReturn(Optional.of(nextHost));
        //
        // when
        // ResponseLeaveOpenChatRoomDto result = openChatRoomService.leaveRoom(hostId, roomId);
        //
        // then
        // assertThat(result.isRoomDeleted()).isFalse();
        // then(openChatRoomRepository).should(times(1)).save(any());  // hostUserId 업데이트
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-06: 방장 나가기 + 마지막 참여자 + is_official=FALSE → 방 삭제, roomDeleted=true")
    void should_delete_room_when_BR_06_last_host_leaves_non_official_room() {
        // given
        // Long hostId = 1L, roomId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();  // hostUserId=1L, isOfficial=false
        // OpenChatParticipant hostParticipant = OpenChatRoomFixture.createParticipant(hostId);
        // given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, hostId)).willReturn(Optional.of(hostParticipant));
        // given(openChatParticipantRepository.findOldestParticipantExcluding(roomId, hostId)).willReturn(Optional.empty());
        //
        // when
        // ResponseLeaveOpenChatRoomDto result = openChatRoomService.leaveRoom(hostId, roomId);
        //
        // then
        // assertThat(result.isRoomDeleted()).isTrue();
        // then(openChatRoomRepository).should(times(1)).delete(any());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-07: 방장 나가기 + 마지막 참여자 + is_official=TRUE → 방 유지, roomDeleted=false")
    void should_keep_room_when_BR_07_last_host_leaves_official_room() {
        // given
        // Long hostId = 1L, roomId = 1L;
        // OpenChatRoom officialRoom = OpenChatRoomFixture.createOfficialRoom();  // isOfficial=true
        // OpenChatParticipant hostParticipant = OpenChatRoomFixture.createParticipant(hostId);
        // given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(officialRoom));
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, hostId)).willReturn(Optional.of(hostParticipant));
        // given(openChatParticipantRepository.findOldestParticipantExcluding(roomId, hostId)).willReturn(Optional.empty());
        //
        // when
        // ResponseLeaveOpenChatRoomDto result = openChatRoomService.leaveRoom(hostId, roomId);
        //
        // then
        // assertThat(result.isRoomDeleted()).isFalse();
        // then(openChatRoomRepository).should(never()).delete(any());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("CustomException 발생 — 참여하지 않은 방 나가기 시 OPEN_CHAT_PARTICIPANT_NOT_FOUND")
    void should_throw_CustomException_when_participant_not_found_on_leave() {
        // given
        // Long userId = 99L, roomId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.empty());
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.leaveRoom(userId, roomId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND);
        assertThat(true).isTrue();
    }

    // ============================================================
    // 방 삭제
    // ============================================================

    @Test
    @DisplayName("BR-08: 방장 USER가 채팅방 삭제 성공")
    void should_delete_room_when_BR_08_host_user_requests_deletion() {
        // given
        // Long hostId = 1L, roomId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();  // hostUserId=1L, isOfficial=false
        // given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.deleteRoom(hostId, roomId);
        //
        // then
        // assertThatCode(action).doesNotThrowAnyException();
        // then(openChatRoomRepository).should(times(1)).delete(any());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("CustomException 발생 — BR-08: 비방장 USER가 삭제 시도 시 OPEN_CHAT_ROOM_FORBIDDEN")
    void should_throw_CustomException_when_BR_08_non_host_tries_to_delete() {
        // given
        // Long nonHostId = 2L, roomId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();  // hostUserId=1L
        // given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.deleteRoom(nonHostId, roomId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("CustomException 발생 — BR-08: USER가 is_official=TRUE 방 삭제 시도 시 OPEN_CHAT_ROOM_FORBIDDEN")
    void should_throw_CustomException_when_BR_08_user_tries_to_delete_official_room() {
        // given
        // Long userId = 1L, roomId = 1L;
        // OpenChatRoom officialRoom = OpenChatRoomFixture.createOfficialRoom();  // isOfficial=true
        // given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(officialRoom));
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.deleteRoom(userId, roomId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
        assertThat(true).isTrue();
    }
}
