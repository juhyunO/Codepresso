package com.codepresso.codepresso.member.service;

import com.codepresso.codepresso.auth.dto.request.SignUpRequest;
import com.codepresso.codepresso.member.entity.Member;
import com.codepresso.codepresso.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 아이디 중복 여부 확인
     */
    public boolean isAccountIdDuplicate(String accountId) {
        return memberRepository.existsByAccountId(accountId);
    }

    /**
     * 닉네임 중복 여부 확인
     */
    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    /**
     * 이메일 중복 여부 확인
     */
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 회원가입 처리
     */
    @Transactional
    public Member register(SignUpRequest request) {
        validateDuplicate(request);

        String normalizedPhone = normalizePhone(request.getPhone());

        Member member = Member.builder()
                .accountId(request.getAccountId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .name(request.getName())
                .phone(normalizedPhone)
                .build();

        try {
            return memberRepository.save(member);
        }catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("중복된 정보가 존재합니다.");
        }
    }

    private void validateDuplicate(SignUpRequest request) {
        if(isAccountIdDuplicate(request.getAccountId())){
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (isNicknameDuplicate(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        if (isEmailDuplicate(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String normalized = phone.replaceAll("[^0-9]", "").trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
