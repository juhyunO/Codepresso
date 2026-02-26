package com.codepresso.codepresso.branch.dto.response;

import com.codepresso.codepresso.branch.dto.request.BranchSearchRequest;
import com.codepresso.codepresso.branch.entity.Branch;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class BranchListResponse {
    private List<BranchResponse> branches;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    // 검색 조건 (View에서 사용)
    private String keyword;
    private Double lat;
    private Double lng;
    private Double radius;

    // Page<Branch> → BranchListResponse 변환
    public static BranchListResponse from(Page<Branch> page, BranchSearchRequest request) {
        List<BranchResponse> branchResponses = page.getContent().stream()
                .map(BranchResponse::from)
                .toList();

        return BranchListResponse.builder()
                .branches(branchResponses)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .keyword(request.getQ())
                .lat(request.getLat())
                .lng(request.getLng())
                .radius(request.getRadiusKm())
                .build();
    }
}
