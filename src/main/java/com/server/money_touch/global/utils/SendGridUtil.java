package com.server.money_touch.global.utils;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendGridUtil {
    private final SendGrid sendGrid;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.sendgrid.from}")
    private String fromEmail;

    public String generateAuthCode() {
        Random random = new Random();
        int authCode = 100000 + random.nextInt(900000);  // 100000~999999 범위의 6자리 숫자 생성
        return String.valueOf(authCode);
    }


    public void sendEmail(String toEmail) throws IOException {

        // 보내는 사람 (발신자)
        Email from = new Email(fromEmail);
        // 제목
        String subject = "💸돈터치 이메일 발송 안내";
        // 받는 사람 (수신자)
        Email to = new Email(toEmail);

        // 인증번호 생성
        String authCode = generateAuthCode();

        // Redis에 저장 (5분 TTL)
        String redisKey = "emailAuth:" + toEmail;
        redisTemplate.opsForValue().set(redisKey, authCode, 5, TimeUnit.MINUTES);

        // 로그 출력 (인증코드 저장 확인)
        String savedCode = redisTemplate.opsForValue().get(redisKey);
        log.info("✅ Redis 저장 완료 - key: {}, value: {}", redisKey, savedCode);

        Content content = new Content("text/plain", "인증번호: " + authCode);
        Mail mail = new Mail(from, subject, to, content);

        send(mail);


    }

    private void send(Mail mail) throws IOException {
        sendGrid.addRequestHeader("X-Mock", "true");

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);
        log.info("SendGrid Response: {}", response.getStatusCode());
        log.info("SendGrid Response: {}", response.getBody());
        log.info("SendGrid Response: {}", response.getHeaders());
    }

    public boolean verifyAuthCode(String toEmail, String inputCode) {
        String redisKey = "emailAuth:" + toEmail;
        String savedCode = redisTemplate.opsForValue().get(redisKey);

        // 로그로 현재 Redis 값 확인
        log.info("🔍 Redis 조회 - key: {}, value: {}", redisKey, savedCode);

        if (savedCode != null && savedCode.equals(inputCode)) {
            // 인증 성공 시 Redis 키 삭제
            redisTemplate.delete(redisKey);
            log.info("🗑️ Redis 키 삭제 완료 - key: {}", redisKey);
            return true;
        }
        return false;
    }

}
