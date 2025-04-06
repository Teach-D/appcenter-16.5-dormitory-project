package com.example.appcenter_project.entity.groupOrder;

import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Entity
public class GroupOrderComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reply; // 댓글 내용

    @ManyToOne
    @JoinColumn(name = "group_order_board_id")
    private GroupOrder groupOrder;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private GroupOrderComment parentGroupOrderComment;

    @OneToMany(mappedBy = "parentGroupOrderComment", orphanRemoval = true)
    private List<GroupOrderComment> childGroupOrderComments = new ArrayList<>();
}
