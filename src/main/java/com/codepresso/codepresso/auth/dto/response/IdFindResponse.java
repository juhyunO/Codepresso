package com.codepresso.codepresso.auth.dto.response;

import lombok.*;

/**
 * 아이디 찾기 응답 DTO
 */
@Getter
@Builder
public class IdFindResponse {
    private boolean success;      // 성공 여부
    private String message;       // 응답 메시지
    private String accountId;     // 찾은 아이디 (성공 시에만)

    public static IdFindResponse success(String message, String accountId) {
        return IdFindResponse.builder()
                .success(true)
                .message(message)
                .accountId(accountId)
                .build();
    }

    public static IdFindResponse successWithoutId(String message) {
        return IdFindResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static IdFindResponse fail(String message) {
        return IdFindResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
