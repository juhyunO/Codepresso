package com.codepresso.codepresso.branch.controller;

import com.codepresso.codepresso.branch.dto.response.BranchInfoResponse;
import com.codepresso.codepresso.branch.service.BranchService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
