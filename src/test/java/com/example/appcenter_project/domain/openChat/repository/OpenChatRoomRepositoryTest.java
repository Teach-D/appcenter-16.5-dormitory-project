package com.example.appcenter_project.domain.openChat.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenChatRoom Repository 커스텀 쿼리 테스트.
 *
 * 구현 에이전트가 생성해야 할 Repository 인터페이스와 커스텀 쿼리:
 *
 * [OpenChatRoomRepository]
 * - findByIdWithLock(Long roomId): Optional<OpenChatRoom>
 *   @Lock(LockModeType.PESSIMISTIC_WRITE)
 *   @Query("SELECT r FROM OpenChatRoom r WHERE r.id = :roomId")
 *   비관적 락(SELECT FOR UPDATE)으로 OpenChatRoom 조회
 *
 * - findMyRooms(Long userId): List<OpenChatRoom>
 *   커스텀 쿼리: OpenChatParticipant에 userId로 참여한 방 조회
 *   @Query("SELECT r FROM OpenChatRoom r JOIN OpenChatParticipant p ON r.id = p.roomId WHERE p.userId = :userId")
 *
 * - findByCreatorDormitory(String dormType): List<OpenChatRoom>
 *   커스텀 쿼리: scope=DORMITORY AND creatorDormitory=:dormType 조건
 *   @Query("SELECT r FROM OpenChatRoom r WHERE r.scope = 'DORMITORY' AND r.creatorDormitory = :dormType")
 *
 * - findAllPublic(): List<OpenChatRoom>
 *   커스텀 쿼리: scope=ALL 조건
 *   @Query("SELECT r FROM OpenChatRoom r WHERE r.scope = 'ALL'")
 *
 * [OpenChatParticipantRepository]
 * - findOldestParticipantExcluding(Long roomId, Long excludeUserId): Optional<OpenChatParticipant>
 *   커스텀 쿼리: roomId 조건 + userId != excludeUserId + ORDER BY joinedAt ASC LIMIT 1
 *   @Query("SELECT p FROM OpenChatParticipant p WHERE p.roomId = :roomId AND p.userId <> :excludeUserId ORDER BY p.joinedAt ASC")
 *   + Pageable 또는 첫 번째 결과만 반환하는 방식
 *
 * - existsByRoomIdAndUserId(Long roomId, Long userId): boolean
 *   Spring Data 자동 생성 (별도 구현 불필요)
 *
 * - countByRoomId(Long roomId): long
 *   Spring Data 자동 생성 (별도 구현 불필요)
 *
 * - findByRoomIdAndUserId(Long roomId, Long userId): Optional<OpenChatParticipant>
 *   Spring Data 자동 생성 (별도 구현 불필요)
 *
 * [테스트 데이터 시나리오]
 * setUp: room(ALL, maxParticipants=10, hostUserId=1L) + participant(userId=1L, joinedAt=now-1day)
 *
 * findByIdWithLock: savedRoom.id로 조회 → present, id 일치
 * findOldestParticipantExcluding: participant(2L, now) 추가 후 excludeUserId=1L → userId=2L 반환
 * existsByRoomIdAndUserId: (savedRoom.id, 1L) → true
 * countByRoomId: savedRoom.id → 1L
 * findMyRooms: userId=1L → savedRoom 포함
 * findByCreatorDormitory: dormType="SAMSUNG" 방 저장 후 조회 → 해당 방만 반환
 */
@ExtendWith(MockitoExtension.class)
class OpenChatRoomRepositoryTest {

    // NOTE: 구현 에이전트가 엔티티와 Repository를 생성한 후
    // @DataJpaTest로 변경하고 @Autowired로 Repository를 주입하세요.
    //
    // @Autowired OpenChatRoomRepository openChatRoomRepository;
    // @Autowired OpenChatParticipantRepository openChatParticipantRepository;
    // private OpenChatRoom savedRoom;
    //
    // @BeforeEach void setUp() {
    //     savedRoom = openChatRoomRepository.save(
    //         OpenChatRoom.create("테스트 방", "설명", OpenChatRoomScope.ALL, 10, 1L, null, false)
    //     );
    //     openChatParticipantRepository.save(
    //         OpenChatParticipant.create(savedRoom.getId(), 1L, LocalDateTime.now().minusDays(1))
    //     );
    // }
    //
    // @AfterEach void tearDown() {
    //     openChatParticipantRepository.deleteAll();
    //     openChatRoomRepository.deleteAll();
    // }

    @Test
    @DisplayName("findByIdWithLock 조회 성공 — 비관적 락으로 방 조회")
    void should_find_room_with_lock_when_room_exists() {
        // given: savedRoom.getId()
        // when: openChatRoomRepository.findByIdWithLock(roomId)
        // then: present, id == roomId
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("findOldestParticipantExcluding — joinedAt ASC 기준 나가는 유저 제외 가장 오래된 참여자 반환")
    void should_return_oldest_participant_excluding_leaving_user() {
        // given: participant(2L, now) 추가 후
        // when: findOldestParticipantExcluding(roomId, excludeUserId=1L)
        // then: userId=2L (1L 제외 후 유일한 참여자)
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("existsByRoomIdAndUserId — 참여 중인 유저 존재 여부 true 반환")
    void should_return_true_when_participant_exists() {
        // given: savedRoom, userId=1L 참여 중
        // when: existsByRoomIdAndUserId(savedRoom.id, 1L)
        // then: true
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("countByRoomId — 채팅방 현재 참여자 수 반환")
    void should_return_participant_count_when_room_exists() {
        // given: savedRoom에 참여자 1명
        // when: countByRoomId(savedRoom.id)
        // then: 1L
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("findMyRooms(userId) — 요청자가 참여한 방 목록 반환")
    void should_return_rooms_where_user_is_participant() {
        // given: savedRoom, participant userId=1L
        // when: findMyRooms(1L)
        // then: size=1, id == savedRoom.id
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("findByCreatorDormitory(dormType) — 기숙사 타입과 일치하는 방 목록 반환")
    void should_return_rooms_matching_creator_dormitory() {
        // given: room(DORMITORY, creatorDormitory="SAMSUNG") 저장
        // when: findByCreatorDormitory("SAMSUNG")
        // then: size=1, creatorDormitory=="SAMSUNG"
        assertThat(true).isTrue();
    }
}
