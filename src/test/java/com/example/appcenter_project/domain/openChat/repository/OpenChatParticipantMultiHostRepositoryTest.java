package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class OpenChatParticipantMultiHostRepositoryTest {

    @Autowired
    private OpenChatParticipantRepository openChatParticipantRepository;

    private static final Long ROOM_ID = 1L;
    private static final Long HOST_USER_ID = 10L;
    private static final Long PARTICIPANT_USER_ID = 20L;
    private static final Long ANOTHER_HOST_USER_ID = 30L;

    @BeforeEach
    void setUp() {
        openChatParticipantRepository.save(OpenChatParticipant.create(ROOM_ID, HOST_USER_ID, true));
        openChatParticipantRepository.save(OpenChatParticipant.create(ROOM_ID, PARTICIPANT_USER_ID, false));
        openChatParticipantRepository.save(OpenChatParticipant.create(ROOM_ID, ANOTHER_HOST_USER_ID, true));
    }

    @AfterEach
    void tearDown() {
        openChatParticipantRepository.deleteAll();
    }

    @Test
    @DisplayName("countByRoomIdAndIsHost — isHost=true 방장 수 2명 반환")
    void should_return_host_count_when_multiple_hosts_exist() {
        long hostCount = openChatParticipantRepository.countByRoomIdAndIsHost(ROOM_ID, true);
        assertThat(hostCount).isEqualTo(2L);
    }

    @Test
    @DisplayName("countByRoomIdAndIsHost — isHost=false 일반 참여자 수 1명 반환")
    void should_return_non_host_count_correctly() {
        long nonHostCount = openChatParticipantRepository.countByRoomIdAndIsHost(ROOM_ID, false);
        assertThat(nonHostCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("existsByRoomIdAndUserIdAndIsHost — 방장 존재 시 true 반환")
    void should_return_true_when_user_is_host() {
        boolean exists = openChatParticipantRepository.existsByRoomIdAndUserIdAndIsHost(ROOM_ID, HOST_USER_ID, true);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByRoomIdAndUserIdAndIsHost — 일반 참여자 방장 여부 false 반환")
    void should_return_false_when_user_is_not_host() {
        boolean exists = openChatParticipantRepository.existsByRoomIdAndUserIdAndIsHost(ROOM_ID, PARTICIPANT_USER_ID, true);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findAllByRoomIdWithLock — 해당 방의 모든 참여자 반환 (비관적 락 SELECT FOR UPDATE)")
    void should_return_all_participants_with_pessimistic_lock() {
        List<OpenChatParticipant> participants = openChatParticipantRepository.findAllByRoomIdWithLock(ROOM_ID);
        assertThat(participants).hasSize(3);
    }

    @Test
    @DisplayName("findAllByRoomIdWithLock — 다른 방 participant는 반환하지 않음")
    void should_not_return_participants_from_other_rooms() {
        Long otherRoomId = 999L;
        openChatParticipantRepository.save(OpenChatParticipant.create(otherRoomId, HOST_USER_ID, true));
        List<OpenChatParticipant> participants = openChatParticipantRepository.findAllByRoomIdWithLock(ROOM_ID);
        assertThat(participants).hasSize(3);
        assertThat(participants).noneMatch(p -> p.getRoomId().equals(otherRoomId));
    }
}
