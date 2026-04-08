package com.springboot.todoapi.todo.dto.response;

import com.springboot.todoapi.group.entity.TodoGroup;
import com.springboot.todoapi.group.entity.TodoGroupStatus;
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

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean completed;
    private Long completedBy;
    private LocalDateTime completedAt;

    private boolean carryOver;

    // null이면 개인 Todo
    private Long groupId;
    private String groupName;
    private boolean groupDisbanded;

    private int editCount;

    public static TodoResponse from(Todo todo) {
        return from(todo, 0);
    }

    public static TodoResponse from(Todo todo, int editCount) {
        TodoGroup group = todo.getGroup();

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
                .groupId(group != null ? group.getId() : null)
                .groupName(group != null ? group.getGroupName() : null)
                .groupDisbanded(group != null && group.getStatus() == TodoGroupStatus.DISBANDED)
                .editCount(editCount)
                .build();
    }
}