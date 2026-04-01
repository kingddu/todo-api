package com.springboot.todoapi.group.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class GroupInviteRequest {

    @NotEmpty
    @Size(max = 20)
    private List<@Email @NotBlank String> inviteEmails;
}