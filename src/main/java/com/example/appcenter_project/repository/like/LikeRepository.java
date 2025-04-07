package com.example.appcenter_project.repository.like;

import com.example.appcenter_project.entity.like.GroupOrderLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeRepository extends JpaRepository<GroupOrderLike, Long> {
    List<GroupOrderLike> findByUser_Id(Long userId);
}
