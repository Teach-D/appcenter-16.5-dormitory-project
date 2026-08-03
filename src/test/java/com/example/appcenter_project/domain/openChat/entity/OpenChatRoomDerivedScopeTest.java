package com.example.appcenter_project.domain.openChat.entity;

import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomType;
import com.example.appcenter_project.domain.user.enums.DormType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenChatRoomDerivedScopeTest {

    @Test
    @DisplayName("createDerived — 부모가 기숙사방이면(creatorDormitory 있음) 파생방도 DORMITORY scope (버그2b)")
    void derivedInheritsDormitoryScope() {
        OpenChatRoom derived = OpenChatRoom.createDerived(
                "파생방", "설명", 10, 1L, null, true, DormType.DORM_1.name());

        assertThat(derived.getScope()).isEqualTo(OpenChatRoomScope.DORMITORY);
        assertThat(derived.getCreatorDormitory()).isEqualTo(DormType.DORM_1.name());
        assertThat(derived.getRoomType()).isEqualTo(OpenChatRoomType.DERIVED);
    }

    @Test
    @DisplayName("createDerived — 부모가 전체방이면(creatorDormitory=null) 파생방은 ALL scope 유지")
    void derivedKeepsAllScope() {
        OpenChatRoom derived = OpenChatRoom.createDerived(
                "파생방", "설명", 10, 1L, null, true, null);

        assertThat(derived.getScope()).isEqualTo(OpenChatRoomScope.ALL);
        assertThat(derived.getCreatorDormitory()).isNull();
    }
}