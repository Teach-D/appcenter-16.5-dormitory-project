package com.example.appcenter_project.domain.groupOrder.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class GroupOrderLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 좋아요 누른 게시판
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "group_order_id")
    private GroupOrder groupOrder;

    @Builder
    public GroupOrderLike(User user, GroupOrder groupOrder) {
        this.user = user;
        this.groupOrder = groupOrder;
    }
}
