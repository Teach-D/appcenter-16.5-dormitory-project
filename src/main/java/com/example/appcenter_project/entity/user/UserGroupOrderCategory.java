package com.example.appcenter_project.entity.user;

import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class UserGroupOrderCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private GroupOrderType category;

    @Builder
    public UserGroupOrderCategory(User user, GroupOrderType category) {
        this.user = user;
        this.category = category;
    }

    public void updateKeyword(GroupOrderType category) {
        this.category = category;
    }
}
