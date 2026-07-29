package com.example.appcenter_project.domain.block.fixture;

import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;

public class BlockFixture {

    public static Long blockerId() {
        return 1L;
    }

    public static Long blockedId() {
        return 2L;
    }

    public static Long nonExistentUserId() {
        return 99999L;
    }

    public static User createUser() {
        return User.createForTest(2L, "target-user", DormType.DORM_1);
    }

    public static User createUserWithId(Long id) {
        return User.createForTest(id, "user-" + id, DormType.DORM_1);
    }
}
