package com.example.appcenter_project.domain.user.repository;

import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.enums.Role;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNumber(String studentNumber);
    Boolean existsByStudentNumber(String studentNumber);
    Optional<User> findByRefreshToken(String refreshToken);
    List<User> findByDormTypeNot(DormType dormType);
    List<User> findByRoleIn(List<Role> roles);
    List<User> findByRole(Role role);
    List<User> findByReceiveNotificationTypesContains(NotificationType notificationType);

    @Query("SELECT u FROM User u " +
            "WHERE :notificationType MEMBER OF u.receiveNotificationTypes " +
            "AND u.role NOT IN :excludedRoles")
    List<User> findByReceiveNotificationTypesContainsAndRoleNotIn(
            @Param("notificationType") NotificationType notificationType,
            @Param("excludedRoles") List<Role> excludedRoles
    );

    @Query("SELECT u FROM User u " +
            "WHERE u.dormType != :dormType " +
            "AND :notificationType MEMBER OF u.receiveNotificationTypes " +
            "AND u.role NOT IN :excludedRoles")
    List<User> findByDormTypeNotAndReceiveNotificationTypesContainsAndRoleNotIn(
            @Param("dormType") DormType dormType,
            @Param("notificationType") NotificationType notificationType,
            @Param("excludedRoles") List<Role> excludedRoles
    );
}
