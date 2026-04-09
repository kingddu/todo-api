package com.springboot.todoapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 50, message = "이름은 50자 이내로 입력해주세요.")
    @Pattern(
        regexp = "^[^'\"`,!@#$%^&*()=+\\[\\]{}<>?/\\\\|~]+$",
        message = "이름에는 따옴표, 쉼표 등 특수문자를 사용할 수 없어요."
    )
    private String name;
}
