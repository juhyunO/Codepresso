package com.codepresso.codepresso.coupon;

import com.codepresso.codepresso.coupon.entity.CouponType;
import com.codepresso.codepresso.coupon.entity.MemberCoupon;
import com.codepresso.codepresso.coupon.repository.CouponTypeRepository;
import com.codepresso.codepresso.coupon.repository.MemberCouponRepository;
import com.codepresso.codepresso.coupon.service.CouponService;
import com.codepresso.codepresso.member.entity.Member;
import com.codepresso.codepresso.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private MemberCouponRepository memberCouponRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CouponTypeRepository couponTypeRepository;

    private MemberCoupon testCoupon;

    @BeforeEach
    void setUp() {
        // 테스트용 회원
        Member member = memberRepository.save(
                Member.builder()
                        .accountId("coupon-test-" + System.currentTimeMillis())
                        .email("coupon-test-" + System.currentTimeMillis() + "@test.com")
                        .nickname("쿠폰테스트" + System.currentTimeMillis())
                        .password("password")
                        .build()
        );

        // 테스트용 쿠폰 타입
        CouponType couponType = couponTypeRepository.findAll().iterator().next();

        // 테스트용 쿠폰 생성
        testCoupon = memberCouponRepository.save(
                MemberCoupon.builder()
                .member(member)
                .couponType(couponType)
                .issuedDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusMonths(6))
                .status(MemberCoupon.CouponStatus.UNUSED)
                .build()
        );
    }

    @Test
    @DisplayName("비관적 락 : 10개 스레드가 동시에 같은 쿠폰 사용 시도 - 1개만 성공")
    void pessimisticLock_preventDoubleCouponUse() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for(int i=0; i<threadCount; i++) {
            executorService.submit(() -> {
                try {
                    couponService.useCoupon(testCoupon.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        System.out.println("성공 : " + successCount.get() + ", 실패 : " + failCount.get());
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
    }
}
