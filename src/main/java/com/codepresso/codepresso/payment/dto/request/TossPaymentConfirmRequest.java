package com.codepresso.codepresso.payment.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentConfirmRequest {
    private String paymentKey;
    private String orderId;
    private Integer amount;
}
