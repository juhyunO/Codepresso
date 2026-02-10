package com.codepresso.codepresso.cart.controller;

import com.codepresso.codepresso.cart.dto.CartResponse;
import com.codepresso.codepresso.cart.service.CartService;
import com.codepresso.codepresso.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 장바구니 뷰 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class CartViewController {

    private final CartService cartService;

    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal LoginUser loginUser, Model model) {
        CartResponse cart = null;
        if (loginUser != null) {
            try {
                cart = cartService.getCartByMemberId(loginUser.getMemberId());
            } catch (IllegalArgumentException ignored) {
                // 장바구니가 없으면 빈 화면을 보여준다.
            }
        }
        model.addAttribute("cart", cart);
        return "cart/cart";
    }
}
