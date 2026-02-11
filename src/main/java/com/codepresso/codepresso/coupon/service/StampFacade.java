package com.codepresso.codepresso.coupon.service;

import com.codepresso.codepresso.order.entity.OrdersDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StampFacade {

    private final StampService stampService;

    /**
     * 재시도 전용 파사드
     * - 트랜잭션 없음
     * - 재시도마다 StampService 새 트랜잭션 시작
     */
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 10,
            backoff = @Backoff(delay = 50, multiplier = 2, maxDelay = 1000)
    )
    public void earnStampsWithRetry(Long memberId, List<OrdersDetail> ordersDetails) {
        stampService.earnStampsFromOrder(memberId, ordersDetails);
    }
}
