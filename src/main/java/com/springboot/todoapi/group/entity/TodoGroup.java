package com.springboot.todoapi.group.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "todo_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TodoGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    // 최초 생성자
    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TodoGroupStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 25)
    private String description;

    public static TodoGroup create(String groupName, Long userId, String description) {
        return TodoGroup.builder()
                .groupName(groupName)
                .creatorUserId(userId)
                .status(TodoGroupStatus.INVITING)
                .createdAt(LocalDateTime.now())
                .description(description)
                .build();
    }

    public void changeGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void activate() {
        this.status = TodoGroupStatus.ACTIVE;
    }

    public void inactivate() {
        this.status = TodoGroupStatus.INACTIVE;
    }

    public void markInviting() {
        this.status = TodoGroupStatus.INVITING;
    }

    public void disband() {
        this.status = TodoGroupStatus.DISBANDED;
    }
}