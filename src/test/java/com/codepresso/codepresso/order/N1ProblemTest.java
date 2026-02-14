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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class N1ProblemTest {

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
    @DisplayName("[N+1 발생] Fetch Join 없이 조회 - 쿼리 수 확인")
    void withoutFetchJoin_causeN1Problem() {
        // given
        statistics.clear();
        entityManager.clear();

        // when
        List<Orders> orders = ordersRepository.findByMemberIdWithoutFetchJoin(TEST_MEMBER_ID);

        // 연관 엔티티 접근
        for(Orders order : orders) {
            String branchName = order.getBranch().getBranchName();

            for(OrdersDetail detail : order.getOrdersDetails()) {
                String productName = detail.getProduct().getProductName();
            }
        }

        // then
        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("===========================================");
        System.out.println("[N+1 발생] Fetch Join 없이 조회");
        System.out.println("주문 수: " + orders.size());
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("===========================================");

        assertThat(queryCount).isGreaterThan(1);
    }

    @Test
    @DisplayName("[N+1 해결] Fetch Join으로 조회 - 쿼리 1개만 실행")
    void withFetchJoin_solvesN1Problem() {
        // given
        statistics.clear();
        entityManager.clear();  // 1차 캐시 비우기

        // when
        List<Orders> orders = ordersRepository.findByMemberIdWithFetchJoin(TEST_MEMBER_ID);

        // 연관 엔티티 접근 (추가 쿼리 없음!)
        for (Orders order : orders) {
            // Branch 접근 -> 이미 로딩됨
            String branchName = order.getBranch().getBranchName();

            // OrdersDetail 접근 -> 이미 로딩됨
            for (OrdersDetail detail : order.getOrdersDetails()) {
                // Product 접근 -> 이미 로딩됨
                String productName = detail.getProduct().getProductName();
            }
        }

        // then
        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("===========================================");
        System.out.println("[N+1 해결] Fetch Join으로 조회");
        System.out.println("주문 수: " + orders.size());
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("===========================================");

        // Fetch Join: 쿼리 1개만 실행되어야 함
        assertThat(queryCount).isEqualTo(1);
    }

    @Test
    @DisplayName("[비교] N+1 vs Fetch Join 쿼리 수 비교")
    void compareQueryCount() {
        // === N+1 발생 케이스 ===
        statistics.clear();
        entityManager.clear();

        List<Orders> ordersWithoutFetch = ordersRepository.findByMemberIdWithoutFetchJoin(TEST_MEMBER_ID);
        for (Orders order : ordersWithoutFetch) {
            order.getBranch().getBranchName();
            for (OrdersDetail detail : order.getOrdersDetails()) {
                detail.getProduct().getProductName();
            }
        }
        long queryCountWithoutFetch = statistics.getPrepareStatementCount();

        // === Fetch Join 케이스 ===
        statistics.clear();
        entityManager.clear();

        List<Orders> ordersWithFetch = ordersRepository.findByMemberIdWithFetchJoin(TEST_MEMBER_ID);
        for (Orders order : ordersWithFetch) {
            order.getBranch().getBranchName();
            for (OrdersDetail detail : order.getOrdersDetails()) {
                detail.getProduct().getProductName();
            }
        }
        long queryCountWithFetch = statistics.getPrepareStatementCount();

        // === 결과 출력 ===
        System.out.println("===========================================");
        System.out.println("          N+1 vs Fetch Join 비교");
        System.out.println("===========================================");
        System.out.println("주문 수: " + ordersWithoutFetch.size());
        System.out.println("-------------------------------------------");
        System.out.println("Fetch Join 없이: " + queryCountWithoutFetch + "개 쿼리");
        System.out.println("Fetch Join 적용: " + queryCountWithFetch + "개 쿼리");
        System.out.println("-------------------------------------------");
        System.out.println("절감된 쿼리 수: " + (queryCountWithoutFetch - queryCountWithFetch) + "개");
        System.out.println("===========================================");

        // 검증
        assertThat(queryCountWithFetch).isLessThan(queryCountWithoutFetch);
    }
}
