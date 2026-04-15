package com.springboot.todoapi.todo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class TodoMemoRequest {

    @NotBlank(message = "메모 내용을 입력해주세요.")
    @Size(max = 500, message = "메모는 500자 이하여야 합니다.")
    private String content;
}
