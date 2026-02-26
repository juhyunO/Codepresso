package com.codepresso.codepresso.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DuplicateCheckResponse {
    private String field;
    private boolean duplicate;

    public static DuplicateCheckResponse of(String field, boolean duplicate) {
        return DuplicateCheckResponse.builder()
                .field(field)
                .duplicate(duplicate)
                .build();
    }
}
