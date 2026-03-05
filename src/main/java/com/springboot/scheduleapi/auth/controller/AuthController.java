package com.springboot.scheduleapi.auth.controller;


import com.springboot.scheduleapi.auth.dto.LoginRequest;
import com.springboot.scheduleapi.auth.dto.LoginResponse;
import com.springboot.scheduleapi.auth.dto.TokenVerifyRequest;
import com.springboot.scheduleapi.auth.jwt.JwtProvider;
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
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify (@Valid @RequestBody TokenVerifyRequest request) {
        boolean valid = jwtProvider.validateToken(request.getToken());

        if(!valid) {
            return ResponseEntity.status(401).body("INVALID_TOKEN");
        }
        return ResponseEntity.ok("VALID_TOKEN");
    }


}
