package com.example.appcenter_project.entity.like;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class TipLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 좋아요 누른 게시판
    @ManyToOne
    @JoinColumn(name = "tip_id")
    private Tip tip;

    @Builder
    public TipLike(User user, Tip tip) {
        this.user = user;
        this.tip = tip;
    }
}
