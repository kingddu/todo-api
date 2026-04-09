package com.springboot.todoapi.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "그룹 소개 변경 요청")
public class GroupDescriptionUpdateRequest {

    @Schema(description = "새 그룹 소개 (빈 값이면 소개 삭제)", example = "우리 가족 Todo")
    @Size(max = 25)
    private String description;
}