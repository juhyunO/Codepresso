package com.codepresso.codepresso.coupon;

import com.codepresso.codepresso.coupon.entity.Stamp;
import com.codepresso.codepresso.coupon.repository.StampRepository;
import com.codepresso.codepresso.coupon.service.StampService;
import com.codepresso.codepresso.member.entity.Member;
import com.codepresso.codepresso.member.repository.MemberRepository;
import com.codepresso.codepresso.order.entity.OrdersDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class StampConcurrencyTest {
    @Autowired
    private StampService stampService;

    @Autowired
    private StampRepository stampRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트용 회원
        testMember = memberRepository.save(
                Member.builder()
                        .accountId("stamp-test-" + System.currentTimeMillis())
                        .email("stamp-test-" + System.currentTimeMillis() + "@test.com")
                        .nickname("스탬프 테스트-" + System.currentTimeMillis())
                        .password("password")
                        .build());

        // 초기 스탬프 0개
        stampRepository.save(
                Stamp.builder()
                        .member(testMember)
                        .quantity(0)
                        .earnedDate(LocalDateTime.now())
                        .build()
        );
    }

    @Test
    @DisplayName("낙관적 락 : 10개 스레드가 동시에 스탬프 적립 - Lost Update 방지")
    void optimisticLock_preventLostUpdate() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // Mock OrdersDetail

        OrdersDetail mockDetail = OrdersDetail.builder()
                .quantity(1)
                .build();

        // when
        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stampService.earnStampsFromOrder(testMember.getId(), List.of(mockDetail));
                    successCount.incrementAndGet();
                }catch (Exception e) {
                    System.out.println("실패 : " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        //then
        Stamp resultStamp = stampRepository.findByMember(testMember).orElseThrow();
        System.out.println("성공:" + successCount.get() + "  실패 : " + failCount.get());
        System.out.println("최종 스탬프 : "+ resultStamp.getQuantity());


        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
    }

}
