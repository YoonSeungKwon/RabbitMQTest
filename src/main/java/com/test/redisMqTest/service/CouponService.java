package com.test.redisMqTest.service;

import com.test.redisMqTest.entity.Coupons;
import com.test.redisMqTest.entity.Members;
import com.test.redisMqTest.entity.MembersCoupon;
import com.test.redisMqTest.repository.CouponRepository;
import com.test.redisMqTest.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CouponService {

    @Value("${RABBITMQ_EXCHANGE_NAME}")
    private String exchangeName;

    @Value("${RABBITMQ_ROUTING_KEY}")
    private String routingKey;


    private final CouponRepository couponRepository;

    private final MemberRepository memberRepository;

    private final RabbitTemplate rabbitTemplate;

    private final RedissonClient redissonClient;


    @Transactional
    public Coupons createCoupon(String name, int quantity){
        Coupons coupons = Coupons.builder()
                .couponName(name)
                .quantity(quantity)
                .build();

        couponRepository.save(coupons);

        RBucket<Coupons> couponsRBucket = redissonClient.getBucket("coupons::"+coupons.getCouponId());
        couponsRBucket.set(coupons, Duration.ofMinutes(10L));

        return coupons;
    }


    @Transactional
    public String getCoupons(long memberId, long couponId) {

        RLock rlock = redissonClient.getLock("coupons"+couponId);


        try {
            boolean available = rlock.tryLock(5L, 3L, TimeUnit.SECONDS);

            if(!available)
                return "서버 요청이 많습니다";

            RBucket<Coupons> rBucket = redissonClient.getBucket("coupons::" + couponId);
            Coupons coupons = rBucket.get();


            if (coupons == null) {
                coupons = couponRepository.findCouponsByCouponId(couponId);
            }


            if (coupons.getQuantity() > 0) {

                Members members = memberRepository.findMembersByMemberId(memberId);

                coupons.minusCoupon();
                rBucket.set(coupons, Duration.ofMinutes(10L));

                MembersCoupon membersCoupon = MembersCoupon.builder()
                        .coupons(coupons)
                        .members(members)
                        .build();

                rabbitTemplate.convertAndSend(exchangeName, routingKey, membersCoupon);

                return coupons.getCouponName();
            } else {
                return "OUT OF ORDERS";
            }
        }catch (InterruptedException e){
            return "응답 시간 초과";
        }finally {
            if (rlock.isHeldByCurrentThread()) {
                rlock.unlock();
            }
        }

    }


}
