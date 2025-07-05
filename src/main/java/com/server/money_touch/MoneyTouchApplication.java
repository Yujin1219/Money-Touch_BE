package com.server.money_touch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoneyTouchApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyTouchApplication.class, args);
	}

}
