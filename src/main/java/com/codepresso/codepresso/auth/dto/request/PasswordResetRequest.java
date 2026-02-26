package com.codepresso.codepresso.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비밀번호 재설정 요청 DTO
 * - 인증번호 확인 후 새 비밀번호 설정
 */
@Getter
@NoArgsConstructor
public class PasswordResetRequest {
    @NotBlank(message = "아이디를 입력해주세요.")
    private String accountId;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email
    private String email;

    @NotBlank(message = "인증번호를 입력해주세요.")
    private String verificationCode;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String newPassword;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String confirmPassword;
}
