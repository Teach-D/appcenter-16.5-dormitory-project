package com.example.appcenter_project.entity.roommate;

import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.roommate.MatchingStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "roommate_matching")
public class RoommateMatching {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MatchingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Builder
    public RoommateMatching(MatchingStatus check, User sender, User receiver) {
        this.status = check;
        this.sender = sender;
        this.receiver = receiver;
    }

    public void complete() {
        this.status = MatchingStatus.COMPLETED;
    }

    public void fail() {
        this.status = MatchingStatus.FAILED;
    }
}

