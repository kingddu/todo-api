package com.springboot.todoapi.group.dto.response;

import com.springboot.todoapi.group.entity.TodoGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "그룹 생성 응답")
public class GroupResponse {

    @Schema(description = "그룹 ID", example = "1")
    private Long id;

    @Schema(description = "정식 그룹명", example = "가족")
    private String groupName;

    @Schema(description = "그룹 최초 생성자 ID", example = "1")
    private Long creatorUserId;

    @Schema(description = "초대된 이메일 목록")
    private List<String> invitedEmails;

    public static GroupResponse from(TodoGroup group) {
        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .creatorUserId(group.getCreatorUserId())
                .build();
    }
}