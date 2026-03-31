package com.springboot.todoapi.group.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_invitation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GroupInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupInvitationStatus status;

    @Column(name = "invited_by_user_id", nullable = false)
    private Long invitedByUserId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static GroupInvitation create(Long groupId, String email, Long invitedByUserId, LocalDateTime expiresAt) {
        return GroupInvitation.builder()
                .groupId(groupId)
                .email(email)
                .status(GroupInvitationStatus.PENDING)
                .invitedByUserId(invitedByUserId)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
