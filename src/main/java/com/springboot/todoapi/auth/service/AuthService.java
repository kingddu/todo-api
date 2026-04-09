package com.springboot.todoapi.auth.service;


import com.springboot.todoapi.auth.dto.request.LoginRequest;
import com.springboot.todoapi.auth.dto.request.SignupRequest;
import com.springboot.todoapi.auth.dto.response.LoginResponse;
import com.springboot.todoapi.auth.dto.response.MeResponse;
import com.springboot.todoapi.auth.security.CustomUserPrincipal;
import com.springboot.todoapi.user.entity.User;
import com.springboot.todoapi.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import net.coobird.thumbnailator.Thumbnails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String BAD_CREDENTIALS_MESSAGE = "로그인 정보가 올바르지 않습니다.";

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public void signup(SignupRequest request) {
        String email = normalizeEmail(request.getEmail());
        String name = request.getName().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }

        emailVerificationService.ensureSignupVerified(email);

        User user = User.create(email, passwordEncoder.encode(request.getPassword()), name);
        userRepository.save(user);

        emailVerificationService.clearSignupVerification(email);
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = normalizeEmail(request.getEmail());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, request.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS_MESSAGE);
        }

        if (httpRequest.getSession(false) != null) {
            httpRequest.changeSessionId();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();   //현재 로그인 한 사용자 정보를 담는 보안 컨테이너
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,   // 로그인 정보를 찾을 때의 표준 키
                context // 세션에 저장할 실제 값
        );

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

        return new LoginResponse(
                principal.getId(),
                principal.getEmail(),
                principal.getName(),
                principal.getRole().name()
        );
    }

    @Transactional
    public MeResponse updateProfile(Long userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        user.updateProfile(name.trim(), user.getEmail());

        return toMeResponse(user);
    }

    @Transactional
    public MeResponse changeEmail(Long userId, String email) {
        String normalizedEmail = normalizeEmail(email);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getEmail().equals(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 사용 중인 이메일과 같습니다.");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        emailVerificationService.ensureChangeEmailVerified(userId, normalizedEmail);

        user.updateProfile(user.getName(), normalizedEmail);

        emailVerificationService.clearChangeEmailVerification(userId, normalizedEmail);

        return toMeResponse(user);
    }

    public void verifyPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다.");
        }
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다.");
        }

        user.changePassword(passwordEncoder.encode(newPassword));
    }

    public MeResponse me(CustomUserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        return toMeResponse(user);
    }

    @Transactional
    public MeResponse uploadProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        String filename = "profile_" + userId + ".jpg";
        Path dir = Paths.get(uploadDir, "profiles");

        try {
            Files.createDirectories(dir);
            Thumbnails.of(file.getInputStream())
                    .size(200, 200)
                    .keepAspectRatio(true)
                    .outputFormat("jpg")
                    .outputQuality(0.80)
                    .toFile(dir.resolve(filename).toFile());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 저장에 실패했어요.");
        }

        user.updateProfileImage("/uploads/profiles/" + filename);
        return toMeResponse(user);
    }

    private MeResponse toMeResponse(User user) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getProfileImageUrl()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}