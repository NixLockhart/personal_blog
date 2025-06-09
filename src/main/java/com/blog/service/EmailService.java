package com.blog.service;

import com.blog.util.LogUtil;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮件发送服务
 * 负责发送验证码邮件
 */
public class EmailService {
    private static EmailService instance;
    private final LogUtil logUtil = LogUtil.getInstance();

    // 邮件服务器配置
    private final String host = "smtp.126.com"; 		// 使用126邮箱SMTP服务器
    private final String port = "465"; 					// SMTP SSL端口
    private final String username = "xxxx@126.com"; 	// 发件人邮箱，需要替换为实际邮箱
    private final String password = "xxxxxxxxxxxxxxxx"; // 邮箱授权码，需要替换为实际授权码

    private EmailService() {
        // 私有构造函数，防止直接实例化
    }

    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    /**
     * 发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            // 配置邮件服务器属性
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true"); // 启用SSL加密
            props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // 指定SSL协议版本
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // 使用SSL Socket工厂
            props.put("mail.smtp.socketFactory.port", port); // Socket工厂端口

            // 创建会话
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // 创建邮件消息
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("星光小栈 - 验证码");

            // 邮件内容
            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;'>"
                    + "<h2 style='color: #333; text-align: center;'>星光小栈 - 验证码</h2>"
                    + "<p style='color: #555; font-size: 16px;'>您好，</p>"
                    + "<p style='color: #555; font-size: 16px;'>您的验证码是：</p>"
                    + "<div style='background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; margin: 20px 0;'>"
                    + code
                    + "</div>"
                    + "<p style='color: #555; font-size: 16px;'>该验证码将在10分钟内有效。</p>"
                    + "<p style='color: #555; font-size: 16px;'>如果您没有请求此验证码，请忽略此邮件。</p>"
                    + "<p style='color: #777; font-size: 14px; margin-top: 30px; text-align: center;'>此邮件由系统自动发送，请勿回复。</p>"
                    + "</div>";

            message.setContent(htmlContent, "text/html;charset=UTF-8");

            // 发送邮件
            Transport.send(message);

            logUtil.log("EMAIL_SENT", toEmail, "Verification code sent successfully");
            return true;
        } catch (MessagingException e) {
            logUtil.logError("EMAIL_ERROR", "Failed to send verification code: " + e.getMessage());
            return false;
        }
    }
}