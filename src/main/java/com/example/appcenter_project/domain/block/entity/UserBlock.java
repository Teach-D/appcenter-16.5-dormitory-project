package com.example.appcenter_project.domain.block.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_block",
        uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"}))
public class UserBlock extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_id", nullable = false)
    private Long blockerId;

    @Column(name = "blocked_id", nullable = false)
    private Long blockedId;

    public static UserBlock create(Long blockerId, Long blockedId) {
        UserBlock block = new UserBlock();
        block.blockerId = blockerId;
        block.blockedId = blockedId;
        return block;
    }
}
