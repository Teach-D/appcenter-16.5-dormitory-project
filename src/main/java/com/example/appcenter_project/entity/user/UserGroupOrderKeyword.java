package com.example.appcenter_project.entity.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class UserGroupOrderKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(unique = true)
    private String keyword;

    @Builder
    public UserGroupOrderKeyword(User user, String keyword) {
        this.user = user;
        this.keyword = keyword;
    }

    public void updateKeyword(String afterKeyword) {
        this.keyword = afterKeyword;
    }
}
