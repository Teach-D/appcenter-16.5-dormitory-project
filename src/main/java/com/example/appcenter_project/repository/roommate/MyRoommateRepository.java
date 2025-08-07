package com.example.appcenter_project.repository.roommate;

import com.example.appcenter_project.entity.roommate.MyRoommate;
import com.example.appcenter_project.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyRoommateRepository extends JpaRepository<MyRoommate, Long> {
    Optional<MyRoommate> findByUserId(Long userId);  // userId로 내 룸메 정보 조회
    void deleteByUserAndRoommate(User user, User roommate);

    Optional<MyRoommate> findByUserAndRoommate(User sender, User receiver);
}
