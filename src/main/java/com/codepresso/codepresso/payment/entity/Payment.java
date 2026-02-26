package com.codepresso.codepresso.payment.entity;

import com.codepresso.codepresso.order.entity.Orders;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    // FK -> OrderMaster(1:1 관계)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;

    // TOSS 결제 정보
    @Column(name = "payment_key", length = 200, unique = true)
    private String paymentKey;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 20)
    private PaymentMethod method;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PaymentDetail paymentDetail;

    @Builder
    public Payment(Orders orders, String paymentKey, Integer amount, PaymentStatus status,
                   PaymentMethod method, LocalDateTime approvedAt, LocalDateTime requestedAt,
                   String receiptUrl) {
        this.orders = orders;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.approvedAt = approvedAt;
        this.requestedAt = requestedAt;
        this.receiptUrl = receiptUrl;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 상태 변경 메서드
    public void updateStatus(PaymentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    // PaymentDetail 연결
    public void setPaymentDetail(PaymentDetail paymentDetail) {
        this.paymentDetail = paymentDetail;
    }
}
