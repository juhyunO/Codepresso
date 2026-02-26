package com.codepresso.codepresso.payment.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → PaymentMaster
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // 카드 결제 상세
    @Column(name = "card_company", length = 50)
    private String cardCompany;

    @Column(name = "card_number", length = 20)
    private String cardNumber;

    @Column(name = "installment_months")
    private Integer installmentMonths;

    @Column(name = "card_type", length = 20)
    private String cardType;

    @Column(name = "owner_type", length = 20)
    private String ownerType;

    @Column(name = "acquire_status", length = 20)
    private String acquireStatus;

    @Builder
    public PaymentDetail(Payment payment, String cardCompany, String cardNumber,
                         Integer installmentMonths, String cardType, String ownerType,
                         String acquireStatus) {
        this.payment = payment;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.installmentMonths = installmentMonths;
        this.cardType = cardType;
        this.ownerType = ownerType;
        this.acquireStatus = acquireStatus;
    }
}
