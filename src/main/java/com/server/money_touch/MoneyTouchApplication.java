package com.server.money_touch;

//import com.server.money_touch.global.config.KakaoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
//@EnableConfigurationProperties(KakaoProperties.class)
public class MoneyTouchApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyTouchApplication.class, args);
	}

}
