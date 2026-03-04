package com.springboot.scheduleapi.auth.controller;


import com.springboot.scheduleapi.auth.dto.LoginRequest;
import com.springboot.scheduleapi.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login (
            @Valid @RequestBody LoginRequest request
    ) {
        authService.login(request);
        return ResponseEntity.ok().build();
    }
}
