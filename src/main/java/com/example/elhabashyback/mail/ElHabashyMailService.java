package com.example.elhabashyback.mail;

import com.example.elhabashyback.configuration.mail.AppMailProperties;
import com.example.elhabashyback.user.entity.Users;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ElHabashyMailService {

    private static final String LOGO_CLASSPATH = "static/brand/el-habashy-logo.png";

    private final JavaMailSender mailSender;
    private final EmailTemplateService templates;
    private final AppMailProperties properties;

    public void sendActivationEmail(Users user, String rawToken) {
        String activationUrl = UriComponentsBuilder.fromUriString(properties.backendPublicUrl())
                .path("/api/v1/auth/activate")
                .queryParam("token", rawToken)
                .build()
                .toUriString();
        send(
                user.getEmail(),
                "فعّل حسابك على منصة الحبشي",
                "فعّل حسابك من خلال الرابط: " + activationUrl,
                templates.activation(user.getFirstName(), activationUrl)
        );
    }

    public void sendPasswordResetOtp(Users user, String otp) {
        send(
                user.getEmail(),
                "رمز استعادة كلمة المرور - الحبشي",
                "رمز استعادة كلمة المرور هو: " + otp + " (صالح لمدة 10 دقائق)",
                templates.passwordReset(user.getFirstName(), otp)
        );
    }

    private void send(String recipient, String subject, String plainText, String html) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.from());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            helper.addInline(
                    EmailTemplateService.LOGO_CONTENT_ID,
                    new ClassPathResource(LOGO_CLASSPATH),
                    "image/png"
            );
        } catch (MessagingException | IllegalStateException exception) {
            throw new MailPreparationException("Could not prepare email message", exception);
        }
        mailSender.send(message);
    }
}
