package com.codepresso.codepresso.auth.controller;

import com.codepresso.codepresso.auth.dto.request.IdFindRequest;
import com.codepresso.codepresso.auth.dto.request.VerifyCodeRequest;
import com.codepresso.codepresso.auth.dto.response.IdFindResponse;
import com.codepresso.codepresso.auth.service.IdFindService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 아이디 찾기 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class IdFindController {

    private final IdFindService idFindService;

    /**
     * 아이디 찾기 - 인증번호 발송
     */
    @PostMapping("/find-id")
    public ResponseEntity<IdFindResponse> findId(@RequestBody @Valid IdFindRequest request) {
        log.info("[find-id] email={}", request.getEmail());

        IdFindResponse response = idFindService.findId(request);

        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 인증번호 검증 후 아이디 반환
     */
    @PostMapping("/verify-id-code")
    public ResponseEntity<IdFindResponse> verifyIdCode(@RequestBody @Valid VerifyCodeRequest request) {
        IdFindResponse response = idFindService.verifyCodeAndGetId(request);

        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
