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

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static TodoGroup create(String groupName, Long userId) {
        return TodoGroup.builder()
                .groupName(groupName)
                .ownerUserId(userId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}