package com.example.appcenter_project.domain.tip.entity;

import com.example.appcenter_project.domain.tip.dto.request.RequestTipDto;
import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Tip extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;
    private Integer tipLike;
    private Integer tipCommentCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "tip", orphanRemoval = true)
    private List<TipComment> tipCommentList = new ArrayList<>();

    @OneToMany(mappedBy = "tip", orphanRemoval = true)
    private List<TipLike> tipLikeList = new ArrayList<>();

    @Builder
    private Tip(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.tipLike = 0;
        this.tipCommentCount = 0;
    }

    public static Tip createTip(String title, String content, User user) {
        return Tip.builder()
                .title(title)
                .content(content)
                .user(user)
                .build();
    }

    public Integer increaseLike() {
        return this.tipLike += 1;
    }

    public Integer decreaseLike() {
        if (this.tipLike > 0) {
            this.tipLike -= 1;
        }

        return this.tipLike;
    }

    public void update(RequestTipDto requestTipDto) {
        this.title = requestTipDto.getTitle();
        this.content = requestTipDto.getContent();
    }

    public void plusTipCommentCount() {
        this.tipCommentCount += 1;
    }

    public void minusTipCommentCount() {
        this.tipCommentCount -= 1;
    }

    public boolean isLikedBy(User user) {
        return this.tipLikeList.stream()
                .anyMatch(tipLike -> tipLike.getUser().equals(user));
    }
}