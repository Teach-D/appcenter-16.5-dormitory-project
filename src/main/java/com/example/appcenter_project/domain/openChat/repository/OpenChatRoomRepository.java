package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OpenChatRoomRepository extends JpaRepository<OpenChatRoom, Long>, OpenChatRoomQuerydslRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM OpenChatRoom r WHERE r.id = :roomId")
    Optional<OpenChatRoom> findByIdWithLock(@Param("roomId") Long roomId);
}
