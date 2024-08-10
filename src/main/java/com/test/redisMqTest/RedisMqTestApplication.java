package com.test.redisMqTest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RedisMqTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisMqTestApplication.class, args);
	}

}
