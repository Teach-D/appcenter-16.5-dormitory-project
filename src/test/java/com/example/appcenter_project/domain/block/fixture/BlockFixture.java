package com.example.appcenter_project.domain.block.fixture;

// TODO: 구현 후 아래 import 주석 해제
// import com.example.appcenter_project.domain.block.entity.UserBlock;

public class BlockFixture {

    // UserBlock 엔티티가 구현되면 주석 해제
    // public static UserBlock createUserBlock(Long blockerId, Long blockedId) {
    //     return UserBlock.create(blockerId, blockedId);
    // }

    public static Long blockerId() {
        return 1L;
    }

    public static Long blockedId() {
        return 2L;
    }

    public static Long nonExistentUserId() {
        return 99999L;
    }
}
