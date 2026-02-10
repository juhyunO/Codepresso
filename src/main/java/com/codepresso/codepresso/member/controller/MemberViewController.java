package com.codepresso.codepresso.member.controller;

import com.codepresso.codepresso.member.dto.FavoriteListResponse;
import com.codepresso.codepresso.member.service.FavoriteService;
import com.codepresso.codepresso.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 회원 관련 뷰 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class MemberViewController {

    private final FavoriteService favoriteService;

    @GetMapping("/member/mypage")
    public String mypage() {
        return "member/mypage";
    }

    @GetMapping("/favorites")
    public String favoriteList(Authentication authentication, Model model) {
        Long memberId = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser lu) {
            memberId = lu.getMemberId();
        }
        try {
            FavoriteListResponse favoriteList = favoriteService.getFavoriteList(memberId);
            model.addAttribute("favoriteList", favoriteList);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "member/favorite-list";
    }
}
