package com.springboot.todoapi.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class GroupCreateRequest {

    @NotBlank
    private String name;
}