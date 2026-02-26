package com.codepresso.codepresso.payment.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentConfirmResponse {
    private String paymentKey;
    private String orderId;
    private String status;
    private String method;
    private Integer totalAmount;
    private String approvedAt;
    private String requestedAt;
    private TossCard card;
    private TossReceipt receipt;

    @Getter
    @NoArgsConstructor
    public static class TossCard {
        private String company;
        private String number;
        private Integer installmentPlanMonths;
        private String cardType;
        private String ownerType;
        private String acquireStatus;
    }

    @Getter
    @NoArgsConstructor
    public static class TossReceipt {
        private String url;
    }
}
