package com.example.appcenter_project.entity.groupOrder;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.user.User;
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

    @Column(nullable = false, length = 100)
    private String reply; // 댓글 내용

    private boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "group_order_id")
    private GroupOrder groupOrder;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
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

    public void setParentGroupOrderCommentNull() {
        this.parentGroupOrderComment = null;
    }

    public void addChildGroupOrderComments(GroupOrderComment groupOrderComment) {
        this.childGroupOrderComments.add(groupOrderComment);
    }

    public void updateIsDeleted() {
        this.isDeleted = true;
    }
}
