package com.example.appcenter_project.domain.block.service;

import com.example.appcenter_project.domain.block.dto.response.ResponseBlockedUserDto;
import com.example.appcenter_project.domain.block.entity.UserBlock;
import com.example.appcenter_project.domain.block.repository.UserBlockRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    @Transactional
    public void blockUser(Long requesterId, Long targetId) {
        if (requesterId.equals(targetId)) {
            throw new CustomException(ErrorCode.USER_BLOCK_CANNOT_BLOCK_SELF);
        }
        userRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (userBlockRepository.existsByBlockerIdAndBlockedId(requesterId, targetId)) {
            throw new CustomException(ErrorCode.USER_BLOCK_ALREADY_EXISTS);
        }
        userBlockRepository.save(UserBlock.create(requesterId, targetId));
    }

    @Transactional(readOnly = true)
    public boolean isBlockedBy(Long blockerId, Long blockedId) {
        return userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Transactional(readOnly = true)
    public void assertNotBlockedByAny(Set<Long> participantIds, Long senderId) {
        if (userBlockRepository.existsByBlockerIdInAndBlockedId(participantIds, senderId)) {
            throw new CustomException(ErrorCode.USER_BLOCKED_BY_TARGET);
        }
    }

    @Transactional(readOnly = true)
    public List<ResponseBlockedUserDto> getMyBlockList(Long requesterId) {
        List<UserBlock> blocks = userBlockRepository.findAllByBlockerId(requesterId);
        List<Long> blockedIds = blocks.stream().map(UserBlock::getBlockedId).toList();
        Map<Long, User> userMap = userRepository.findAllById(blockedIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return blocks.stream()
                .map(block -> ResponseBlockedUserDto.of(block, userMap.get(block.getBlockedId())))
                .toList();
    }

    @Transactional
    public void unblockUser(Long requesterId, Long targetId) {
        if (requesterId.equals(targetId)) {
            throw new CustomException(ErrorCode.USER_BLOCK_CANNOT_BLOCK_SELF);
        }
        if (!userBlockRepository.existsByBlockerIdAndBlockedId(requesterId, targetId)) {
            throw new CustomException(ErrorCode.USER_BLOCK_NOT_FOUND);
        }
        userBlockRepository.deleteByBlockerIdAndBlockedId(requesterId, targetId);
    }
}
