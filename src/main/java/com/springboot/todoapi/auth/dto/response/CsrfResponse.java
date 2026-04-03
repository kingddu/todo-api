package com.springboot.todoapi.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CsrfResponse {

    private String headerName;
    private String parameterName;
    private String token;
}