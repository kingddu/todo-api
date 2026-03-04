package com.springboot.scheduleapi.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

}
