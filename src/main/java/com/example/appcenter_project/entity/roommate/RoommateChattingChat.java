package com.example.appcenter_project.entity.roommate;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class RoommateChattingChat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "roommate_chatting_room_id", nullable = false)
    private RoommateChattingRoom roommateChattingRoom;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private boolean readByReceiver = false; // 읽음 여부

    @Builder
    public RoommateChattingChat(RoommateChattingRoom roommateChattingRoom, User member, String content, boolean readByReceiver) {
        this.roommateChattingRoom = roommateChattingRoom;
        this.member = member;
        this.content = content;
        this.readByReceiver = readByReceiver;
    }


    public void markAsRead() {
        this.readByReceiver = true;
    }
}

