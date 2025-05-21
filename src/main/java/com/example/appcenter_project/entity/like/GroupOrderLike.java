package com.example.appcenter_project.entity.like;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.like.BoardType;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 좋아요 누른 게시판
    @ManyToOne
    @JoinColumn(name = "group_order_id")
    private GroupOrder groupOrder;

    @Builder
    public GroupOrderLike(User user, GroupOrder groupOrder) {
        this.user = user;
        this.groupOrder = groupOrder;
    }
}
