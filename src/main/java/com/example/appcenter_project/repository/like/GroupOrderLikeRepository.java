package com.example.appcenter_project.repository.like;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<GroupOrderLike, Long> {
    List<GroupOrderLike> findByUser_Id(Long userId);
    boolean existsByUserAndGroupOrder(User user, GroupOrder groupOrder);
    Optional<GroupOrderLike> findByUserAndGroupOrder(User user, GroupOrder groupOrder);
}
