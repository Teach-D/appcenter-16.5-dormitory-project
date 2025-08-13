package com.example.appcenter_project.repository.roommate;

import com.example.appcenter_project.entity.roommate.RoommateBoard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoommateBoardRepository extends JpaRepository<RoommateBoard, Long> {
    List<RoommateBoard> findAllByOrderByCreatedDateDesc();
    Optional<RoommateBoard> findByUserId(Long userId);

    // 가장 최근 10개의 게시글 조회
    List<RoommateBoard> findTop10ByOrderByCreatedDateDesc();

    List<RoommateBoard> findAllByOrderByIdDesc(Pageable pageable);

    List<RoommateBoard> findByIdLessThanOrderByIdDesc(Long lastId, Pageable pageable);
    // 자기 자신 제외 전체 가져오기 (유사도 계산용)
    List<RoommateBoard> findAllByIdNot(Long boardId);

}