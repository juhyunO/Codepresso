package com.codepresso.codepresso.auth.dto.response;

import com.codepresso.codepresso.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpResponse {
    private Long id;
    private String accountId;
    private String name;
    private String phone;
    private String nickname;
    private String email;

    public static SignUpResponse from(Member member) {
        return SignUpResponse.builder()
                .id(member.getId())
                .accountId(member.getAccountId())
                .name(member.getName())
                .phone(member.getPhone())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .build();
    }
}
