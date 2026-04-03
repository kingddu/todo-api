package com.springboot.todoapi.group.dto.response;

import com.springboot.todoapi.group.entity.GroupMember;
import com.springboot.todoapi.group.entity.TodoGroup;

public record MyGroupSummaryResponse(
        Long groupId,
        String groupName,
        String aliasName,
        String myRole,
        String status
) {
    public static MyGroupSummaryResponse of(TodoGroup group, GroupMember member) {
        return new MyGroupSummaryResponse(
                group.getId(),
                group.getGroupName(),
                member.getAliasName(),
                member.getRole().name(),
                group.getStatus().name()
        );
    }
}
