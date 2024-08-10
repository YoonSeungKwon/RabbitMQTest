package com.test.redisMqTest.repository;

import com.test.redisMqTest.entity.Coupons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupons, Long> {

    Coupons findCouponsByCouponId(long id);

}
