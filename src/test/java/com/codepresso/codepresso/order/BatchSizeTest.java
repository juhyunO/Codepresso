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

import java.util.List;

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
    @DisplayName("@BatchSize 미적용: N+1 발생 확인")
    void withoutBatchSize_N1Problem() {
        // 현재 @BatchSize가 주석 처리된 상태에서 실행
        // N+1 문제가 실제로 발생하는지 확인

        statistics.clear();
        entityManager.clear();

        // fetch join 없이 조회 (N+1 발생)
        List<Orders> orders = ordersRepository.findByMemberIdWithoutFetchJoin(TEST_MEMBER_ID);

        int orderCount = orders.size();

        // 모든 연관 엔티티 접근 → N+1 발생!
        int totalDetails = 0;
        for (Orders order : orders) {
            // Branch 접근 → 추가 쿼리
            order.getBranch().getBranchName();

            // OrdersDetail 접근 → 추가 쿼리
            for (OrdersDetail detail : order.getOrdersDetails()) {
                // Product 접근 → 추가 쿼리
                detail.getProduct().getProductName();
                totalDetails++;
            }
        }

        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("===========================================");
        System.out.println("     @BatchSize 미적용 - N+1 발생");
        System.out.println("===========================================");
        System.out.println("조회된 주문 수: " + orderCount);
        System.out.println("총 주문 상세 수: " + totalDetails);
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("===========================================");

        // N+1 발생 시 쿼리 수가 많아야 함
        assertThat(queryCount).isGreaterThan(orderCount);
    }

    @Test
    @DisplayName("@BatchSize 적용: IN 절로 최적화")
    void withBatchSize_optimized() {
        // @BatchSize 적용 후 실행하면 쿼리 수가 줄어듦
        // 엔티티에서 @BatchSize 주석 해제 후 테스트

        statistics.clear();
        entityManager.clear();

        // fetch join 없이 조회
        List<Orders> orders = ordersRepository.findByMemberIdWithoutFetchJoin(TEST_MEMBER_ID);

        int orderCount = orders.size();

        // 모든 연관 엔티티 접근
        int totalDetails = 0;
        for (Orders order : orders) {
            order.getBranch().getBranchName();

            for (OrdersDetail detail : order.getOrdersDetails()) {
                detail.getProduct().getProductName();
                totalDetails++;
            }
        }

        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("===========================================");
        System.out.println("     @BatchSize 적용 - IN 절 최적화");
        System.out.println("===========================================");
        System.out.println("조회된 주문 수: " + orderCount);
        System.out.println("총 주문 상세 수: " + totalDetails);
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("-------------------------------------------");
        System.out.println("@BatchSize 효과: IN 절로 묶어서 조회");
        System.out.println("===========================================");
    }
}