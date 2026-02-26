package com.codepresso.codepresso.payment.service;

import com.codepresso.codepresso.branch.entity.Branch;
import com.codepresso.codepresso.branch.repository.BranchRepository;
import com.codepresso.codepresso.cart.dto.CartResponse;
import com.codepresso.codepresso.cart.service.CartService;
import com.codepresso.codepresso.coupon.service.CouponService;
import com.codepresso.codepresso.coupon.service.StampService;
import com.codepresso.codepresso.member.entity.Member;
import com.codepresso.codepresso.member.repository.MemberRepository;
import com.codepresso.codepresso.order.repository.OrdersRepository;
import com.codepresso.codepresso.payment.dto.request.CheckoutRequest;
import com.codepresso.codepresso.order.entity.Orders;
import com.codepresso.codepresso.order.entity.OrdersDetail;
import com.codepresso.codepresso.order.entity.OrdersItemOptions;
import com.codepresso.codepresso.payment.dto.request.TossPaymentSuccessRequest;
import com.codepresso.codepresso.product.entity.Product;
import com.codepresso.codepresso.product.entity.ProductOption;
import com.codepresso.codepresso.product.repository.ProductOptionRepository;
import com.codepresso.codepresso.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 생성 로직을 담당하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreationService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final MemberRepository memberRepository;
    private final BranchRepository branchRepository;
    private final OrdersRepository ordersRepository;
    private final CartService cartService;
    private final CouponService couponService;
    private final StampService stampService;

    /**
     * 결제 성공 후 주문 생성
     */
    @Transactional
    public Orders createOrderFromPayment(TossPaymentSuccessRequest request) {
        // 회원 지점 조회
        Member member = findMember(request.getMemberId());
        Branch branch = findBranch(request.getBranchId());

        // 주문 생성
        Orders orders = buildOrder(request, member, branch);

        // 주문 상세 생성
        List<CheckoutRequest.OrderItem> checkoutItems = request.getOrderItems().stream()
                .map(this::convertToCheckoutOrderItem)
                .collect(Collectors.toList());
        List<OrdersDetail> ordersDetails = createOrderDetails(checkoutItems, orders);
        orders.setOrdersDetails(ordersDetails);

        // 주문 저장
        Orders savedOrder = ordersRepository.save(orders);

        // 후처리
        processPostOrderActions(request, member, ordersDetails);

        log.info("주문 생성 완료 - orderId: {}, memberId: {}", savedOrder.getId(), member.getId());

        return savedOrder;
    }

    /**
     * 주문 상세 생성
     */
    public List<OrdersDetail> createOrderDetails(List<CheckoutRequest.OrderItem> orderItems, Orders orders) {
        List<OrdersDetail> orderDetails = new ArrayList<>();

        int totalBeforeDiscount = orderItems.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        int totalDiscount = orders.getDiscountAmount() != null ? orders.getDiscountAmount() : 0;
        double discountRate = totalBeforeDiscount > 0 ? (double) totalDiscount / totalBeforeDiscount : 0;

        int accumulatedDiscount = 0;

        for (int i = 0; i < orderItems.size(); i++) {
            CheckoutRequest.OrderItem item = orderItems.get(i);
            Product product = findProduct(item.getProductId());

            int itemOriginalPrice = item.getPrice() * item.getQuantity();
            int itemDiscount = calculateItemDiscount(i, orderItems.size(), itemOriginalPrice,
                    discountRate, totalDiscount, accumulatedDiscount);
            accumulatedDiscount += itemDiscount;

            int discountedPrice = itemOriginalPrice - itemDiscount;

            OrdersDetail ordersDetail = OrdersDetail.builder()
                    .orders(orders)
                    .product(product)
                    .price(discountedPrice)
                    .quantity(item.getQuantity())
                    .build();

            if (item.getOptionIds() != null && !item.getOptionIds().isEmpty()) {
                List<OrdersItemOptions> options = createOrderItemOptions(item.getOptionIds(), ordersDetail);
                ordersDetail.setOptions(options);
            }

            orderDetails.add(ordersDetail);
        }

        return orderDetails;
    }

    /**
     * 주문 아이템 옵션 생성
     */
    public List<OrdersItemOptions> createOrderItemOptions(List<Long> optionIds, OrdersDetail orderDetail) {
        List<OrdersItemOptions> orderItemOptions = new ArrayList<>();

        for (Long optionId : optionIds) {
            ProductOption productOption = productOptionRepository.findById(optionId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다: " + optionId));

            OrdersItemOptions orderItemOption = OrdersItemOptions.builder()
                    .option(productOption)
                    .ordersDetail(orderDetail)
                    .build();

            orderItemOptions.add(orderItemOption);
        }

        return orderItemOptions;
    }

    // ========== Private Helper Methods ==========

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + memberId));
    }

    private Branch findBranch(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점입니다: " + branchId));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
    }

    private Orders buildOrder(TossPaymentSuccessRequest request, Member member, Branch branch) {
        LocalDateTime pickupTime = parsePickupTime(request.getPickupTime());

        int discountAmount = calculateDiscountAmount(request);
        int finalAmount = request.getAmount() != null ? request.getAmount() : 0;
        int totalAmount = finalAmount + discountAmount;

        return Orders.builder()
                .member(member)
                .branch(branch)
                .productionStatus("픽업완료")
                .isTakeout(request.getIsTakeout())
                .pickupTime(pickupTime)
                .orderDate(LocalDateTime.now())
                .requestNote(request.getRequestNote())
                .isPickup(true)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    private LocalDateTime parsePickupTime(String pickupTimeStr) {
        if (pickupTimeStr == null || pickupTimeStr.isEmpty()) {
            return LocalDateTime.now().plusMinutes(5);
        }
        try {
            return LocalDateTime.parse(pickupTimeStr);
        } catch (Exception e) {
            return LocalDateTime.now().plusMinutes(5);
        }
    }

    private int calculateDiscountAmount(TossPaymentSuccessRequest request) {
        if (Boolean.TRUE.equals(request.getUseCoupon()) && request.getDiscountAmount() != null) {
            return request.getDiscountAmount();
        }
        return 0;
    }

    private int calculateItemDiscount(int index, int totalSize, int itemOriginalPrice,
                                      double discountRate, int totalDiscount, int accumulatedDiscount) {
        if (index == totalSize - 1) {
            return totalDiscount - accumulatedDiscount;
        }
        return (int) Math.round(itemOriginalPrice * discountRate);
    }

    private CheckoutRequest.OrderItem convertToCheckoutOrderItem(TossPaymentSuccessRequest.OrderItem tossItem) {
        return CheckoutRequest.OrderItem.builder()
                .productId(tossItem.getProductId())
                .quantity(tossItem.getQuantity())
                .price(tossItem.getPrice())
                .optionIds(tossItem.getOptionIds())
                .build();
    }

    /**
     * 주문 후 처리 (장바구니 비우기, 쿠폰 사용, 스탬프 적립)
     */
    private void processPostOrderActions(TossPaymentSuccessRequest request, Member member,
                                         List<OrdersDetail> ordersDetails) {
        // 장바구니 비우기
        if (Boolean.TRUE.equals(request.getIsFromCart())) {
            clearCart(member.getId());
        }

        // 쿠폰 사용 처리
        if (Boolean.TRUE.equals(request.getUseCoupon()) &&
                request.getDiscountAmount() != null && request.getDiscountAmount() > 0) {
            useCoupon(member.getId());
        }

        // 스탬프 적립
        earnStamps(member.getId(), ordersDetails);
    }

    private void clearCart(Long memberId) {
        try {
            CartResponse cartData = cartService.getCartByMemberId(memberId);
            cartService.clearCart(memberId, cartData.getCartId());
            log.info("장바구니 비우기 성공 - memberId: {}", memberId);
        } catch (Exception e) {
            log.error("장바구니 비우기 실패 - memberId: {}, error: {}", memberId, e.getMessage());
        }
    }

    private void useCoupon(Long memberId) {
        try {
            var validCoupons = couponService.getMemberValidCoupons(memberId);
            if (!validCoupons.isEmpty()) {
                Long couponId = validCoupons.get(0).getCouponId();
                couponService.useCoupon(couponId);
                log.info("쿠폰 사용 성공 - memberId: {}, couponId: {}", memberId, couponId);
            }
        } catch (Exception e) {
            log.error("쿠폰 사용 실패 - memberId: {}, error: {}", memberId, e.getMessage());
        }
    }

    private void earnStamps(Long memberId, List<OrdersDetail> ordersDetails) {
        try {
            stampService.earnStampsFromOrder(memberId, ordersDetails);
            log.info("스탬프 적립 성공 - memberId: {}", memberId);
        } catch (Exception e) {
            log.error("스탬프 적립 실패 - memberId: {}, error: {}", memberId, e.getMessage());
        }
    }
}
