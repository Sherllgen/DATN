package com.project.evgo.notification.internal;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private SmtpEmailServiceImpl emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@evgo.com");
        ReflectionTestUtils.setField(emailService, "fromName", "EVGo System");
        ReflectionTestUtils.setField(emailService, "appName", "EVGo");

        Session session = Session.getDefaultInstance(new Properties());
        mimeMessage = new MimeMessage(session);
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testSendVerificationEmail_Success() {
        // Arrange
        String email = "test@example.com";
        String otp = "123456";

        // Act
        emailService.sendVerificationEmail(email, otp);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendPasswordResetEmail_Success() {
        // Arrange
        String email = "test@example.com";
        String otp = "654321";

        // Act
        emailService.sendPasswordResetEmail(email, otp);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendApprovalEmailWithPassword_Success() {
        // Arrange
        String email = "owner@example.com";
        String fullName = "Station Owner";
        String password = "TempPassword123";

        // Act
        emailService.sendApprovalEmailWithPassword(email, fullName, password);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendRejectionEmail_Success() {
        // Arrange
        String email = "rejected@example.com";
        String reason = "Invalid documents";

        // Act
        emailService.sendRejectionEmail(email, reason);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendEmail_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String otp = "123456";
        doThrow(new RuntimeException("SMTP Error")).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            emailService.sendVerificationEmail(email, otp);
        });
    }
}
