package com.springboot.scheduleapi.user.controller;

import com.springboot.scheduleapi.user.dto.SignupRequest;
import com.springboot.scheduleapi.user.repository.UserRepository;
import com.springboot.scheduleapi.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup (@Valid @RequestBody SignupRequest request) {

        userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        return ResponseEntity.ok("OK");

    }
}
