package com.example.appcenter_project.domain.groupOrder.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.groupOrder.dto.request.RequestGroupOrderCommentDto;
import com.example.appcenter_project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class GroupOrderComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reply; // 댓글 내용

    private boolean isDeleted = false;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "group_order_id")
    private GroupOrder groupOrder;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GroupOrderComment parentGroupOrderComment;

    @OneToMany(mappedBy = "parentGroupOrderComment", orphanRemoval = true)
    private List<GroupOrderComment> childGroupOrderComments = new ArrayList<>();

    @Builder
    public GroupOrderComment(String reply, GroupOrder groupOrder, User user, GroupOrderComment parentGroupOrderComment, List<GroupOrderComment> childGroupOrderComments) {
        this.reply = reply;
        this.groupOrder = groupOrder;
        this.user = user;
        this.parentGroupOrderComment = parentGroupOrderComment;
        this.childGroupOrderComments = childGroupOrderComments != null ? childGroupOrderComments : new ArrayList<>();
    }

    public void updateIsDeleted() {
        this.isDeleted = true;
    }
}