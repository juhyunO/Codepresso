package com.codepresso.codepresso.auth.service;

import com.codepresso.codepresso.auth.dto.request.IdFindRequest;
import com.codepresso.codepresso.auth.dto.request.VerifyCodeRequest;
import com.codepresso.codepresso.auth.dto.response.IdFindResponse;
import com.codepresso.codepresso.member.entity.Member;
import com.codepresso.codepresso.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 아이디 찾기 서비스
 * - 닉네임/이메일로 사용자 확인
 * - 메모리 맵 기반 인증번호 관리
 * - 이메일 발송 및 아이디 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdFindService {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    
    // 아이디 찾기용 인증번호 저장 (이메일 -> 인증번호)
    private final Map<String, String> idFindCodes = new ConcurrentHashMap<>();
    private final Random random = new Random();

    /**
     * 아이디 찾기 - 사용자 확인 및 인증번호 발급
     */
    @Transactional
    public IdFindResponse findId(IdFindRequest request) {
        try {
            // 이메일로 사용자 확인
            Member member = memberRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new NoSuchElementException("이메일이 일치하지 않습니다."));

            // 6자리 인증번호 생성
            String verificationCode = generateIdFindCode();
            
            // 인증번호 저장
            idFindCodes.put(request.getEmail(), verificationCode);
            
            // 이메일 발송
            emailService.sendIdFindEmail(request.getEmail(), member.getNickname(), verificationCode);
            
            log.info("아이디 찾기 인증번호 이메일 발송 완료: {}", request.getEmail());

            return IdFindResponse.successWithoutId("인증번호가 이메일로 발송되었습니다. 이메일을 확인해주세요");
                    
        } catch (NoSuchElementException e) {
            // 사용자를 찾을 수 없는 경우
            log.warn("아이디 찾기 실패 - 사용자 없음: {}", request.getEmail());
            
            return IdFindResponse.fail("등록되지 않은 이메일입니다.");
                    
        } catch (Exception e) {
            // 이메일 발송 실패 등 기타 오류
            log.error("이메일 발송 실패: {} - {}", request.getEmail(), e.getMessage());
            
            return IdFindResponse.fail("이메일 발송에 실패했습니다. 다시 시도해주세요");
        }
    }

    /**
     * 인증번호 검증 후 아이디 반환
     */
    @Transactional
    public IdFindResponse verifyCodeAndGetId(VerifyCodeRequest request) {
        // 메모리 맵에서 인증번호 검증
        String storedCode = idFindCodes.remove(request.getEmail()); // 한 번 사용하면 삭제
        if (!request.getCode().equals(storedCode)) {
            return IdFindResponse.fail("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        // 이메일로 사용자 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        log.info("아이디 찾기 인증 성공: {} -> {}", request.getEmail(), member.getAccountId());

        return IdFindResponse.successWithoutId("인증이 완료되었습니다.");
    }
    
    /**
     * 아이디 찾기용 인증번호 생성
     */
    private String generateIdFindCode() {
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }
}
