package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import com.example.appcenter_project.domain.user.enums.DormType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class OpenChatRoomVisibilityRepositoryTest {

    @Autowired
    private OpenChatRoomRepository openChatRoomRepository;

    private static final Long CREATOR = 1L;

    @AfterEach
    void tearDown() {
        openChatRoomRepository.deleteAll();
    }

    @Test
    @DisplayName("findAllPublicRooms — DORMITORY(일부방)은 전체 탭에서 제외된다 (버그1)")
    void allTab_excludesDormitoryRoom() {
        OpenChatRoom dormRoom = openChatRoomRepository.save(OpenChatRoom.create(
                "1기숙사방", "설명", OpenChatRoomScope.DORMITORY, 10, CREATOR,
                DormType.DORM_1.name(), false, null, true));
        OpenChatRoom globalRoom = openChatRoomRepository.save(OpenChatRoom.create(
                "전체방", "설명", OpenChatRoomScope.ALL, 10, CREATOR,
                null, false, null, true));

        List<OpenChatRoom> result = openChatRoomRepository.findAllPublicRooms(null);

        assertThat(result).extracting(OpenChatRoom::getId)
                .contains(globalRoom.getId())
                .doesNotContain(dormRoom.getId());
    }

    @Test
    @DisplayName("findAllPublicRooms — 공개 파생방(부모=전체, cd=null)은 전체 탭에 노출된다")
    void allTab_includesPublicDerivedFromAllParent() {
        OpenChatRoom derived = openChatRoomRepository.save(OpenChatRoom.createDerived(
                "공개 파생방", "설명", 10, CREATOR, null, true, null));

        List<OpenChatRoom> result = openChatRoomRepository.findAllPublicRooms(null);

        assertThat(result).extracting(OpenChatRoom::getId).contains(derived.getId());
    }

    @Test
    @DisplayName("findAllPublicRooms — 파생방(부모=1기숙사)은 전체 탭에서 제외된다 (버그2b)")
    void allTab_excludesDerivedFromDormParent() {
        OpenChatRoom derived = openChatRoomRepository.save(OpenChatRoom.createDerived(
                "1기숙사 파생방", "설명", 10, CREATOR, null, true, DormType.DORM_1.name()));

        List<OpenChatRoom> result = openChatRoomRepository.findAllPublicRooms(null);

        assertThat(result).extracting(OpenChatRoom::getId).doesNotContain(derived.getId());
    }

    @Test
    @DisplayName("findAllPublicRooms — 관리자 공식 기숙사방(targetDorm)은 전체 탭에 계속 노출된다 (공식방 미변경 가드)")
    void allTab_keepsOfficialDormRoom() {
        OpenChatRoom official = openChatRoomRepository.save(
                OpenChatRoom.createDormOfficial("1기숙사 공식방", "설명", CREATOR, DormType.DORM_1));

        List<OpenChatRoom> result = openChatRoomRepository.findAllPublicRooms(null);

        assertThat(result).extracting(OpenChatRoom::getId).contains(official.getId());
    }

    @Test
    @DisplayName("findByDormitory — 비노출(isPublic=false) 기숙사방은 기숙사 탭에서 제외된다 (버그2a)")
    void dormTab_excludesHiddenRoom() {
        OpenChatRoom hidden = openChatRoomRepository.save(OpenChatRoom.create(
                "비노출 1기숙사방", "설명", OpenChatRoomScope.DORMITORY, 10, CREATOR,
                DormType.DORM_1.name(), false, null, false));
        OpenChatRoom visible = openChatRoomRepository.save(OpenChatRoom.create(
                "공개 1기숙사방", "설명", OpenChatRoomScope.DORMITORY, 10, CREATOR,
                DormType.DORM_1.name(), false, null, true));

        List<OpenChatRoom> result = openChatRoomRepository.findByDormitory(DormType.DORM_1.name(), null);

        assertThat(result).extracting(OpenChatRoom::getId)
                .contains(visible.getId())
                .doesNotContain(hidden.getId());
    }

    @Test
    @DisplayName("findByDormitory — 공개 파생방(부모=1기숙사)은 1기숙사 탭에 노출된다")
    void dormTab_includesPublicDerivedFromDormParent() {
        OpenChatRoom derived = openChatRoomRepository.save(OpenChatRoom.createDerived(
                "1기숙사 공개 파생방", "설명", 10, CREATOR, null, true, DormType.DORM_1.name()));

        List<OpenChatRoom> result = openChatRoomRepository.findByDormitory(DormType.DORM_1.name(), null);

        assertThat(result).extracting(OpenChatRoom::getId).contains(derived.getId());
    }
}