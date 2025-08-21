package com.example.appcenter_project.entity.report;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String title;
    private String content;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Report(String category, String title, String content, User user) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.user = user;
    }

    public void updateUser(User user) {
        this.user = user;
    }
}
