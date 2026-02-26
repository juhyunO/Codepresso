package com.codepresso.codepresso.auth.service;

import com.codepresso.codepresso.auth.dto.request.PasswordFindRequest;
import com.codepresso.codepresso.auth.dto.response.PasswordFindResponse;
import com.codepresso.codepresso.auth.dto.request.PasswordResetRequest;
import com.codepresso.codepresso.auth.dto.response.PasswordResetResponse;
import com.codepresso.codepresso.member.entity.Member;
import com.codepresso.codepresso.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 비밀번호 찾기 서비스
 * - 아이디/이메일로 사용자 확인
 * - 메모리 맵 기반 인증번호 관리
 * - 이메일 발송 및 비밀번호 재설정
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordFindService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // 비밀번호 찾기용 인증번호 저장 (이메일 -> 인증번호)
    private final Map<String, String> passwordResetCodes = new ConcurrentHashMap<>();
    private final Random random = new Random();

    /**
     * 비밀번호 찾기 - 사용자 확인 및 인증번호 발급
     */
    @Transactional
    public PasswordFindResponse findPassword(PasswordFindRequest request) {
        try {
            Member member = memberRepository.findByAccountIdAndEmail(request.getAccountId(), request.getEmail())
                    .orElseThrow(() -> new NoSuchElementException("아이디 또는 이메일이 일치하지 않습니다."));

            String verificationCode = generatePasswordResetCode();

            passwordResetCodes.put(request.getEmail(), verificationCode);

            emailService.sendPasswordResetEmail(request.getEmail(), request.getAccountId(), verificationCode);

            log.info("비밀번호 찾기 인증번호 발송 완료 : {} ({})",request.getEmail(), request.getAccountId());

            return PasswordFindResponse.success("인증번호가 이메일로 발송되었습니다. 이메일을 확인해주세요");
        }catch (NoSuchElementException e) {
            log.warn("비밀번호 찾기 실패: {} ({})", request.getEmail(), request.getAccountId());
            return PasswordFindResponse.fail("아이디 또는 이메일이 일치하지 않습니다.");
        }catch (Exception e) {
            log.error("이메일 발송 실패: {} - {}", request.getEmail(), e.getMessage());
            return PasswordFindResponse.fail("이메일 발송에 실패했습니다. 다시 시도해주세요.");
        }
    }

    /**
     * 비밀번호 재설정
     */
    @Transactional
    public PasswordResetResponse resetPassword(PasswordResetRequest request) {
        // 사용자 확인
        Member member = memberRepository.findByAccountIdAndEmail(request.getAccountId(), request.getEmail())
                .orElse(null);

        if (member == null) {
            return PasswordResetResponse.fail("아이디 또는 이메일이 일치하지 않습니다.");
        }

        // 인증번호 검증
        String storedCode = passwordResetCodes.remove(request.getEmail());
        if (!request.getVerificationCode().equals(storedCode)) {
            return PasswordResetResponse.fail("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        // 비밀번호 확인 일치 검증
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return PasswordResetResponse.fail("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 암호화 및 저장
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        member.setPassword(encodedPassword);
        memberRepository.save(member);

        log.info("비밀번호 재설정 완료: {} ({})", request.getEmail(), request.getAccountId());

        return PasswordResetResponse.success("비밀번호가 성공적으로 변경되었습니다.");
    }
    
    /**
     * 비밀번호 재설정용 인증번호 생성
     */
    private String generatePasswordResetCode() {
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }
}