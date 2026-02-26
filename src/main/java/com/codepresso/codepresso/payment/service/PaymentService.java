package com.codepresso.codepresso.payment.service;

import java.time.format.DateTimeFormatter;
import com.codepresso.codepresso.payment.dto.request.PaymentRequest;
import com.codepresso.codepresso.payment.dto.response.CheckoutResponse;
import com.codepresso.codepresso.payment.dto.request.TossPaymentSuccessRequest;
import com.codepresso.codepresso.payment.dto.response.PaymentResponse;
import com.codepresso.codepresso.payment.dto.response.TossPaymentConfirmResponse;
import com.codepresso.codepresso.payment.entity.Payment;
import com.codepresso.codepresso.payment.entity.PaymentDetail;
import com.codepresso.codepresso.payment.entity.PaymentMethod;
import com.codepresso.codepresso.payment.entity.PaymentStatus;
import com.codepresso.codepresso.payment.repository.PaymentRepository;
import com.codepresso.codepresso.order.entity.Orders;
import com.codepresso.codepresso.order.entity.OrdersDetail;
import com.codepresso.codepresso.order.entity.OrdersItemOptions;
import com.codepresso.codepresso.order.repository.OrdersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 결제 서비스
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrdersRepository ordersRepository;
    private final TossPaymentClient tossPaymentClient;
    private final OrderCreationService orderCreationService;

    /**
     * Toss 결제 승인 및 주문/결제 저장 (프론트에서 호출)
     */
    @Transactional
    public CheckoutResponse processTossPaymentSuccess(TossPaymentSuccessRequest request) {
        // 1. 주문 생성 (장바구니/쿠폰/스탬프 처리 포함)
        Orders savedOrder = orderCreationService.createOrderFromPayment(request);

        TossPaymentConfirmResponse tossResponse = tossPaymentClient.getPaymentByPaymentKey(request.getPaymentKey());

        // 2. Payment 엔티티 저장 (Toss 결제 정보)
        Payment payment;
        if(tossResponse != null) {
            payment = createPaymentFromTossResponse(tossResponse, savedOrder);
        }else {
            payment = createPaymentFromRequest(request, savedOrder);
        }
        paymentRepository.save(payment);

        if (tossResponse != null && tossResponse.getCard() != null) {
            PaymentDetail detail = createPaymentDetail(tossResponse, payment);
            payment.setPaymentDetail(detail);
        }

        log.info("결제 저장 완료 - paymentKey: {}, orderId: {}",
                request.getPaymentKey(), savedOrder.getId());

        // 3. 응답 생성
        return buildCheckoutResponse(savedOrder);
    }

    /**
     * Toss API로 결제 승인 후 저장 (새로운 API 방식)
     */
    @Transactional
    public PaymentResponse confirmPayment(PaymentRequest request, Long memberId) {
        // 1. 중복 결제 체크
        if (paymentRepository.existsByPaymentKey(request.getPaymentKey())) {
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }

        // 2. Toss API로 결제 승인
        TossPaymentConfirmResponse tossResponse = tossPaymentClient.confirm(
                request.getPaymentKey(),
                request.getOrderId(),
                request.getAmount()
        );

        // 3. 주문 조회
        Long orderId = extractOrderId(request.getOrderId());
        Orders orders = ordersRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 4. 주문 소유자 검증
        if (!orders.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("본인의 주문만 결제할 수 있습니다.");
        }

        // 5. Payment 엔티티 저장
        Payment payment = createPaymentFromTossResponse(tossResponse, orders);
        paymentRepository.save(payment);

        // 6. 카드 결제인 경우 PaymentDetail 저장
        if (tossResponse.getCard() != null) {
            PaymentDetail detail = createPaymentDetail(tossResponse, payment);
            payment.setPaymentDetail(detail);
        }

        log.info("결제 승인 완료 - paymentKey: {}, orderId: {}",
                payment.getPaymentKey(), orderId);

        return PaymentResponse.from(payment);
    }

    /**
     * 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentId));
        return PaymentResponse.from(payment);
    }

    /**
     * 회원 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByMember(Long memberId) {
        return paymentRepository.findByOrdersMemberId(memberId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    /**
     * 주문 ID로 결제 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrdersId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문의 결제 정보를 찾을 수 없습니다: " + orderId));
        return PaymentResponse.from(payment);
    }

    // ========== Private Helper Methods ==========

    private Payment createPaymentFromRequest(TossPaymentSuccessRequest request, Orders orders) {
        return Payment.builder()
                .orders(orders)
                .paymentKey(request.getPaymentKey())
                .amount(request.getAmount())
                .status(PaymentStatus.DONE)
                .method(PaymentMethod.CARD)
                .approvedAt(LocalDateTime.now())
                .requestedAt(LocalDateTime.now())
                .build();
    }

    private Payment createPaymentFromTossResponse(TossPaymentConfirmResponse response, Orders orders) {
        return Payment.builder()
                .orders(orders)
                .paymentKey(response.getPaymentKey())
                .amount(response.getTotalAmount())
                .status(convertStatus(response.getStatus()))
                .method(convertMethod(response.getMethod()))
                .approvedAt(parseDateTime(response.getApprovedAt()))
                .requestedAt(parseDateTime(response.getRequestedAt()))
                .receiptUrl(response.getReceipt() != null ? response.getReceipt().getUrl() : null)
                .build();
    }

    private PaymentDetail createPaymentDetail(TossPaymentConfirmResponse response, Payment payment) {
        TossPaymentConfirmResponse.TossCard card = response.getCard();
        return PaymentDetail.builder()
                .payment(payment)
                .cardCompany(card.getCompany())
                .cardNumber(card.getNumber())
                .installmentMonths(card.getInstallmentPlanMonths())
                .cardType(card.getCardType())
                .ownerType(card.getOwnerType())
                .acquireStatus(card.getAcquireStatus())
                .build();
    }

    private PaymentStatus convertStatus(String status) {
        return switch (status) {
            case "READY" -> PaymentStatus.READY;
            case "IN_PROGRESS" -> PaymentStatus.IN_PROGRESS;
            case "DONE" -> PaymentStatus.DONE;
            case "CANCELED" -> PaymentStatus.CANCELED;
            case "PARTIAL_CANCELED" -> PaymentStatus.PARTIAL_CANCELED;
            default -> PaymentStatus.FAILED;
        };
    }

    private PaymentMethod convertMethod(String method) {
        return switch (method) {
            case "카드" -> PaymentMethod.CARD;
            case "계좌이체" -> PaymentMethod.TRANSFER;
            case "가상계좌" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "휴대폰" -> PaymentMethod.MOBILE;
            case "간편결제" -> PaymentMethod.EASY_PAY;
            default -> PaymentMethod.CARD;
        };
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private Long extractOrderId(String orderId) {
        try {
            return Long.parseLong(orderId);
        } catch (NumberFormatException e) {
            String[] parts = orderId.split("_");
            if (parts.length > 1) {
                return Long.parseLong(parts[parts.length - 1]);
            }
            throw new IllegalArgumentException("잘못된 주문 ID 형식입니다: " + orderId);
        }
    }

    private CheckoutResponse buildCheckoutResponse(Orders orders) {
        Orders fetchedOrders = ordersRepository.findByIdWithDetailsAndOptions(orders.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        List<CheckoutResponse.OrderItem> orderItems = new ArrayList<>();

        for (OrdersDetail detail : fetchedOrders.getOrdersDetails()) {
            List<String> optionNames = new ArrayList<>();
            if (detail.getOptions() != null) {
                for (OrdersItemOptions option : detail.getOptions()) {
                    optionNames.add(option.getOption().getOptionStyle().getOptionName().getOptionName());
                }
            }

            CheckoutResponse.OrderItem orderItem = CheckoutResponse.OrderItem.builder()
                    .orderDetailId(detail.getId())
                    .productName(detail.getProduct().getProductName())
                    .quantity(detail.getQuantity() != null ? detail.getQuantity() : 1)
                    .price(detail.getPrice())
                    .optionNames(optionNames)
                    .build();

            orderItems.add(orderItem);
        }

        int totalAmount = fetchedOrders.getOrdersDetails().stream()
                .mapToInt(OrdersDetail::getPrice)
                .sum();

        return CheckoutResponse.builder()
                .orderId(fetchedOrders.getId())
                .productionStatus(fetchedOrders.getProductionStatus())
                .orderDate(fetchedOrders.getOrderDate())
                .pickupTime(fetchedOrders.getPickupTime())
                .isTakeout(fetchedOrders.getIsTakeout())
                .requestNote(fetchedOrders.getRequestNote())
                .totalAmount(totalAmount)
                .orderItems(orderItems)
                .build();
    }
}
