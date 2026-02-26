package com.codepresso.codepresso.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailVerificationRequest {
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email
    private String email;
}
