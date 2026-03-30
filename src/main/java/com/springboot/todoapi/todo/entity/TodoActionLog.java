package com.springboot.todoapi.todo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TodoActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @Column(nullable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TodoActionType actionType;

    @Column(length = 500)
    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TodoActionLog(
            Todo todo,
            Long actorUserId,
            TodoActionType actionType,
            String description
    ) {
        this.todo = todo;
        this.actorUserId = actorUserId;
        this.actionType = actionType;
        this.description = description;
    }

    public static TodoActionLog created(Todo todo, Long actorUserId) {
        return TodoActionLog.builder()
                .todo(todo)
                .actorUserId(actorUserId)
                .actionType(TodoActionType.CREATED)
                .description("Todo 생성")
                .build();
    }

    public static TodoActionLog updated(Todo todo, Long actorUserId, String description) {
        return TodoActionLog.builder()
                .todo(todo)
                .actorUserId(actorUserId)
                .actionType(TodoActionType.UPDATED)
                .description(description)
                .build();
    }
}