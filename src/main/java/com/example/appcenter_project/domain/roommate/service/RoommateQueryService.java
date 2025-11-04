package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoommateQueryService {

    private final RoommateBoardLikeRepository roommateBoardLikeRepository;

    public List<ResponseRoommatePostDto> findLikedByUser(Long userId) {
        return roommateBoardLikeRepository.findByUserIdWithRoommateBoardAndRoommateCheckListAndUser(userId)
                .stream().map(roommateBoardLike -> ResponseRoommatePostDto.entityToDto(roommateBoardLike.getRoommateBoard(), roommateBoardLike.getRoommateBoard().isMatched(), null)).toList();
    }

}