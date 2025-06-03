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

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, length = 100)
    private String content;
    private Integer tipLike;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany
    private List<Image> imageList = new ArrayList<>();

    @OneToMany(mappedBy = "tip", orphanRemoval = true)
    private List<TipComment> tipCommentList = new ArrayList<>();

    @OneToMany(mappedBy = "tip", orphanRemoval = true)
    private List<TipLike> tipLikeList = new ArrayList<>();

    @Builder
    private Tip(String title, String content, User user, Image image) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.tipLike = 0;
        this.imageList.add(image);
    }

    public Integer plusLike() {
        return this.tipLike += 1;
    }

    public void update(RequestTipDto requestTipDto) {
        this.title = requestTipDto.getTitle();
        this.content = requestTipDto.getContent();
    }
}
