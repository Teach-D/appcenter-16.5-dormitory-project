package com.example.appcenter_project.entity.groupOrder;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class GroupOrderChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GroupOrder의 title과 같은 문자열
    private String title;

    @OneToMany(mappedBy = "groupOrderChatRoom")
    private List<UserGroupOrderChatRoom> userGroupOrderChatRoomList = new ArrayList<>();

    @OneToMany(mappedBy = "groupOrderChatRoom")
    private List<GroupOrderChat> groupOrderChatList = new ArrayList<>();

    @OneToOne(mappedBy = "groupOrderChatRoom")
    private GroupOrder groupOrder;

    public GroupOrderChatRoom(String title) {
        this.title = title;
    }

    public void updateGroupOrder(GroupOrder groupOrder) {
        this.groupOrder = groupOrder;
    }
}
