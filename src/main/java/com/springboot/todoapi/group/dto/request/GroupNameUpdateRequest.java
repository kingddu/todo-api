package com.springboot.todoapi.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "공식 그룹명 변경 요청")
public class GroupNameUpdateRequest {

    @Schema(description = "새 공식 그룹명", example = "우리집")
    @NotBlank
    @Size(max = 100)
    private String groupName;
}
