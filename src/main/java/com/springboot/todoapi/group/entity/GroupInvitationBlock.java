package com.springboot.todoapi.group.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_invitation_block",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_invitation_block_user",
                        columnNames = {"blocker_user_id", "blocked_user_id"}
                ),
                @UniqueConstraint(
                        name = "uk_group_invitation_block_email",
                        columnNames = {"blocker_user_id", "blocked_email"}
                )
        },
        indexes = {
                @Index(name = "idx_group_invitation_block_blocker", columnList = "blocker_user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GroupInvitationBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_user_id", nullable = false)
    private Long blockerUserId;

    @Column(name = "blocked_user_id", nullable = false)
    private Long blockedUserId;

    @Column(name = "blocked_email", nullable = false, length = 100)
    private String blockedEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static GroupInvitationBlock create(Long blockerUserId, Long blockedUserId, String blockedEmail) {
        return GroupInvitationBlock.builder()
                .blockerUserId(blockerUserId)
                .blockedUserId(blockedUserId)
                .blockedEmail(blockedEmail)
                .createdAt(LocalDateTime.now())
                .build();
    }
}