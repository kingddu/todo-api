package com.springboot.todoapi.group.dto.response;

import com.springboot.todoapi.group.entity.TodoGroup;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupResponse {

    private Long id;
    private String name;
    private Long ownerUserId;

    public static GroupResponse from(TodoGroup group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .ownerUserId(group.getOwnerUserId())
                .build();
    }
}