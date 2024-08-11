package com.test.redisMqTest;

import com.test.redisMqTest.entity.Coupons;
import com.test.redisMqTest.repository.CouponRepository;
import com.test.redisMqTest.repository.MemberRepository;
import com.test.redisMqTest.repository.MembersCouponRepository;
import com.test.redisMqTest.service.CouponService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

	@Test
	public void test() throws InterruptedException {

		int test = 10000;

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failureCount = new AtomicInteger();
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		CountDownLatch countDownLatch = new CountDownLatch(test);

		long index = couponService.createCoupon("선착순 1000명 20% 할인 쿠폰", 1000).getCouponId();

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

		System.out.println("Success : "+successCount);
		System.out.println("Failure : "+failureCount);

	}

	@Test
	void redissonTest(){

		Coupons coupons = couponService.createCoupon("temp2", 100);
//
//		RBucket<Coupons> bucket = redissonClient.getBucket("coupons::"+24);

//		bucket.set(coupons);

//		Coupons coupons = bucket.get();

//		System.out.println(coupons.toString());

	}

}
