package com.springboot.todoapi.group.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GroupLeaderTransferRequest {

    @NotNull
    private Long targetUserId;

}
