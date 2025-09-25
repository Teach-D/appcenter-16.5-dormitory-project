package com.example.appcenter_project.repository.user;

import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.entity.user.UserGroupOrderCategory;
import com.example.appcenter_project.entity.user.UserGroupOrderKeyword;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGroupOrderCategoryRepository extends JpaRepository<UserGroupOrderCategory, Long> {
    boolean existsByCategory(GroupOrderType groupOrderType);
    List<UserGroupOrderCategory> findByUserId(Long userId);
    List<UserGroupOrderCategory> user(User user);
    Optional<UserGroupOrderCategory> findByUserIdAndCategory(Long userId, GroupOrderType beforeCategory);
}
