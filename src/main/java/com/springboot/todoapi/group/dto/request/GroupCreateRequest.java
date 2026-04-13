package com.springboot.todoapi.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "그룹 생성 요청")
public class GroupCreateRequest {

    @Schema(description = "정식 그룹명", example = "가족")
    @NotBlank
    @Size(max = 20, message = "그룹 이름은 20자 이하여야 합니다.")
    private String groupName;

    @Schema(description = "그룹 소개 (25자 이내, 선택)", example = "정가네 todo")
    @Size(max = 25)
    private String description;

    @Schema(description = "초대할 이메일 목록", example = "[\"a@test.com\", \"b@test.com\"]")
    @NotEmpty
    @Size(max = 20)
    private List<@Email @NotBlank String> inviteEmails;
}