package com.project.evgo.notification.internal;

import com.project.evgo.notification.EmailService;
import com.project.evgo.user.internal.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * SMTP implementation of EmailService using JavaMailSender.
 */
@Service
@Primary
@RequiredArgsConstructor
public class SmtpEmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name:EV-Go}")
    private String fromName;

    @Value("${app.name:EV-Go}")
    private String appName;

    @Override
    @Async
    public void sendVerificationEmail(String email, String otp) {
        String subject = appName + " - Verify Your Email";
        String htmlContent = buildVerificationEmailHtml(otp);

        sendHtmlEmail(email, subject, htmlContent);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String email, String otp) {
        String subject = appName + " - Reset Your Password";
        String htmlContent = buildPasswordResetEmailHtml(otp);

        sendHtmlEmail(email, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    @Async
    public void sendApprovalEmailWithPassword(String email, String fullName, String password) {
        String subject = appName + " - Registration Approved";
        String htmlContent = buildApprovalEmailWithPassword(email, fullName, password);

        sendHtmlEmail(email, subject, htmlContent);
    }


    @Override
    @Async
    public void sendRejectionEmail(String email, String rejectionReason) {
        String subject = appName + " - Registration Rejected";
        String htmlContent = buildRejectionEmailHtml(rejectionReason);

        sendHtmlEmail(email, subject, htmlContent);
    }

    private String buildApprovalEmailWithPassword(String email, String fullName, String password) {
        return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <style>
            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
            .header { background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;}
            .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;}
            .password-box { background: #4CAF50; color: white; font-size: 28px; font-weight: bold; letter-spacing: 4px; padding: 20px 40px; text-align: center; border-radius: 8px; margin: 20px 0; }
            .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
            .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 4px; color: #856404; }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h1>✅ %s</h1>
                <p>Registration Approved!</p>
            </div>
            <div class="content">
                <h2>Welcome, %s!</h2>
                <p>Congratulations! Your station owner registration has been <strong>approved</strong>.</p>
                <p>Your login credentials:</p>
                <p><strong>Email:</strong> %s</p>
                <p><strong>Temporary Password:</strong></p>
                <div class="password-box">%s</div>
                <div class="warning">
                    <strong>⚠️ Security Notice:</strong><br>
                    Please change this password immediately after your first login for security reasons.
                </div>
                <p>You can now log in to your account and start managing your charging stations.</p>
                <p>If you have any questions, please contact our support team.</p>
            </div>
            <div class="footer">
                <p>© 2025 %s. All rights reserved.</p>
            </div>
        </div>
    </body>
    </html>
    """.formatted(
                appName,
                fullName,
                email,
                password,
                appName
        );
    }


    private String buildRejectionEmailHtml(String rejectionReason) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f5576c 0%%, #f093fb 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .reason-box { background: #ffebee; border-left: 4px solid #f44336; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>❌ %s</h1>
                        <p>Registration Update</p>
                    </div>
                    <div class="content">
                        <h2>Registration Not Approved</h2>
                        <p>Unfortunately, your station owner registration has been <strong>rejected</strong>.</p>
                        <div class="reason-box">
                            <strong>Reason for Rejection:</strong><br>
                            %s
                        </div>
                        <p>If you believe this is a mistake or have questions, please contact our support team.</p>
                        <p>You can resubmit your registration after addressing the issues mentioned above.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """
                .formatted(appName, rejectionReason, appName);
    }


    private String buildVerificationEmailHtml(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .otp-box { background: #667eea; color: white; font-size: 32px; font-weight: bold; letter-spacing: 8px; padding: 20px 40px; text-align: center; border-radius: 8px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>⚡ %s</h1>
                            <p>Email Verification</p>
                        </div>
                        <div class="content">
                            <h2>Verify Your Email Address</h2>
                            <p>Thank you for registering with %s! Please use the following OTP code to verify your email address:</p>
                            <div class="otp-box">%s</div>
                            <p><strong>This code will expire in 30 minutes.</strong></p>
                            <p>If you didn't create an account with us, please ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(appName, appName, otp, appName);
    }

    private String buildPasswordResetEmailHtml(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .otp-box { background: #f5576c; color: white; font-size: 32px; font-weight: bold; letter-spacing: 8px; padding: 20px 40px; text-align: center; border-radius: 8px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 %s</h1>
                            <p>Password Reset</p>
                        </div>
                        <div class="content">
                            <h2>Reset Your Password</h2>
                            <p>We received a request to reset your password. Use the following OTP code:</p>
                            <div class="otp-box">%s</div>
                            <p><strong>This code will expire in 30 minutes.</strong></p>
                            <p>If you didn't request a password reset, please ignore this email or contact support if you have concerns.</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(appName, otp, appName);
    }
}
