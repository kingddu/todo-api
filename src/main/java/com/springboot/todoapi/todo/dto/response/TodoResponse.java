package com.springboot.todoapi.todo.dto.response;

import com.springboot.todoapi.todo.entity.Todo;
import com.springboot.todoapi.todo.entity.TodoType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TodoResponse {

    private Long id;

    private String title;
    private String content;
    private String category;

    private TodoType type;

    // 🔥 날짜들
    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime dueDate;

    // 🔥 상태
    private boolean completed;
    private Long completedBy;
    private LocalDateTime completedAt;

    private boolean carryOver;

    public static TodoResponse from(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .content(todo.getContent())
                .category(todo.getCategory())
                .type(todo.getType())
                .startDate(todo.getStartDate())
                .endDate(todo.getEndDate())
                .completed(todo.isCompleted())
                .completedBy(todo.getCompletedBy())
                .completedAt(todo.getCompletedAt())
                .carryOver(todo.isCarryOver())
                .build();
    }
}