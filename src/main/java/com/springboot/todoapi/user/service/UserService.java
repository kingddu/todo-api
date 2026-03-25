package com.springboot.todoapi.user.service;

import com.springboot.todoapi.user.dto.SignupRequest;
import com.springboot.todoapi.user.entity.User;
import com.springboot.todoapi.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getEmail(),
                encodedPassword
        );

        userRepository.save(user);
    }
}
