package com.springboot.todoapi.todo.dto.request;

import com.springboot.todoapi.todo.entity.TodoType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class TodoCreateRequest {

    @NotBlank
    private String title;

    private String content;

    private String category;

    @NotNull
    private TodoType type;

    // 🔥 날짜들 (yyyyMMdd 형식)
    private LocalDate startDate;
    private LocalDate endDate;



    private Boolean carryOver;
}