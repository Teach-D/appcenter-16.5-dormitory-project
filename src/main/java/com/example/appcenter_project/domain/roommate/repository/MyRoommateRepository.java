package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.MyRoommate;
import com.example.appcenter_project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MyRoommateRepository extends JpaRepository<MyRoommate, Long> {
    Optional<MyRoommate> findByUserId(Long userId);  // userId로 내 룸메 정보 조회
    
    void deleteByUserAndRoommate(User user, User roommate);

    Optional<MyRoommate> findByUserAndRoommate(User sender, User receiver);
    
    // 더 명확한 쿼리 메서드들 추가
    @Query("SELECT m FROM MyRoommate m WHERE m.user.id = :userId AND m.roommate.id = :roommateId")
    Optional<MyRoommate> findByUserIdAndRoommateId(@Param("userId") Long userId, @Param("roommateId") Long roommateId);
    
    @Modifying
    @Query("DELETE FROM MyRoommate m WHERE m.user.id = :userId AND m.roommate.id = :roommateId")
    int deleteByUserIdAndRoommateId(@Param("userId") Long userId, @Param("roommateId") Long roommateId);
    
    // 두 사용자 간의 모든 MyRoommate 관계 조회
    @Query("SELECT m FROM MyRoommate m WHERE (m.user.id = :userId1 AND m.roommate.id = :userId2) OR (m.user.id = :userId2 AND m.roommate.id = :userId1)")
    List<MyRoommate> findAllByTwoUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
