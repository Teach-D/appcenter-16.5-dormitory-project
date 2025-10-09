package com.example.appcenter_project.entity.tip;

import com.example.appcenter_project.dto.request.tip.RequestTipDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.like.TipLike;
import com.example.appcenter_project.entity.user.User;
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

    public Integer plusLike() {
        return this.tipLike += 1;
    }

    public void update(RequestTipDto requestTipDto) {
        this.title = requestTipDto.getTitle();
        this.content = requestTipDto.getContent();
    }

    public Integer minusLike() {
        return this.tipLike -= 1;
    }

    public void plusTipCommentCount() {
        this.tipCommentCount += 1;
    }

    public void minusTipCommentCount() {
        this.tipCommentCount -= 1;
    }
}
