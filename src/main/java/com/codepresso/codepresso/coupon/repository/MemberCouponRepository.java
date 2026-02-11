package com.codepresso.codepresso.coupon.repository;

import com.codepresso.codepresso.coupon.entity.MemberCoupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository extends CrudRepository<MemberCoupon, Long> {

    // 사용 가능한 쿠폰 개수 조회
    int countByMemberIdAndStatusAndExpiryDateAfter(Long memberId, MemberCoupon.CouponStatus couponStatus, LocalDateTime now);

    // 사용 가능한 쿠폰 목록 조회
    List<MemberCoupon> findByMemberIdAndStatusAndExpiryDateAfter(Long memberId, MemberCoupon.CouponStatus couponStatus, LocalDateTime now);

    /**
     * 비관적 락으로 쿠폰 조회
     * 다른 트랜잭션이 이 쿠폰에 접근하려면 대기해야 함
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mc FROM MemberCoupon mc WHERE mc.id = :couponId")
    Optional<MemberCoupon> findByIdWithPessimisticLock(@Param("couponId") Long couponId);
}
