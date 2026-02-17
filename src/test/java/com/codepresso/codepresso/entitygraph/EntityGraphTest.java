package com.codepresso.codepresso.entitygraph;

import com.codepresso.codepresso.cart.entity.CartItem;
import com.codepresso.codepresso.cart.entity.CartOption;
import com.codepresso.codepresso.cart.repository.CartItemRepository;
import com.codepresso.codepresso.product.entity.Product;
import com.codepresso.codepresso.product.repository.ProductRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EntityGraphTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private EntityManager entityManager;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        SessionFactory sessionFactory = entityManager
                .getEntityManagerFactory()
                .unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
    }

    // ==================== 상품 상세 조회 ====================

    @Test
    @DisplayName("[미적용] 상품 조회 - 기본 findById")
    void product_withoutEntityGraph() {
        // given
        Long productId = 1L;
        statistics.clear();
        entityManager.clear();

        // when - @EntityGraph 없는 기본 메서드 사용
        Product product = productRepository.findById(productId).orElse(null);

        // 연관 엔티티 접근 → N+1 발생!
        if (product != null) {
            if (product.getNutritionInfo() != null) {
                product.getNutritionInfo().getCalories();
            }
            if (product.getCategory() != null) {
                product.getCategory().getCategoryName();
            }
            product.getHashtags().size();
        }

        // then
        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("===========================================");
        System.out.println("  [미적용] 상품 조회 - findById()");
        System.out.println("===========================================");
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("===========================================");
    }

    @Test
    @DisplayName("[적용] 상품 조회 - @EntityGraph")
    void product_withEntityGraph() {
        // given
        Long productId = 1L;
        statistics.clear();
        entityManager.clear();

        // when - @EntityGraph 적용된 메서드 사용
        Product product = productRepository.findProductById(productId);

        // 연관 엔티티 접근 → 추가 쿼리 없음!
        if (product != null) {
            if (product.getNutritionInfo() != null) {
                product.getNutritionInfo().getCalories();
            }
            if (product.getCategory() != null) {
                product.getCategory().getCategoryName();
            }
            product.getHashtags().size();
        }

        // then
        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("===========================================");
        System.out.println("  [적용] 상품 조회 - @EntityGraph");
        System.out.println("===========================================");
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("===========================================");
    }

    // ==================== 장바구니 아이템 조회 ====================

    @Test
    @DisplayName("[미적용] 장바구니 조회 - 기본 findByCartId")
    void cartItem_withoutEntityGraph() {
        // given
        Long cartId = 1L;
        statistics.clear();
        entityManager.clear();

        // when - @EntityGraph 없는 메서드 사용
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);

        // 연관 엔티티 접근 → N+1 발생!
        for (CartItem cartItem : cartItems) {
            cartItem.getProduct().getProductName();
            for (CartOption option : cartItem.getOptions()) {
                if (option.getProductOption() != null &&
                    option.getProductOption().getOptionStyle() != null) {
                    option.getProductOption().getOptionStyle().getOptionStyle();
                }
            }
        }

        // then
        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("===========================================");
        System.out.println("  [미적용] 장바구니 조회 - findByCartId()");
        System.out.println("===========================================");
        System.out.println("조회된 아이템 수: " + cartItems.size());
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("===========================================");
    }

    @Test
    @DisplayName("[적용] 장바구니 조회 - @EntityGraph (4단계 중첩)")
    void cartItem_withEntityGraph() {
        // given
        Long cartId = 1L;
        Long productId = 4L;  // 실제 데이터가 있는 product_id
        statistics.clear();
        entityManager.clear();

        // when - @EntityGraph 적용된 메서드 사용
        List<CartItem> cartItems = cartItemRepository.findByCart_IdAndProduct_Id(cartId, productId);

        // 연관 엔티티 접근 → 추가 쿼리 없음!
        for (CartItem cartItem : cartItems) {
            cartItem.getProduct().getProductName();
            for (CartOption option : cartItem.getOptions()) {
                if (option.getProductOption() != null &&
                    option.getProductOption().getOptionStyle() != null) {
                    option.getProductOption().getOptionStyle().getOptionStyle();
                }
            }
        }

        // then
        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("===========================================");
        System.out.println("  [적용] 장바구니 조회 - @EntityGraph");
        System.out.println("===========================================");
        System.out.println("조회된 아이템 수: " + cartItems.size());
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("===========================================");
    }

    // ==================== 비교 테스트 ====================

    @Test
    @DisplayName("[비교] @EntityGraph 미적용 vs 적용")
    void compareEntityGraphEffect() {
        Long productId = 1L;
        Long cartId = 1L;

        // === 상품: 미적용 ===
        statistics.clear();
        entityManager.clear();
        Product product1 = productRepository.findById(productId).orElse(null);
        if (product1 != null) {
            if (product1.getNutritionInfo() != null) product1.getNutritionInfo().getCalories();
            if (product1.getCategory() != null) product1.getCategory().getCategoryName();
            product1.getHashtags().size();
        }
        long productWithout = statistics.getPrepareStatementCount();

        // === 상품: 적용 ===
        statistics.clear();
        entityManager.clear();
        Product product2 = productRepository.findProductById(productId);
        if (product2 != null) {
            if (product2.getNutritionInfo() != null) product2.getNutritionInfo().getCalories();
            if (product2.getCategory() != null) product2.getCategory().getCategoryName();
            product2.getHashtags().size();
        }
        long productWith = statistics.getPrepareStatementCount();

        // === 장바구니: 미적용 ===
        statistics.clear();
        entityManager.clear();
        List<CartItem> items1 = cartItemRepository.findByCartId(cartId);
        for (CartItem item : items1) {
            item.getProduct().getProductName();
            for (CartOption opt : item.getOptions()) {
                if (opt.getProductOption() != null && opt.getProductOption().getOptionStyle() != null) {
                    opt.getProductOption().getOptionStyle().getOptionStyle();
                }
            }
        }
        long cartWithout = statistics.getPrepareStatementCount();

        // === 장바구니: 적용 ===
        statistics.clear();
        entityManager.clear();
        List<CartItem> items2 = cartItemRepository.findByCart_IdAndProduct_Id(cartId, 4L);
        for (CartItem item : items2) {
            item.getProduct().getProductName();
            for (CartOption opt : item.getOptions()) {
                if (opt.getProductOption() != null && opt.getProductOption().getOptionStyle() != null) {
                    opt.getProductOption().getOptionStyle().getOptionStyle();
                }
            }
        }
        long cartWith = statistics.getPrepareStatementCount();

        // === 결과 출력 ===
        System.out.println("===========================================");
        System.out.println("     @EntityGraph 미적용 vs 적용 비교");
        System.out.println("===========================================");
        System.out.println();
        System.out.println("[상품 상세 조회]");
        System.out.println("  미적용 (findById): " + productWithout + "개 쿼리");
        System.out.println("  적용 (@EntityGraph): " + productWith + "개 쿼리");
        System.out.println("  → " + (productWithout - productWith) + "개 절감");
        System.out.println();
        System.out.println("[장바구니 아이템 조회]");
        System.out.println("  미적용 (findByCartId): " + cartWithout + "개 쿼리");
        System.out.println("  적용 (@EntityGraph): " + cartWith + "개 쿼리");
        System.out.println("  → " + (cartWithout - cartWith) + "개 절감");
        System.out.println("===========================================");
    }
}
