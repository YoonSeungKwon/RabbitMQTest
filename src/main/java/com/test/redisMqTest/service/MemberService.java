package com.test.redisMqTest.service;

import com.test.redisMqTest.entity.Coupons;
import com.test.redisMqTest.entity.Members;
import com.test.redisMqTest.entity.MembersCoupon;
import com.test.redisMqTest.repository.CouponRepository;
import com.test.redisMqTest.repository.MemberRepository;
import com.test.redisMqTest.repository.MembersCouponRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final int BATCH_SIZE = 50;
    private static final int SAVE_INTERVAL = 5000;
    private final List<MembersCoupon> list = new ArrayList<>();

    private final CouponRepository couponRepository;

    private final MembersCouponRepository membersCouponRepository;

    @Transactional
    @RabbitListener(queues = "${RABBITMQ_QUEUE_NAME}")
    public void saveMemberCoupons(MembersCoupon dto){
        list.add(dto);

        if(list.size() == BATCH_SIZE){
            saveAllCoupons();
        }
    }

    @Transactional
    public void saveAllCoupons(){
        List<MembersCoupon> batch = new ArrayList<>();
        for(MembersCoupon mc : list){
            Coupons coupons = couponRepository.findCouponsByCouponId(mc.getCoupons().getCouponId());
            if(coupons.getQuantity() <= 0)
                continue;
            coupons.minusCoupon();
            couponRepository.save(coupons);
            batch.add(mc);
        }
        membersCouponRepository.saveAll(batch);
        list.clear();
    }

    @Scheduled(fixedRate = SAVE_INTERVAL)
    @Transactional
    public void saveRemain(){
        if(!list.isEmpty()){
            saveAllCoupons();
        }
    }

    @PreDestroy
    @Transactional
    public void cleanUp(){
        saveRemain();
    }


}
