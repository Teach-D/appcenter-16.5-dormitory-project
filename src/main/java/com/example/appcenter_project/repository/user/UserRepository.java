package com.example.appcenter_project.repository.user;

import com.example.appcenter_project.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNumber(String studentNumber);
    Boolean existsByStudentNumber(String studentNumber);
    Optional<User> findByRefreshToken(String refreshToken);
}
