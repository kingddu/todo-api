package com.springboot.todoapi.group.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupMemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    // 공식 그룹명과 다른 개인 alias
    @Column(name = "alias_name", length = 100)
    private String aliasName;

    public static GroupMember createLeader(Long groupId, Long userId) {
        return GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role(GroupMemberRole.LEADER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public static GroupMember createMember(Long groupId, Long userId) {
        return GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public void changeAliasName(String aliasName) {
        this.aliasName = (aliasName == null || aliasName.trim().isBlank())
                ? null
                : aliasName.trim();
    }

    public void leave() {
        this.status = GroupMemberStatus.LEFT;
    }

    public void kick() {
        this.status = GroupMemberStatus.KICKED;
    }

    public void promoteToLeader() {
        this.role = GroupMemberRole.LEADER;
    }

    public void demoteToMember() {
        this.role = GroupMemberRole.MEMBER;
    }

    public boolean isLeader() {
        return this.role == GroupMemberRole.LEADER;
    }

    public boolean isActive() {
        return this.status == GroupMemberStatus.ACTIVE;
    }
}