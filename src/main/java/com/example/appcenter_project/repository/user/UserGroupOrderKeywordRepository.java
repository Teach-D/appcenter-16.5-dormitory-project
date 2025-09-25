package com.example.appcenter_project.repository.user;

import com.example.appcenter_project.entity.user.UserGroupOrderKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGroupOrderKeywordRepository extends JpaRepository<UserGroupOrderKeyword, Long> {
    List<UserGroupOrderKeyword> findByUserId(Long userId);

    Optional<UserGroupOrderKeyword> findByUserIdAndKeyword(Long userId, String beforeKeyword);

    void deleteByUserIdAndKeyword(Long userId, String keyword);

    boolean existsByKeyword(String keyword);
}
