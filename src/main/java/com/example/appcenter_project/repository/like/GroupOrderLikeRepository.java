package com.example.appcenter_project.repository.like;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.user.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupOrderLikeRepository extends JpaRepository<GroupOrderLike, Long> {
    List<GroupOrderLike> findByUserId(Long userId);
    boolean existsByUserAndGroupOrder(User user, GroupOrder groupOrder);
    Optional<GroupOrderLike> findByUserAndGroupOrder(User user, GroupOrder groupOrder);
    boolean existsByUserIdAndGroupOrderId(Long userId, Long groupOrderId);

    @Query("SELECT gol FROM GroupOrderLike gol JOIN FETCH gol.groupOrder g WHERE gol.user.id = :userId")
    List<GroupOrderLike> findByUserIdWithGroupOrder(@Param("userId") Long userId);
}
