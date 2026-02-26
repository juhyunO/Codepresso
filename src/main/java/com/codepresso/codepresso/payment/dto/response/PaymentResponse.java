package com.codepresso.codepresso.payment.dto.response;

import com.codepresso.codepresso.payment.entity.Payment;
import com.codepresso.codepresso.payment.entity.PaymentMethod;
import com.codepresso.codepresso.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private String paymentKey;
    private Integer amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private LocalDateTime approvedAt;
    private String receiptUrl;

    // 카드 정보 (카드 결제 시)
    private String cardCompany;
    private String cardNumber;
    private Integer installmentMonths;

    public static PaymentResponse from(Payment payment) {
        PaymentResponseBuilder builder = PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrders().getId())
                .paymentKey(payment.getPaymentKey())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .approvedAt(payment.getApprovedAt())
                .receiptUrl(payment.getReceiptUrl());

        // 카드 결제인 경우 카드 정보 추가
        if (payment.getPaymentDetail() != null) {
            builder.cardCompany(payment.getPaymentDetail().getCardCompany())
                    .cardNumber(payment.getPaymentDetail().getCardNumber())
                    .installmentMonths(payment.getPaymentDetail().getInstallmentMonths());
        }

        return builder.build();
    }
}
