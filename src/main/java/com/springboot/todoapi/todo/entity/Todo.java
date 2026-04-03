package com.springboot.todoapi.todo.entity;

import com.springboot.todoapi.group.entity.TodoGroup;
import com.springboot.todoapi.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private TodoGroup group;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String content;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoType type;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean completed = false;

    private Long completedBy;
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private boolean carryOver = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Todo(
            String title,
            String content,
            String category,
            TodoType type,
            LocalDate startDate,
            LocalDate endDate,
            boolean carryOver,
            User user,
            TodoGroup group
    ) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.carryOver = carryOver;
        this.user = user;
        this.group = group;
        this.completed = false;
    }

    public void complete(Long userId) {
        this.completed = true;
        this.completedBy = userId;
        this.completedAt = LocalDateTime.now();
    }

    public void uncomplete() {
        this.completed = false;
        this.completedBy = null;
        this.completedAt = null;
    }

    public void changeCarryOver(boolean carryOver) {
        this.carryOver = carryOver;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeCategory(String category) {
        this.category = category;
    }

    public void changeType(TodoType type) {
        this.type = type;
    }

    public void changeStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void changeEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }


}