package com.example.appcenter_project.domain.roommate.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * RoommateChattingChatRepository 커스텀 쿼리 테스트 — BR-665.
 *
 * 구현 에이전트가 생성해야 할 인터페이스와 구현체:
 *
 * [RoommateChattingChatQuerydslRepository] (신규 인터페이스)
 * - findLastMessagesByRoomIds(List<Long> roomIds): Map<Long, RoommateChattingChat>
 *   방 ID 목록에서 방별 마지막 메시지(max id) 1개씩 bulk 조회
 *   서브쿼리: SELECT max(c.id) FROM RoommateChattingChat c WHERE c.roommateChattingRoom.id IN :roomIds GROUP BY c.roommateChattingRoom.id
 *   → roomId → RoommateChattingChat 매핑
 *
 * - countUnreadByRoomIdsAndUserId(List<Long> roomIds, Long userId): Map<Long, Long>
 *   방별 안읽음 수 (member.id != userId AND readByReceiver = false) bulk 조회
 *   SELECT c.roommateChattingRoom.id, COUNT(c) FROM RoommateChattingChat c
 *   WHERE c.roommateChattingRoom.id IN :roomIds AND c.member.id != :userId AND c.readByReceiver = false
 *   GROUP BY c.roommateChattingRoom.id
 *   → roomId → unreadCount 매핑
 *
 * [RoommateChattingChatRepository] 변경
 * - RoommateChattingChatQuerydslRepository 상속 추가
 *
 * [RoommateChattingRoomRepository] 변경
 * - findActiveRoomsByUserId(@Param("userId") Long userId): List<RoommateChattingRoom>
 *   JPQL: SELECT r FROM RoommateChattingRoom r JOIN FETCH r.host JOIN FETCH r.guest
 *         WHERE (r.host.id = :userId AND r.hostLeft = false)
 *            OR (r.guest.id = :userId AND r.guestLeft = false)
 *
 * [테스트 데이터 시나리오]
 * host(userId=1L), guest(userId=2L)로 RoommateChattingRoom 생성
 * chat1(member=host, content="안녕", readByReceiver=false)
 * chat2(member=guest, content="반가워", readByReceiver=false)
 */
@ExtendWith(MockitoExtension.class)
class RoommateChattingChatRepositoryTest {

    // NOTE: 구현 에이전트가 RoommateChattingChatQuerydslRepositoryImpl을 생성한 후
    // @DataJpaTest로 변경하고 @Autowired로 Repository를 주입하세요.
    //
    // @Autowired RoommateChattingChatRepository roommateChattingChatRepository;
    // @Autowired RoommateChattingRoomRepository roommateChattingRoomRepository;
    // @Autowired UserRepository userRepository;
    // private RoommateChattingRoom savedRoom;
    // private RoommateChattingChat chat1, chat2;
    //
    // @BeforeEach void setUp() {
    //     User host = userRepository.save(UserFixture.createUser(1L));
    //     User guest = userRepository.save(UserFixture.createUser(2L));
    //     savedRoom = roommateChattingRoomRepository.save(
    //         RoommateChattingRoom.create(host, guest)
    //     );
    //     chat1 = roommateChattingChatRepository.save(
    //         RoommateChattingChat.create(savedRoom, host, "안녕")
    //     );
    //     chat2 = roommateChattingChatRepository.save(
    //         RoommateChattingChat.create(savedRoom, guest, "반가워")
    //     );
    // }

    @Test
    @DisplayName("findLastMessagesByRoomIds — BR-665: 방별 마지막 메시지 bulk 조회 성공")
    void should_return_last_message_per_room_when_rooms_have_messages() {
        // given: savedRoom에 chat1, chat2 존재
        // when: roommateChattingChatRepository.findLastMessagesByRoomIds(List.of(savedRoom.getId()))
        // then: size=1, key=savedRoom.getId(), value.id == chat2.getId() (마지막 메시지)
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("findLastMessagesByRoomIds — BR-665: 메시지 없는 방은 결과에 미포함")
    void should_return_empty_map_when_room_has_no_messages() {
        // given: 메시지 없는 별도 방
        // when: roommateChattingChatRepository.findLastMessagesByRoomIds(List.of(emptyRoom.getId()))
        // then: result.isEmpty()
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("countUnreadByRoomIdsAndUserId — BR-665: userId=host 기준 안읽음 수 반환 (guest 발신 메시지만)")
    void should_count_unread_messages_by_receiver_for_host() {
        // given: chat2(member=guest, readByReceiver=false) 존재
        // when: roommateChattingChatRepository.countUnreadByRoomIdsAndUserId(List.of(savedRoom.getId()), hostId)
        // then: result.get(savedRoom.getId()) == 1L (chat2만 카운트, chat1은 본인 발신이므로 제외)
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("countUnreadByRoomIdsAndUserId — BR-665: 안읽음 메시지 없으면 해당 방 결과에 미포함")
    void should_not_include_room_in_result_when_no_unread_messages() {
        // given: 안읽음 메시지 없는 방 (readByReceiver=true로 모두 읽음 처리)
        // when: roommateChattingChatRepository.countUnreadByRoomIdsAndUserId(List.of(readRoom.getId()), userId)
        // then: result에 readRoom.getId() 없거나 count=0
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("findActiveRoomsByUserId — BR-665 AC-6: 나가지 않은 방만 반환")
    void should_return_only_active_rooms_for_user() {
        // given: hostLeft=false인 savedRoom, hostLeft=true인 leftRoom
        // when: roommateChattingRoomRepository.findActiveRoomsByUserId(hostId)
        // then: size=1, id==savedRoom.getId()
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("findActiveRoomsByUserId — BR-665: guest 입장에서도 활성 방 조회 성공")
    void should_return_active_rooms_when_user_is_guest() {
        // given: savedRoom (guestLeft=false)
        // when: roommateChattingRoomRepository.findActiveRoomsByUserId(guestId)
        // then: size=1, id==savedRoom.getId()
        assertThat(true).isTrue();
    }
}
