package com.example.appcenter_project.entity.roommate;

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
public class RoommateChattingRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅이 생성된 게시글
    @ManyToOne
    @JoinColumn(name = "roommate_board_id", nullable = false)
    private RoommateBoard roommateBoard;

    // 게시글 작성자 (고정, 모든 채팅방에 동일)
    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    // 채팅 요청자
    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne
    @JoinColumn(name = "guest_roommate_checklist_id", nullable = false)
    private RoommateCheckList guestChecklist;

    @ManyToOne
    @JoinColumn(name = "host_roommate_checklist_id", nullable = false)
    private RoommateCheckList hostChecklist;

    @OneToMany(mappedBy = "roommateChattingRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoommateChattingChat> chattingChatList = new ArrayList<>();

    @Builder
    public RoommateChattingRoom(RoommateBoard roommateBoard, User guest, User host,
                                RoommateCheckList guestChecklist, RoommateCheckList hostChecklist) {
        this.roommateBoard = roommateBoard;
        this.guest = guest;
        this.host = host;
        this.guestChecklist = guestChecklist;
        this.hostChecklist = hostChecklist;
    }
}
