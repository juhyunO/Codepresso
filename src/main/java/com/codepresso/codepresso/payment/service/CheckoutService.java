package com.codepresso.codepresso.payment.service;

import com.codepresso.codepresso.cart.dto.CartItemResponse;
import com.codepresso.codepresso.cart.dto.CartOptionResponse;
import com.codepresso.codepresso.cart.dto.CartResponse;
import com.codepresso.codepresso.cart.service.CartService;
import com.codepresso.codepresso.payment.dto.response.CheckoutResponse;
import com.codepresso.codepresso.product.dto.ProductDetailResponse;
import com.codepresso.codepresso.product.dto.ProductOptionDTO;
import com.codepresso.codepresso.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 결제 페이지 데이터 준비
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final ProductService productService;

    /**
     * 장바구니 결제페이지 데이터 준비
     */
    public CheckoutResponse prepareCartCheckout(Long memberId) {
        CartResponse cartData = cartService.getCartByMemberId(memberId);

        int totalAmount = cartData.getItems().stream()
                .mapToInt(CartItemResponse::getPrice)
                .sum();

        int totalQuantity = cartData.getItems().stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        List<CheckoutResponse.OrderItem> orderItems = cartData.getItems().stream()
                .map(this::convertCartItemToOrderItem)
                .collect(Collectors.toList());

        return CheckoutResponse.builder()
                .totalAmount(totalAmount)
                .totalQuantity(totalQuantity)
                .isFromCart(true)
                .orderItems(orderItems)
                .build();
    }

    /**
     * 직접 결제 페이지 데이터 준비
     */
    public CheckoutResponse prepareDirectCheckout(Long productId, Integer quantity, List<Long> optionIds) {
        validateQuantity(quantity);

        ProductDetailResponse productDetail = productService.findByProductId(productId);
        List<ProductOptionDTO> selectOptions = new ArrayList<>();
        int totalAmount = calculateTotalAmount(productDetail, optionIds,quantity, selectOptions);

        CheckoutResponse.OrderItem orderItem = convertDirectItemToOrderItem(productDetail, selectOptions, quantity, totalAmount, optionIds);

        return CheckoutResponse.builder()
                .totalAmount(totalAmount)
                .totalQuantity(quantity)
                .isFromCart(false)
                .orderItems(Collections.singletonList(orderItem))
                .build();
    }

    private void validateQuantity(Integer quantity) {
        if(quantity == null || quantity <=0) {
            throw new IllegalArgumentException("수량은 1 이상이여야 합니다.");
        }
    }

    private CheckoutResponse.OrderItem convertCartItemToOrderItem(CartItemResponse cartItem) {
        int unitPrice = cartItem.getPrice() / cartItem.getQuantity();

        return CheckoutResponse.OrderItem.builder()
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .productPhoto(cartItem.getProductPhoto())
                .quantity(cartItem.getQuantity())
                .unitPrice(unitPrice)
                .price(cartItem.getPrice())
                .lineTotal(cartItem.getPrice())
                .optionIds(extractOptionIds(cartItem))
                .optionNames(extractOptionNames(cartItem))
                .build();
    }

    private CheckoutResponse.OrderItem convertDirectItemToOrderItem(
            ProductDetailResponse productDetail,
            List<ProductOptionDTO> selectedOptions,
            Integer quantity,
            Integer totalAmount,
            List<Long> optionIds) {

        int unitPrice = totalAmount / quantity;

        return CheckoutResponse.OrderItem.builder()
                .productId(productDetail.getProductId())
                .productName(productDetail.getProductName())
                .productPhoto(productDetail.getProductPhoto())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .price(unitPrice)
                .lineTotal(totalAmount)
                .optionIds(optionIds != null ? optionIds : Collections.emptyList())
                .optionNames(selectedOptions.stream()
                        .map(ProductOptionDTO::getOptionStyleName)
                        .collect(Collectors.toList()))
                .build();
    }

    private int calculateTotalAmount(ProductDetailResponse productDetail, List<Long> optionIds,
                                     Integer quantity, List<ProductOptionDTO> selectedOptions) {
        int basePrice = productDetail.getPrice() != null ? productDetail.getPrice() : 0;
        int optionPrice = 0;

        if (optionIds != null && !optionIds.isEmpty()) {
            for (Long optionId : optionIds) {
                ProductOptionDTO foundOption = productDetail.getProductOptions().stream()
                        .filter(option -> option.getOptionId().equals(optionId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다: " + optionId));

                selectedOptions.add(foundOption);
                optionPrice += foundOption.getExtraPrice() != null ? foundOption.getExtraPrice() : 0;
            }
        }
        return (basePrice + optionPrice) * quantity;
    }

    private List<Long> extractOptionIds(CartItemResponse cartItem) {
        if (cartItem.getOptions() == null || cartItem.getOptions().isEmpty()) {
            return Collections.emptyList();
        }
        return cartItem.getOptions().stream()
                .map(CartOptionResponse::getOptionId)
                .collect(Collectors.toList());
    }

    private List<String> extractOptionNames(CartItemResponse cartItem) {
        if (cartItem.getOptions() == null || cartItem.getOptions().isEmpty()) {
            return Collections.emptyList();
        }
        return cartItem.getOptions().stream()
                .map(CartOptionResponse::getOptionStyle)
                .collect(Collectors.toList());
    }
}
