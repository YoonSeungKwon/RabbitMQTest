package com.test.redisMqTest.service;

import com.rabbitmq.client.Channel;
import com.test.redisMqTest.entity.Coupons;
import com.test.redisMqTest.entity.Members;
import com.test.redisMqTest.entity.MembersCoupon;
import com.test.redisMqTest.repository.CouponRepository;
import com.test.redisMqTest.repository.MemberRepository;
import com.test.redisMqTest.repository.MembersCouponRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final int BATCH_SIZE = 50;
    private static final int SAVE_INTERVAL = 5000;
    private final ConcurrentLinkedQueue<MembersCoupon> queue = new ConcurrentLinkedQueue<>();

    private final CouponRepository couponRepository;

    private final MembersCouponRepository membersCouponRepository;

    @Transactional
    @RabbitListener(queues = "${RABBITMQ_QUEUE_NAME}", ackMode = "MANUAL")
    public void saveMemberCoupons(MembersCoupon dto, Channel channel, Message message) throws IOException {
        try {
            queue.add(dto);

            if(queue.size() == BATCH_SIZE){
                saveAllCoupons();
                queue.clear();
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    @Transactional
    public void saveAllCoupons(){
        List<MembersCoupon> batch = new ArrayList<>();
        for(MembersCoupon mc : queue){
            Coupons coupons = couponRepository.findCouponsByCouponId(mc.getCoupons().getCouponId());
            if(coupons.getQuantity() <= 0)
                continue;
            coupons.minusCoupon();
            couponRepository.save(coupons);
            batch.add(mc);
        }
        membersCouponRepository.saveAll(batch);
    }

    @Scheduled(fixedRate = SAVE_INTERVAL)
    @Transactional
    public void saveRemain(){
        if(!queue.isEmpty()){
            saveAllCoupons();
        }
    }

    @PreDestroy
    @Transactional
    public void cleanUp(){
        saveRemain();
    }


}
