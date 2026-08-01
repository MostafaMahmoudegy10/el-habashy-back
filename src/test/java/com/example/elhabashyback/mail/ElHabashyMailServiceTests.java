package com.example.elhabashyback.mail;

import com.example.elhabashyback.configuration.mail.AppMailProperties;
import com.example.elhabashyback.user.entity.Users;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ElHabashyMailServiceTests {

    @Test
    void activationEmailIsPreparedAsMultipartAlternativeMessage() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);

        AppMailProperties properties = new AppMailProperties(
                "info@elhabashy.com",
                "https://elhabashy.com",
                "https://api.elhabashy.com",
                Duration.ofHours(24),
                Duration.ofMinutes(10),
                Duration.ofMinutes(1)
        );
        ElHabashyMailService mailService = new ElHabashyMailService(
                mailSender,
                new EmailTemplateService(),
                properties
        );
        Users user = new Users();
        user.setFirstName("Mostafa");
        user.setEmail("mostafa@example.com");

        mailService.sendActivationEmail(user, "activation-token");
        message.saveChanges();

        verify(mailSender).send(message);
        assertThat(message.isMimeType("multipart/*")).isTrue();
    }
}
