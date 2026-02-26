package com.codepresso.codepresso.payment.controller;

import com.codepresso.codepresso.payment.dto.request.CheckoutRequest;
import com.codepresso.codepresso.payment.dto.request.PaymentRequest;
import com.codepresso.codepresso.payment.dto.response.CheckoutResponse;
import com.codepresso.codepresso.payment.dto.request.TossPaymentSuccessRequest;
import com.codepresso.codepresso.payment.dto.response.PaymentResponse;
import com.codepresso.codepresso.payment.service.CheckoutService;
import com.codepresso.codepresso.security.LoginUser;
import com.codepresso.codepresso.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결제 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final CheckoutService checkoutService;

    /**
     * 장바구니 결제페이지 데이터 조회 API
     */
    @GetMapping("/cart")
    public ResponseEntity<CheckoutResponse> getCartCheckoutData(@AuthenticationPrincipal LoginUser loginUser) {
        CheckoutResponse response = checkoutService.prepareCartCheckout(loginUser.getMemberId());
        return ResponseEntity.ok(response);
    }

    /**
     * 직접 결제 페이지 데이터 조회 API
     */
    @GetMapping("/direct")
    public ResponseEntity<CheckoutResponse> getDirectCheckoutData(
            @RequestBody @Valid CheckoutRequest.OrderItem orderItem) {
        CheckoutResponse response = checkoutService.prepareDirectCheckout(
                orderItem.getProductId(),
                orderItem.getQuantity(),
                orderItem.getOptionIds());
        return ResponseEntity.ok(response);
    }

    /**
     * 토스페이먼츠 결제 성공 시 주문 생성 API
     * POST /api/payments/toss-success
     */
    @PostMapping("/toss-success")
    public ResponseEntity<CheckoutResponse> processTossPaymentSuccess(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid TossPaymentSuccessRequest request) {
        request.setMemberId(loginUser.getMemberId());
        CheckoutResponse response = paymentService.processTossPaymentSuccess(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Toss 결제 승인 API (새로운 방식 - 서버에서 Toss API 호출)
     * POST /api/payments/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid PaymentRequest request) {
        PaymentResponse response = paymentService.confirmPayment(request, loginUser.getMemberId());
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 상세 조회
     * GET /api/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 결제 내역 목록 조회
     * GET /api/payments/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @AuthenticationPrincipal LoginUser loginUser) {
        List<PaymentResponse> responses = paymentService.getPaymentsByMember(loginUser.getMemberId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 주문 ID로 결제 조회
     * GET /api/payments/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }
}
