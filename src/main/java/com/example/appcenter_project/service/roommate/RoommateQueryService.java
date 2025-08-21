package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.entity.like.RoommateBoardLike;
import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.repository.like.RoommateBoardLikeRepository;
import com.example.appcenter_project.repository.roommate.RoommateCheckListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoommateQueryService {

    private final RoommateBoardLikeRepository roommateBoardLikeRepository;
    private final RoommateCheckListRepository roommateCheckListRepository;

    public List<ResponseRoommatePostDto> findGroupOrderDtosWithImages(Long userId) {
        return roommateBoardLikeRepository.findByUserIdWithRoommateBoadAndRoommateCheckListAndUser(userId)
                .stream().map(roommateBoardLike -> ResponseRoommatePostDto.entityToDto(roommateBoardLike.getRoommateBoard(), false, null)).toList();
    }

}
