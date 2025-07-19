package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupOrderRepository extends JpaRepository<GroupOrder, Long>, JpaSpecificationExecutor<GroupOrder> {
    Optional<GroupOrder> findByGroupOrderChatRoom_id(Long id);
    boolean existsByTitle(String title);
    Optional<GroupOrder> findByIdAndUserId(Long id, Long userId);


    // 방법 1: MultipleBagFetchException 해결 - 단계별 조회
    // 1-1. 기본 정보 + 단일 컬렉션(이미지) + 단일 객체들 조회
    @Query("SELECT DISTINCT go FROM GroupOrder go " +
            "LEFT JOIN FETCH go.imageList " +
            "LEFT JOIN FETCH go.user " +
            "LEFT JOIN FETCH go.groupOrderChatRoom " +
            "ORDER BY go.id DESC")
    List<GroupOrder> findAllWithBasicAssociations();

    // 방법 2: 페이징을 고려한 조회 (대용량 데이터 처리)
    @Query("SELECT DISTINCT go FROM GroupOrder go " +
            "LEFT JOIN FETCH go.imageList " +
            "LEFT JOIN FETCH go.user " +
            "ORDER BY go.id DESC")
    Page<GroupOrder> findAllWithImagesAndUser(Pageable pageable);

    // 1-2. 좋아요 정보만 별도 조회
    @Query("SELECT DISTINCT go FROM GroupOrder go " +
            "LEFT JOIN FETCH go.groupOrderLikeList gol " +
            "LEFT JOIN FETCH gol.user " +
            "WHERE go.id IN :ids")
    List<GroupOrder> findAllWithLikes(@Param("ids") List<Long> ids);

    // 1-3. 댓글 정보만 별도 조회 (중첩 댓글 포함)
    @Query("SELECT DISTINCT go FROM GroupOrder go " +
            "LEFT JOIN FETCH go.groupOrderCommentList goc " +
            "LEFT JOIN FETCH goc.user " +
            "LEFT JOIN FETCH goc.parentGroupOrderComment " +
            "WHERE go.id IN :ids")
    List<GroupOrder> findAllWithComments(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT go FROM GroupOrder go " +
            "LEFT JOIN FETCH go.imageList " +
            "LEFT JOIN FETCH go.user " +
            "LEFT JOIN FETCH go.groupOrderChatRoom " +
            "ORDER BY go.id DESC")
    List<GroupOrder> findAllWithBasicInfo();

}

