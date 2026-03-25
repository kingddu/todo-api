package com.springboot.todoapi.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class TokenVerifyRequest {
    @NotEmpty
    private String token;
}
