package com.springboot.todoapi.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "개인 그룹 별칭 변경 요청")
public class GroupAliasUpdateRequest {

    @Schema(description = "해당 그룹을 개인적으로 부를 별칭", example = "회사팀")
    @Size(max = 100)
    private String aliasName;
}