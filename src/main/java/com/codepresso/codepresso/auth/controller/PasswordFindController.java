package com.codepresso.codepresso.auth.controller;

import com.codepresso.codepresso.auth.dto.request.PasswordFindRequest;
import com.codepresso.codepresso.auth.dto.response.PasswordFindResponse;
import com.codepresso.codepresso.auth.dto.request.PasswordResetRequest;
import com.codepresso.codepresso.auth.dto.response.PasswordResetResponse;
import com.codepresso.codepresso.auth.service.PasswordFindService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 비밀번호 찾기 RESTful API 컨트롤러
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PasswordFindController {

    private final PasswordFindService passwordFindService;

    /**
     * 비밀번호 찾기 - 사용자 확인 및 인증번호 발급
     */
    @PostMapping("/password/find")
    public ResponseEntity<PasswordFindResponse> findPassword(@RequestBody @Valid PasswordFindRequest request) {
        log.info("[password-find] accountId={}, email={}", request.getAccountId(), request.getEmail());

        PasswordFindResponse response = passwordFindService.findPassword(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 비밀번호 재설정
     */
    @PostMapping("/password/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(@RequestBody @Valid PasswordResetRequest request) {
        log.info("[password-reset] accountId={}, email={}", request.getAccountId(), request.getEmail());

        PasswordResetResponse response = passwordFindService.resetPassword(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
