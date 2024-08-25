package com.test.redisMqTest;

import com.test.redisMqTest.entity.Coupons;
import com.test.redisMqTest.repository.CouponRepository;
import com.test.redisMqTest.repository.MemberRepository;
import com.test.redisMqTest.repository.MembersCouponRepository;
import com.test.redisMqTest.service.CouponService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class RedisMqTestApplicationTests {

	@Autowired
	CouponService couponService;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	CouponRepository couponRepository;

	@Autowired
	MembersCouponRepository membersCouponRepository;

	@Autowired
	RedissonClient redissonClient;

	@Autowired
	RabbitAdmin rabbitAdmin;

	@Value("${RABBITMQ_QUEUE_NAME}")
	String queueName;

	@Test
	public void test() throws InterruptedException {

		int test = 600;

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failureCount = new AtomicInteger();
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		CountDownLatch countDownLatch = new CountDownLatch(test);

		long index = couponService.createCoupon("선착순 1000명 20% 할인 쿠폰", 500).getCouponId();

		Random random = new Random();


		for(int i=0; i<test; i++){
			executorService.execute(()->{
				try{
					String result = couponService.getCoupons(1001+ random.nextInt(3900), index);
					if(result.equals("OUT OF ORDERS"))
						failureCount.incrementAndGet();
					else
						successCount.incrementAndGet();
					System.out.println(result);
				}catch (Exception e){
					failureCount.incrementAndGet();
				}finally {
					countDownLatch.countDown();
				}
			});

		}
		countDownLatch.await();

		QueueInformation queueInfo = rabbitAdmin.getQueueInfo(queueName);


		System.out.println("Success : "+successCount);
		System.out.println("Failure : "+failureCount);

	}

	@Test
	void redissonTest(){

		Coupons coupons = couponService.createCoupon("temp2", 100);
		RBucket<Coupons> rBucket = redissonClient.getBucket("coupons::"+coupons.getCouponId());
		rBucket.set(coupons, Duration.ofMinutes(1));
//
//		RBucket<Coupons> bucket = redissonClient.getBucket("coupons::"+24);

//		bucket.set(coupons);

//		Coupons coupons = bucket.get();

//		System.out.println(coupons.toString());

	}

	@Test
	void messageCountTest(){

		QueueInformation queueInfo = rabbitAdmin.getQueueInfo(queueName);

		if(queueInfo == null)
			System.out.println("남은 메시지: 0개");
		else
			System.out.println("남은 메시지: " + queueInfo.getMessageCount()+"개");
	}

}
