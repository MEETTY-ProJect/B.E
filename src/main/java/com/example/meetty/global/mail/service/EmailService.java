package com.example.meetty.global.mail.service;

import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationLink(String email, String token) throws MessagingException {


        String verifyUrl =  baseUrl +"/api/auth/verify?token=" + token;

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setFrom("violetcarrot21@gmail.com"); // Gmail 계정과 동일해야 함
        helper.setTo(email);
        helper.setSubject("회원가입 이메일 인증");
        helper.setText("""
                아래 링크를 클릭하여 이메일 인증을 완료해주세요:

                %s

                ※ 본 링크는 1시간 동안만 유효합니다.
                """.formatted(verifyUrl));

        try {
            javaMailSender.send(mimeMessage);
            log.info("✅ 이메일 인증 링크 전송 완료: {}", email);
        } catch (Exception e) {
            log.error("❌ 이메일 전송 실패: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }
}
