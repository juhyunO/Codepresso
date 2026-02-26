package com.codepresso.codepresso.payment.repository;

import com.codepresso.codepresso.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findByOrdersId(Long ordersId);

    List<Payment> findByOrdersMemberId(Long memberId);

    boolean existsByPaymentKey(String paymentKey);
}
