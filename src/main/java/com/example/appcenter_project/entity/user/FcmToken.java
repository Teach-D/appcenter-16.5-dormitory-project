package com.example.appcenter_project.entity.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String token;

    @Builder
    public FcmToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
