package com.codepresso.codepresso.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비밀번호 찾기 요청 DTO
 * - 아이디와 이메일을 통한 사용자 확인
 */
@Getter
@NoArgsConstructor
public class PasswordFindRequest {
    @NotBlank(message = "아이디를 입력해주세요")
    private String accountId;

    @NotBlank(message = "이메일을 입력해주세요")
    @Email
    private String email;
}
