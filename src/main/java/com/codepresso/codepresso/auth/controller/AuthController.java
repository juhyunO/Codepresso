package com.codepresso.codepresso.auth.controller;

import com.codepresso.codepresso.auth.dto.request.EmailVerificationRequest;
import com.codepresso.codepresso.auth.dto.request.SignUpRequest;
import com.codepresso.codepresso.auth.dto.request.VerifyCodeRequest;
import com.codepresso.codepresso.auth.dto.response.DuplicateCheckResponse;
import com.codepresso.codepresso.auth.dto.response.EmailVerificationResponse;
import com.codepresso.codepresso.auth.dto.response.SignUpResponse;
import com.codepresso.codepresso.auth.dto.response.VerifyCodeResponse;
import com.codepresso.codepresso.member.entity.Member;
import com.codepresso.codepresso.auth.service.EmailService;
import com.codepresso.codepresso.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 인증
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final MemberService memberService;
    private final EmailService emailService;
    
    // 이메일 인증번호 저장용 (실제 운영에서는 Redis 등 사용 권장)
    private final Map<String, String> emailVerificationCodes = new HashMap<>();

    /**
     * 중복체크
     */
    @GetMapping("/check")
    public ResponseEntity<DuplicateCheckResponse> check(
            @RequestParam("value") String value,
            @RequestParam(value = "field", required = false) CheckField field
    ) {
        CheckField target = field != null ? field : CheckField.ID;
        boolean duplicate = isDuplicate(target, value);

        DuplicateCheckResponse response = DuplicateCheckResponse.of(target.asKey(), duplicate);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@RequestBody @Valid SignUpRequest request) {
        log.info("[signup] accountId={}", request.getAccountId());

        Member member = memberService.register(request);

        log.info("[signup] saved id={}, name={}", member.getId(), member.getName());

        return ResponseEntity.ok(SignUpResponse.from(member));
    }

    /**
     * 이메일 인증번호 발송
     */
    @PostMapping("/send-email-verification")
    public ResponseEntity<EmailVerificationResponse> sendEmailVerification(@RequestBody @Valid EmailVerificationRequest request) {
        try {
            String verificationCode = generateVerificationCode();

            emailVerificationCodes.put(request.getEmail(), verificationCode);

            emailService.sendEmailVerification(request.getEmail(), verificationCode);

            log.info("[email-verification] sent to={}", request.getEmail());

            return ResponseEntity.ok(EmailVerificationResponse.success("인증번호가 발송되었습니다."));

        } catch (Exception e) {
            log.error("[email-verification] failed to={}, error={}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(EmailVerificationResponse.fail("이메일 발송에 실패했습니다. 다시 시도해주세요."));
        }
    }

    /** 이메일 인증번호 검증 */
    @PostMapping("/verify-email-code")
    public ResponseEntity<VerifyCodeResponse> verifyEmailCode(@RequestBody @Valid VerifyCodeRequest request) {
        String storedCode = emailVerificationCodes.remove(request.getEmail());
        boolean isValid = request.getCode().equals(storedCode);

        if (isValid) {
            log.info("[email-verification] verified: email={}", request.getEmail());
            return ResponseEntity.ok(VerifyCodeResponse.success("이메일 인증이 완료되었습니다."));
        } else {
            log.warn("[email-verification] failed: email={}", request.getEmail());
            return ResponseEntity.ok(VerifyCodeResponse.fail("인증번호가 일치하지 않거나 만료되었습니다."));
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }

    private boolean isDuplicate(CheckField target, String value) {
        return switch (target) {
            case NICKNAME -> memberService.isNicknameDuplicate(value);
            case EMAIL -> memberService.isEmailDuplicate(value);
            case ID -> memberService.isAccountIdDuplicate(value);
        };
    }
}
