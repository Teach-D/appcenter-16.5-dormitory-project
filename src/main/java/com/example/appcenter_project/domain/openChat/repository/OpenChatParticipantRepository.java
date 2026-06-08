package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface OpenChatParticipantRepository extends JpaRepository<OpenChatParticipant, Long>, OpenChatParticipantQuerydslRepository {

    @Query("SELECT p FROM OpenChatParticipant p WHERE p.roomId = :roomId AND p.userId <> :excludeUserId ORDER BY p.joinedAt ASC")
    List<OpenChatParticipant> findOldestParticipantExcludingList(
            @Param("roomId") Long roomId,
            @Param("excludeUserId") Long excludeUserId,
            Pageable pageable);

    default Optional<OpenChatParticipant> findOldestParticipantExcluding(Long roomId, Long excludeUserId) {
        List<OpenChatParticipant> result = findOldestParticipantExcludingList(
                roomId, excludeUserId, org.springframework.data.domain.PageRequest.of(0, 1));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    long countByRoomId(Long roomId);

    Optional<OpenChatParticipant> findByRoomIdAndUserId(Long roomId, Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM OpenChatParticipant p WHERE p.roomId = :roomId")
    void deleteAllByRoomId(@Param("roomId") Long roomId);
}
