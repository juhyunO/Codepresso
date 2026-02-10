package com.codepresso.codepresso.review.repository;

import com.codepresso.codepresso.review.dto.MyReviewProjection;
import com.codepresso.codepresso.order.entity.OrdersDetail;
import com.codepresso.codepresso.product.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.ordersDetail od WHERE od.product.id = :productId")
    List<Review> findByProductReviews(@Param("productId") Long productId);

    boolean existsByOrdersDetail(OrdersDetail ordersDetail);

    @Query("SELECT AVG(r.rating) FROM Review r LEFT JOIN r.ordersDetail od WHERE od.product.id = :productId")
    Double getAverageRatingByProduct(Long productId);

    @Query("select " +
            "r.id as id, r.photoUrl as photoUrl, r.content as content, r.rating as rating, r.createdAt as createdAt, " +
            "p.id as productId, p.productName as productName, p.productPhoto as productPhoto, " +
            "b.branchName as branchName, os.orderDate as orderDate " +
            "from Review r " +
            "join r.ordersDetail od " +
            "join od.orders os " +
            "join os.branch b " +
            "join od.product p " +
            "where r.member.id = :memberId " +
            "order by r.createdAt desc")
    List<MyReviewProjection> findByMemberId(@Param("memberId") Long memberId);


}