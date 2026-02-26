package com.codepresso.codepresso.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyCodeRequest {
    @NotBlank(message = "이메일을 입력해주세요")
    private String email;

    @NotBlank(message = "인증번호를 입력해주세요")
    private String code;
}
