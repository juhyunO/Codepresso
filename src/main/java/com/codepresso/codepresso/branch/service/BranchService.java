package com.codepresso.codepresso.branch.service;

import com.codepresso.codepresso.branch.dto.request.BranchSearchRequest;
import com.codepresso.codepresso.branch.dto.response.BranchInfoResponse;
import com.codepresso.codepresso.branch.dto.response.BranchListResponse;
import com.codepresso.codepresso.branch.entity.Branch;
import com.codepresso.codepresso.branch.repository.BranchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public Page<Branch> getBranchPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "branchName"));
        return branchRepository.findAll(pageable);
    }

    public Page<Branch> searchByName(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "branchName"));
        return branchRepository.findByBranchNameContainingIgnoreCase(keyword, pageable);
    }

    public Page<Branch> getNearby(double lat, double lng, double radiusKm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return branchRepository.findNearby(lat, lng, radiusKm, pageable);
    }

    /**
     * 검색 조건에 따라 매장 목록 조회
     * - 검색어가 있으면 이름 검색
     * - 위치 정보가 있으면 근처 매장 검색
     * - 둘 다 없으면 전체 목록
     */
    public BranchListResponse searchBranches(BranchSearchRequest request) {
        Page<Branch> branchPage;

        if(request.hasKeyword()) {
            branchPage = searchByName(request.getQ(), request.getPage(), request.getSize());
        }else if(request.hasLocation()) {
            branchPage = getNearby(request.getLat(), request.getLng(), request.getRadiusKm(), request.getPage(), request.getSize());
        }else {
            branchPage = getBranchPage(request.getPage(), request.getSize());
        }

        return BranchListResponse.from(branchPage, request);
    }

    /**
     * 단일 매장 상제 조회 정보
     */
    public BranchInfoResponse getBranchInfo(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다. ID: " + branchId));

        return BranchInfoResponse.from(branch);
    }
}
