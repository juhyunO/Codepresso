package com.codepresso.codepresso.common.config;

import com.codepresso.codepresso.branch.entity.Branch;
import com.codepresso.codepresso.branch.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 뷰 컨트롤러에 공통 모델 속성을 추가하는 ControllerAdvice
 * Branch 모달이 footer에 포함되어 모든 페이지에서 필요
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private final BranchService branchService;

    @ModelAttribute
    public void populateBranchModalDefaults(Model model) {
        if (model.containsAttribute("selectBranches")) {
            return;
        }

        Page<Branch> branchPage = branchService.getBranchPage(0, 6);
        model.addAttribute("selectBranches", branchPage.getContent());
        model.addAttribute("branchModalHasNext", branchPage.hasNext());
        model.addAttribute("branchModalNextPage", branchPage.hasNext() ? 1 : null);
        model.addAttribute("branchModalPageSize", branchPage.getSize());
    }
}
