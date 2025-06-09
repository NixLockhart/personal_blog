package com.blog.service;

import com.blog.util.LogUtil;
import com.blog.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * 验证码服务
 * 负责生成、存储和验证验证码
 */
public class VerificationCodeService {
    private static VerificationCodeService instance;
    private final LogUtil logUtil = LogUtil.getInstance();
    private final EmailService emailService = EmailService.getInstance();

    private VerificationCodeService() {
        // 空构造函数，DBUtil会自动初始化连接池
    }

    public static synchronized VerificationCodeService getInstance() {
        if (instance == null) {
            instance = new VerificationCodeService();
        }
        return instance;
    }

    /**
     * 生成6位数字验证码
     *
     * @return 生成的验证码
     */
    public String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 生成6位随机数
        return String.valueOf(code);
    }

    /**
     * 发送验证码到指定邮箱
     *
     * @param email 接收验证码的邮箱
     * @return 是否发送成功
     */
    public boolean sendCode(String email) {
        try {
            // 生成验证码
            String code = generateCode();

            // 设置过期时间（10分钟后）
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

            // 存储验证码到数据库
            saveVerificationCode(email, code, expiresAt);

            // 发送验证码邮件
            return emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            logUtil.logError("VERIFICATION_CODE_ERROR", "Failed to send verification code: " + e.getMessage());
            return false;
        }
    }

    /**
     * 将验证码保存到数据库
     *
     * @param email 邮箱
     * @param code 验证码
     * @param expiresAt 过期时间
     * @throws SQLException SQL异常
     */
    private void saveVerificationCode(String email, String code, LocalDateTime expiresAt) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // 先删除该邮箱的旧验证码
            String deleteSql = "DELETE FROM tb_verification_codes WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setString(1, email);
                stmt.executeUpdate();
            }

            // 插入新验证码
            String insertSql = "INSERT INTO tb_verification_codes (email, code, expires_at) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, email);
                stmt.setString(2, code);
                stmt.setTimestamp(3, Timestamp.valueOf(expiresAt));
                stmt.executeUpdate();
            }
        }
    }

    /**
     * 验证验证码是否有效
     *
     * @param email 邮箱
     * @param code 验证码
     * @return 验证结果
     */
    public boolean verifyCode(String email, String code) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM tb_verification_codes WHERE email = ? AND code = ? AND expires_at > ? AND is_used = 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setString(2, code);
                stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // 验证成功，标记验证码为已使用
                        markCodeAsUsed(email, code);
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            logUtil.logError("VERIFICATION_CODE_ERROR", "Failed to verify code: " + e.getMessage());
            return false;
        }
    }

    /**
     * 标记验证码为已使用
     *
     * @param email 邮箱
     * @param code 验证码
     * @throws SQLException SQL异常
     */
    private void markCodeAsUsed(String email, String code) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE tb_verification_codes SET is_used = 1 WHERE email = ? AND code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setString(2, code);
                stmt.executeUpdate();
            }
        }
    }

    /**
     * 清理过期的验证码
     */
    public void cleanExpiredCodes() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "DELETE FROM tb_verification_codes WHERE expires_at < ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                int count = stmt.executeUpdate();
                if (count > 0) {
                    logUtil.log("CLEAN_CODES", "system", "Cleaned " + count + " expired verification codes");
                }
            }
        } catch (SQLException e) {
            logUtil.logError("VERIFICATION_CODE_ERROR", "Failed to clean expired codes: " + e.getMessage());
        }
    }
}