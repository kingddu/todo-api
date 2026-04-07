package com.springboot.todoapi.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeResponse {

    private Long userId;
    private String email;
    private String name;
    private String role;
    private String status;
    private String profileImageUrl;
}