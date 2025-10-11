package com.example.appcenter_project.repository.user;

import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.enums.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNumber(String studentNumber);
    Boolean existsByStudentNumber(String studentNumber);
    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByName(String name);

    List<User> findByDormTypeNot(DormType dormType);
    
    // 특정 역할들을 가진 사용자들 조회
    List<User> findByRoleIn(List<Role> roles);

    List<User> findByRole(Role role);
}
