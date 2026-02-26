package com.codepresso.codepresso.branch.controller;

import com.codepresso.codepresso.branch.dto.request.BranchSearchRequest;
import com.codepresso.codepresso.branch.dto.response.BranchInfoResponse;
import com.codepresso.codepresso.branch.dto.response.BranchListResponse;
import com.codepresso.codepresso.branch.service.BranchService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/branch")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }



    @GetMapping(value = "/{branchId}")
    @ResponseBody
    public ResponseEntity<BranchInfoResponse> branchInfo(@PathVariable Long branchId) {
        BranchInfoResponse response = branchService.getBranchInfo(branchId);
        return ResponseEntity.ok(response);
    }
}
