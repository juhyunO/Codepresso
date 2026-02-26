package com.codepresso.codepresso.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 아이디 찾기 요청 DTO
 */
@Getter
@NoArgsConstructor
public class IdFindRequest {
    @NotBlank(message = "이메일을 입력해주세요")
    @Email
    private String email;     // 이메일
}
