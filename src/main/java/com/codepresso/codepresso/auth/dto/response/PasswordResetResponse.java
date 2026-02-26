package com.codepresso.codepresso.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 비밀번호 재설정 응답 DTO
 */
@Getter
@Builder
public class PasswordResetResponse {
    private boolean success;
    private String message;

    public static PasswordResetResponse success(String message) {
        return PasswordResetResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static PasswordResetResponse fail(String message) {
        return PasswordResetResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
