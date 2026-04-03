package com.springboot.todoapi.todo.dto.request;

import com.springboot.todoapi.todo.entity.TodoType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TodoCreateRequest {

    @NotBlank
    private String title;

    private String content;

    private String category;

    @NotNull
    private TodoType type;

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean carryOver;

    // null이면 개인 Todo, 값이 있으면 해당 그룹의 Todo
    private Long groupId;
}