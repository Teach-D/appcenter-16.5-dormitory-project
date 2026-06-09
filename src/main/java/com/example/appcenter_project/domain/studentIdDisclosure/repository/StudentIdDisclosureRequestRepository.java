package com.example.appcenter_project.domain.studentIdDisclosure.repository;

import com.example.appcenter_project.domain.studentIdDisclosure.entity.StudentIdDisclosureRequest;
import com.example.appcenter_project.domain.studentIdDisclosure.enums.DisclosureRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface StudentIdDisclosureRequestRepository extends JpaRepository<StudentIdDisclosureRequest, Long> {

    Optional<StudentIdDisclosureRequest> findByRequesterIdAndTargetIdAndRoomId(Long requesterId, Long targetId, Long roomId);

    boolean existsByRequesterIdAndTargetIdAndRoomIdAndStatusIn(Long requesterId, Long targetId, Long roomId, List<DisclosureRequestStatus> statuses);

    @Modifying
    @Transactional
    void deleteByRequesterIdAndTargetIdAndRoomId(Long requesterId, Long targetId, Long roomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM StudentIdDisclosureRequest r WHERE r.roomId = :roomId AND (r.requesterId = :userId OR r.targetId = :userId)")
    void deleteByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    Optional<StudentIdDisclosureRequest> findByRoomIdAndRequesterIdAndTargetIdAndStatus(Long roomId, Long requesterId, Long targetId, DisclosureRequestStatus status);
}
