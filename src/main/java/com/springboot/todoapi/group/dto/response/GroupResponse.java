package com.springboot.todoapi.group.dto.response;

import com.springboot.todoapi.group.entity.TodoGroup;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GroupResponse {

    private Long id;
    private String groupName;
    private Long ownerUserId;
    private List<String> invitedEmails;

    public static GroupResponse from(TodoGroup group) {
        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .ownerUserId(group.getOwnerUserId())
                .build();
    }
}