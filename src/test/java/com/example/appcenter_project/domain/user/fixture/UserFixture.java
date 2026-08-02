package com.example.appcenter_project.domain.user.fixture;

import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;

public class UserFixture {

    public static User createUserWithDorm(Long id, DormType dormType) {
        return User.createForTest(id, "user-" + id, dormType);
    }
}
