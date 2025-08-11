package com.server.money_touch.global.config;

import com.sendgrid.SendGrid;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {
    @Value("${spring.sendgrid.api-key}")
    private String apiKey;

    @Bean
    public SendGrid sendGrid() {
        if (apiKey == null) {
            throw new ErrorHandler(ErrorStatus.API_IS_NULL);
        }
        return new SendGrid(apiKey);
    }
}
