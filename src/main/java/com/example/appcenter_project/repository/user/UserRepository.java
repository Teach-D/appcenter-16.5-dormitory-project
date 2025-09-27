package com.example.appcenter_project.repository.user;

import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.user.DormType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNumber(String studentNumber);
    Boolean existsByStudentNumber(String studentNumber);
    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByName(String name);

    List<User> findByDormTypeNot(DormType dormType);
}
