package com.changgeng.prompt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
public class PromptApplication {

//	@Autowired
//	IECConfig iecConfig;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(PromptApplication.class, args);
	}

}
