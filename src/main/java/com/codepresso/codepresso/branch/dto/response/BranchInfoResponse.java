package com.codepresso.codepresso.branch.dto.response;

import com.codepresso.codepresso.branch.entity.Branch;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class BranchInfoResponse {
    private Long id;
    private String name;
    private String address;
    private LocalTime openingTime;
    private LocalTime closingTime;

    public static BranchInfoResponse from(Branch branch) {
        return BranchInfoResponse.builder()
                .id(branch.getId())
                .name(branch.getBranchName())
                .address(branch.getAddress())
                .openingTime(branch.getOpeningTime())
                .closingTime(branch.getClosingTime())
                .build();
    }
}
