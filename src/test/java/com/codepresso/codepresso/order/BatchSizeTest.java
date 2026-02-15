package com.codepresso.codepresso.order;

import com.codepresso.codepresso.order.entity.Orders;
import com.codepresso.codepresso.order.entity.OrdersDetail;
import com.codepresso.codepresso.order.repository.OrdersRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BatchSizeTest {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private EntityManager entityManager;

    private Statistics statistics;
    private final Long TEST_MEMBER_ID = 9L;

    @BeforeEach
    void setUp() {
        SessionFactory sessionFactory = entityManager
                .getEntityManagerFactory()
                .unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
    }

    @Test
    @DisplayName("@BatchSize 효과: 페이징 + IN 절 조회")
    void batchSize_withPaging() {
        // given
        statistics.clear();
        entityManager.clear();

        // 10건씩 페이징 조회
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<Orders> ordersPage = ordersRepository.findByMemberIdWithPaging(TEST_MEMBER_ID, pageRequest);

        // 연관 엔티티 접근 (BatchSize로 IN 절 조회)
        for (Orders order : ordersPage.getContent()) {
            // OrdersDetail 접근
            for (OrdersDetail detail : order.getOrdersDetails()) {
                // Product 접근
                detail.getProduct().getProductName();
            }
        }

        // then
        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("===========================================");
        System.out.println("[@BatchSize 테스트 - 페이징 10건]");
        System.out.println("조회된 주문 수: " + ordersPage.getContent().size());
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("===========================================");

        // BatchSize 적용 시: 1(주문) + 1(상세 IN절) + α(상품)
        // N+1이면: 1 + 10 + α
        // BatchSize 효과로 쿼리 수가 크게 줄어야 함
        assertThat(queryCount).isLessThan(ordersPage.getContent().size() + 5);
    }

    @Test
    @DisplayName("@BatchSize vs 미적용 비교")
    void compareBatchSizeEffect() {
        // 현재 프로젝트는 @BatchSize가 이미 적용되어 있음
        // 적용 효과를 보여주기 위한 테스트

        statistics.clear();
        entityManager.clear();

        // 전체 조회 (BatchSize 적용된 상태)
        Page<Orders> ordersPage = ordersRepository.findByMemberIdWithPaging(
                TEST_MEMBER_ID,
                PageRequest.of(0, 50)  // 50건 조회
        );

        int orderCount = ordersPage.getContent().size();

        // 모든 연관 엔티티 접근
        int totalDetails = 0;
        for (Orders order : ordersPage.getContent()) {
            for (OrdersDetail detail : order.getOrdersDetails()) {
                detail.getProduct().getProductName();
                totalDetails++;
            }
        }

        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("===========================================");
        System.out.println("        @BatchSize 효과 분석");
        System.out.println("===========================================");
        System.out.println("조회된 주문 수: " + orderCount);
        System.out.println("총 주문 상세 수: " + totalDetails);
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("-------------------------------------------");
        System.out.println("[@BatchSize 미적용 시 쿼리 수]");
        System.out.println("  1 + " + orderCount + " + " + totalDetails + " = " +
                (1 + orderCount + totalDetails) + "개 쿼리");
//        System.out.println("[@BatchSize 적용 결과]");
//        System.out.println("  " + queryCount + "개 쿼리");
//        System.out.println("-------------------------------------------");
//        System.out.println("절감된 쿼리 수: " + ((1 + orderCount + totalDetails) - queryCount) + "개");
        System.out.println("===========================================");
    }
}