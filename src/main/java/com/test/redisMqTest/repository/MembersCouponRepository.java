package com.test.redisMqTest.repository;

import com.test.redisMqTest.entity.MembersCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembersCouponRepository extends JpaRepository<MembersCoupon, Long> {
}
