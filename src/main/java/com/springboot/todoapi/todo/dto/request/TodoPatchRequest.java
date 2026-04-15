package com.springboot.todoapi.todo.dto.request;

import com.springboot.todoapi.todo.entity.TodoType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TodoPatchRequest {
    @Schema(description = "할 일 제목")
    private String title;

    @Schema(description = "상세 내용", nullable = true)
    private String content;

    @Schema(description = "Todo 타입", allowableValues = {"DATE_ONLY", "RANGE", "DEADLINE"})
    private TodoType type;

    @Schema(description = "시작일", type = "string", format = "date")
    private LocalDate startDate;

    @Schema(description = "종료일", type = "string", format = "date")
    private LocalDate endDate;

    @Schema(description = "다음날 이월 여부", nullable = false)
    private Boolean carryOver;
}
