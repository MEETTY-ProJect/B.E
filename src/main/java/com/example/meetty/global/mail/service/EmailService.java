package com.example.meetty.global.mail.service;

import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
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

    public void sendStudyInviteEmail(String to, String subject, String htmlBody, String invitationToken) throws MailException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("violetcarrot21@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);

            String invitationLink = baseUrl + "/study/invite/accept?token=" + invitationToken;

            String finalHtmlBody = htmlBody.replace("{invitationLink}", invitationLink);

            helper.setText(finalHtmlBody, true);

        } catch (Exception e) {
            log.error("MimeMessageHelper를 사용한 메일 구성 실패", e);
            throw new MailException("메일 구성 중 오류가 발생했습니다.", e) {};
        }

        javaMailSender.send(mimeMessage);
        log.info("스터디 초대 메일 발송 (MimeMessageHelper): 받는 사람={}, 제목={}", to, subject);
    }
    /*
            // TODO: 필요하다면 첨부 파일 추가
            // File attachment = new File("경로/파일.pdf");
            // helper.addAttachment("파일이름.pdf", attachment);

            // TODO: 필요하다면 인라인 이미지 추가 (HTML 본문에서 <img src='cid:imageId'> 형태로 참조)
            // ClassPathResource image = new ClassPathResource("경로/이미지.png");
            // helper.addInline("imageId", image);
    */
}