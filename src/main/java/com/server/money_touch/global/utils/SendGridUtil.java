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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendGridUtil {
    private final SendGrid sendGrid;

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

        String authCode = generateAuthCode();
        Content content = new Content("text/plain", "인증번호: " + authCode);


        // 발신자, 제목, 수신자, 내용을 합쳐 Mail 객체 생성
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

}
