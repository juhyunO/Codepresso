package com.codepresso.codepresso.branch.dto.response;

import com.codepresso.codepresso.branch.entity.Branch;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class BranchResponse {
    private Long id;
    private String branchName;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean isOpen;
    private String branchNumber;
    private String photoUrl;

    // Entity → DTO 변환
    public static BranchResponse from(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .branchName(branch.getBranchName())
                .address(branch.getAddress())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .openingTime(branch.getOpeningTime())
                .closingTime(branch.getClosingTime())
                .isOpen(branch.getIsOpen())
                .branchNumber(branch.getBranchNumber())
                .photoUrl(branch.getPhotoUrl())
                .build();
    }
}
