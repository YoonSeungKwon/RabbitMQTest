package com.test.redisMqTest.service;

import com.test.redisMqTest.entity.Coupons;
import com.test.redisMqTest.entity.Members;
import com.test.redisMqTest.entity.MembersCoupon;
import com.test.redisMqTest.repository.CouponRepository;
import com.test.redisMqTest.repository.MemberRepository;
import com.test.redisMqTest.repository.MembersCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    @Value("${RABBITMQ_EXCHANGE_NAME}")
    private String exchangeName;

    @Value("${RABBITMQ_ROUTING_KEY}")
    private String routingKey;


    private final CouponRepository couponRepository;

    private final MemberRepository memberRepository;

    private final MembersCouponRepository membersCouponRepository;

    private final RabbitTemplate rabbitTemplate;

    private final RedisTemplate<String, Coupons> redisTemplate;


    @Transactional
    @CachePut(value = "coupons", key = "#result.couponId")
    public Coupons createCoupon(String name, int quantity){
        Coupons coupons = Coupons.builder()
                .couponName(name)
                .quantity(quantity)
                .build();

        return couponRepository.save(coupons);
    }


    @Transactional
    public String getCoupons(long memberId, long couponId) {

        Coupons coupons = redisTemplate.opsForValue().get("coupons::"+couponId);
        Members members = memberRepository.findMembersByMemberId(memberId);

        if(coupons == null){
            coupons = couponRepository.findCouponsByCouponId(couponId);
        }

        if(coupons.getQuantity() > 0){
            coupons.minusCoupon();
            MembersCoupon membersCoupon = MembersCoupon.builder()
                    .coupons(coupons)
                    .members(members)
                    .build();

            redisTemplate.opsForValue().set("coupons::"+couponId, coupons);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, membersCoupon);

            return coupons.getCouponName();
        }else{
            return "OUT OF ORDERS";
        }

    }


    @Transactional
    @RabbitListener(queues = "${RABBITMQ_QUEUE_NAME}")
    public void saveCoupon(MembersCoupon dto){
        Coupons coupons = couponRepository.findCouponsByCouponId(dto.getCoupons().getCouponId());
        if(coupons.getQuantity() <= 0)
            return;
        coupons.minusCoupon();
        couponRepository.save(coupons);
        membersCouponRepository.save(dto);
    }


}
