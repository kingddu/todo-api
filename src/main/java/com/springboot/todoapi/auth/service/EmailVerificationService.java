package com.springboot.todoapi.auth.service;

import com.springboot.todoapi.auth.verification.EmailVerificationPurpose;
import com.springboot.todoapi.user.entity.User;
import com.springboot.todoapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.mail.auth-code-expiration-seconds:300}")
    private long codeExpirationSeconds;

    @Value("${app.mail.auth-success-expiration-seconds:1800}")
    private long successExpirationSeconds;

    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Transactional
    public void sendSignupCode(String rawEmail) {
        String email = normalizeEmail(rawEmail);

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }

        String code = generateCode();

        //Redis에 인증코드 저장
        stringRedisTemplate.opsForValue().set(
                codeKey(EmailVerificationPurpose.SIGNUP, null, email),
                code,
                Duration.ofSeconds(codeExpirationSeconds)
        );

        //과거 인증완료 상태를 지움
        //한 번 인증하면 쭉 가는 것이 아니라, 다시 검증하도록
        stringRedisTemplate.delete(successKey(EmailVerificationPurpose.SIGNUP, null, email));

        sendVerificationEmail(email, code, "회원가입");
    }

    @Transactional
    public void verifySignupCode(String rawEmail, String inputCode) {
        String email = normalizeEmail(rawEmail);

        String savedCode = stringRedisTemplate.opsForValue()
                .get(codeKey(EmailVerificationPurpose.SIGNUP, null, email));

        if (savedCode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증코드가 만료되었거나 존재하지 않습니다.");
        }

        if (!savedCode.equals(inputCode.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증코드가 올바르지 않습니다.");
        }

        stringRedisTemplate.delete(codeKey(EmailVerificationPurpose.SIGNUP, null, email));
        stringRedisTemplate.opsForValue().set(
                successKey(EmailVerificationPurpose.SIGNUP, null, email),
                "true",
                Duration.ofSeconds(successExpirationSeconds)
        );
    }

    @Transactional
    public void sendChangeEmailCode(Long userId, String rawNewEmail) {
        String newEmail = normalizeEmail(rawNewEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getEmail().equals(newEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 사용 중인 이메일과 같습니다.");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        String code = generateCode();

        stringRedisTemplate.opsForValue().set(
                codeKey(EmailVerificationPurpose.CHANGE_EMAIL, userId, newEmail),
                code,
                Duration.ofSeconds(codeExpirationSeconds)
        );

        stringRedisTemplate.delete(successKey(EmailVerificationPurpose.CHANGE_EMAIL, userId, newEmail));

        sendVerificationEmail(newEmail, code, "이메일 변경");
    }

    @Transactional
    public void verifyChangeEmailCode(Long userId, String rawNewEmail, String inputCode) {
        String newEmail = normalizeEmail(rawNewEmail);

        String savedCode = stringRedisTemplate.opsForValue()
                .get(codeKey(EmailVerificationPurpose.CHANGE_EMAIL, userId, newEmail));

        if (savedCode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증코드가 만료되었거나 존재하지 않습니다.");
        }

        if (!savedCode.equals(inputCode.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증코드가 올바르지 않습니다.");
        }

        stringRedisTemplate.delete(codeKey(EmailVerificationPurpose.CHANGE_EMAIL, userId, newEmail));
        stringRedisTemplate.opsForValue().set(
                successKey(EmailVerificationPurpose.CHANGE_EMAIL, userId, newEmail),
                "true",
                Duration.ofSeconds(successExpirationSeconds)
        );
    }

    public void ensureSignupVerified(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        String verified = stringRedisTemplate.opsForValue()
                .get(successKey(EmailVerificationPurpose.SIGNUP, null, email));

        if (!"true".equals(verified)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다.");
        }
    }

    public void ensureChangeEmailVerified(Long userId, String rawNewEmail) {
        String newEmail = normalizeEmail(rawNewEmail);
        String verified = stringRedisTemplate.opsForValue()
                .get(successKey(EmailVerificationPurpose.CHANGE_EMAIL, userId, newEmail));

        if (!"true".equals(verified)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 이메일 인증이 완료되지 않았습니다.");
        }
    }

    @Transactional
    public void clearSignupVerification(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        stringRedisTemplate.delete(successKey(EmailVerificationPurpose.SIGNUP, null, email));
    }

    @Transactional
    public void clearChangeEmailVerification(Long userId, String rawNewEmail) {
        String newEmail = normalizeEmail(rawNewEmail);
        stringRedisTemplate.delete(successKey(EmailVerificationPurpose.CHANGE_EMAIL, userId, newEmail));
    }

    private void sendVerificationEmail(String to, String code, String actionName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[TodoKing] 이메일 인증코드");
        message.setText(
                "안녕하세요.\n\n" +
                        actionName + "을 위한 인증코드는 [" + code + "] 입니다.\n" +
                        "인증코드는 5분간 유효합니다."
        );
        mailSender.send(message);
    }

    private String codeKey(EmailVerificationPurpose purpose, Long userId, String email) {
        if (purpose == EmailVerificationPurpose.CHANGE_EMAIL) {
            return "email:verify:code:" + purpose.name() + ":" + userId + ":" + email;
        }
        return "email:verify:code:" + purpose.name() + ":" + email;
    }

    private String successKey(EmailVerificationPurpose purpose, Long userId, String email) {
        if (purpose == EmailVerificationPurpose.CHANGE_EMAIL) {
            return "email:verify:success:" + purpose.name() + ":" + userId + ":" + email;
        }
        return "email:verify:success:" + purpose.name() + ":" + email;
    }

    private String generateCode() {
        int number = RANDOM.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}