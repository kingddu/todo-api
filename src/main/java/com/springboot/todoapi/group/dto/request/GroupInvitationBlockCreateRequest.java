package com.springboot.todoapi.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "그룹 초대 차단 요청")
public class GroupInvitationBlockCreateRequest {

    @Schema(description = "차단할 사용자의 이메일", example = "test@example.com")
    @NotBlank
    @Email
    private String email;
}
