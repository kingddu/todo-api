package com.springboot.todoapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangeEmailRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,63}$",
        message = "올바른 이메일 형식이 아니에요."
    )
    private String email;
}