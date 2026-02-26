package com.codepresso.codepresso.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyCodeResponse {
    private boolean valid;
    private String message;

    public static VerifyCodeResponse success(String message) {
        return VerifyCodeResponse.builder()
                .valid(true)
                .message(message)
                .build();
    }

    public static VerifyCodeResponse fail(String message) {
        return VerifyCodeResponse.builder()
                .valid(false)
                .message(message)
                .build();
    }
}
