package com.chuadatten.notify.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    private final JavaMailSender mailSender;

    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBrandedEmail(String toEmail, String subject, String body) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom("letrogthien@gmail.com");
        helper.setTo(toEmail);
        helper.setSubject(subject);

        // Template HTML
        String htmlContent = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background:#f6f9fc; padding:20px; }
                        .container {
                            max-width:600px; margin:0 auto; background:white; padding:20px;
                            border-radius:12px; box-shadow:0 4px 10px rgba(0,0,0,0.1);
                        }
                        .header {
                            text-align:center; padding:10px 0;
                            border-bottom:2px solid #1976d2;
                        }
                        .header h1 { margin:0; color:#1976d2; }
                        .content { padding:20px 0; color:#333; line-height:1.6; }
                        .button {
                            display:inline-block; margin-top:20px;
                            padding:12px 20px; background:#1976d2; color:white;
                            text-decoration:none; border-radius:6px;
                        }
                        .footer {
                            margin-top:30px; font-size:12px; color:#777; text-align:center;
                        }
                    </style>
                </head>
                <body>
                  <div class="container">
                    <div class="header">
                      <h1>🌟 Chuadatten Service</h1>
                    </div>
                    <div class="content">
                      <p>Xin chào,</p>
                      <p>%s</p>
                      <a href="https://yourdomain.com" class="button">Truy cập ngay</a>
                    </div>
                    <div class="footer">
                      <p>Email này được gửi từ hệ thống <b>Chuadatten</b>.<br/>
                      Vui lòng không trả lời trực tiếp.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(body);

        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }
}