package com.codepresso.codepresso.branch.controller;

import com.codepresso.codepresso.branch.dto.request.BranchSearchRequest;
import com.codepresso.codepresso.branch.dto.response.BranchListResponse;
import com.codepresso.codepresso.branch.service.BranchService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/branch")
public class BranchViewController {
    private final BranchService branchService;

    public BranchViewController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "lat", required = false) Double lat,
                       @RequestParam(value = "lng", required = false) Double lng,
                       @RequestParam(value = "radius", required = false) Double radiusKm) {
        BranchSearchRequest request = BranchSearchRequest.of(q, lat, lng, radiusKm, 0, 6);
        BranchListResponse response = branchService.searchBranches(request);

        model.addAttribute("response", response);
        return "branch/branch-list";
    }

    @GetMapping("/page")
    public String page(@RequestParam int page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "lat", required = false) Double lat,
                       @RequestParam(value = "lng", required = false) Double lng,
                       @RequestParam(value = "radius", required = false) Double radiusKm,
                       Model model, HttpServletResponse httpResponse) {
        BranchSearchRequest request = BranchSearchRequest.of(q, lat, lng, radiusKm, page, size);
        BranchListResponse response = branchService.searchBranches(request);

        model.addAttribute("response", response);
        httpResponse.setHeader("X-Has-Next", String.valueOf(response.isHasNext()));
        return "branch/branch-cards";
    }
}
