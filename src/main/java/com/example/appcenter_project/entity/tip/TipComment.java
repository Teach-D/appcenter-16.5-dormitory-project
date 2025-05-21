package com.example.appcenter_project.entity.tip;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
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
public class TipComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String reply; // 댓글 내용

    @ManyToOne
    @JoinColumn(name = "tip_id")
    private Tip tip;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private TipComment parentTipComment;

    @OneToMany(mappedBy = "parentTipComment", orphanRemoval = true)
    private List<TipComment> childTipComments = new ArrayList<>();

    @Builder
    public TipComment(String reply, Tip tip, User user, TipComment parentTipComment, List<TipComment> childTipComments) {
        this.reply = reply;
        this.tip = tip;
        this.user = user;
        this.parentTipComment = parentTipComment;
        this.childTipComments = childTipComments;
    }

    public void setParentTipCommentNull() {
        this.parentTipComment = null;
    }

    public void addChildTipComments(TipComment tipComment) {
        this.childTipComments.add(tipComment);
    }
}
