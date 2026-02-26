package com.codepresso.codepresso.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 비밀번호 찾기 응답 DTO
 */
@Getter
@Builder
public class PasswordFindResponse {
    private boolean success;
    private String message;

    public static PasswordFindResponse success(String message) {
        return PasswordFindResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static PasswordFindResponse fail(String message) {
        return PasswordFindResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
