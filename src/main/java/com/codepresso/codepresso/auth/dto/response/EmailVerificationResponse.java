package com.codepresso.codepresso.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerificationResponse {
    private boolean success;
    private String message;

    public static EmailVerificationResponse success(String message) {
        return EmailVerificationResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static EmailVerificationResponse fail(String message) {
        return EmailVerificationResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
