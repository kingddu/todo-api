package com.springboot.scheduleapi.user.controller;

import com.springboot.scheduleapi.user.dto.SignupRequest;
import com.springboot.scheduleapi.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup (@Valid @RequestBody SignupRequest request) {

        userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
}
