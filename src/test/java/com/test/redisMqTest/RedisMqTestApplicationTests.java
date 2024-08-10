package com.test.redisMqTest;

import com.test.redisMqTest.repository.CouponRepository;
import com.test.redisMqTest.repository.MemberRepository;
import com.test.redisMqTest.repository.MembersCouponRepository;
import com.test.redisMqTest.service.CouponService;
import org.junit.jupiter.api.Test;
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

	@Test
	public void test() throws InterruptedException {

		int test = 1000;

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failureCount = new AtomicInteger();
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CountDownLatch countDownLatch = new CountDownLatch(test);

		long index = couponService.createCoupon("coupon21", 100).getCouponId();

		Random random = new Random();


		for(int i=0; i<test; i++){
			executorService.execute(()->{
				try{
					System.out.println(couponService.getCoupons(1001+ random.nextInt(3900), index));
					successCount.incrementAndGet();
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

}
