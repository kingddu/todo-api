package com.springboot.todoapi.auth.controller;


import com.springboot.todoapi.auth.dto.request.LoginRequest;
import com.springboot.todoapi.auth.dto.request.SignupRequest;
import com.springboot.todoapi.auth.dto.request.UpdateProfileRequest;
import com.springboot.todoapi.auth.dto.response.AuthMessageResponse;
import com.springboot.todoapi.auth.dto.response.CsrfResponse;
import com.springboot.todoapi.auth.dto.response.LoginResponse;
import com.springboot.todoapi.auth.dto.response.MeResponse;
import com.springboot.todoapi.auth.security.CustomUserPrincipal;
import com.springboot.todoapi.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "회원가입, 로그인, 로그아웃, 현재 사용자 조회 등 인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "CSRF 토큰 조회")
    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(
                new CsrfResponse(csrfToken.getHeaderName(), csrfToken.getParameterName(), csrfToken.getToken())
        );
    }

    @Operation(
            summary = "회원가입",
            description = "이름, 이메일, 비밀번호로 새 계정을 생성합니다. 이메일은 중복될 수 없습니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<AuthMessageResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthMessageResponse("SIGNUP_SUCCESS"));
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하고, 성공 시 서버 세션을 생성합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 기본 정보를 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(authService.me(principal));
    }

    @Operation(
            summary = "내 프로필 수정",
            description = "현재 로그인한 사용자의 이름과 이메일을 변경합니다."
    )
    @PatchMapping("/me")
    public ResponseEntity<MeResponse> updateProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(authService.updateProfile(principal.getId(), request.getName(), request.getEmail()));
    }
}