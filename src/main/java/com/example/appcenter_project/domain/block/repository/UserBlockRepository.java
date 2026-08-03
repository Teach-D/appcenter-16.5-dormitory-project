package com.example.appcenter_project.domain.block.repository;

import com.example.appcenter_project.domain.block.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    boolean existsByBlockerIdInAndBlockedId(Collection<Long> blockerIds, Long blockedId);

    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    List<UserBlock> findAllByBlockerId(Long blockerId);
}
