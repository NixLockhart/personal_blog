package com.blog.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用SHA-256算法对密码进行加密
     * @param password 原始密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            // 将盐值解码为字节数组
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            // 将密码转换为字节数组
            byte[] passwordBytes = password.getBytes();
            // 将盐值和密码合并
            byte[] combined = new byte[saltBytes.length + passwordBytes.length];
            System.arraycopy(saltBytes, 0, combined, 0, saltBytes.length);
            System.arraycopy(passwordBytes, 0, combined, saltBytes.length, passwordBytes.length);
            // 计算哈希值
            byte[] hashedBytes = digest.digest(combined);
            // 将哈希值转换为Base64字符串
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("加密算法不可用", e);
        }
    }

    /**
     * 验证密码是否正确
     * @param inputPassword 输入的密码
     * @param storedHash 存储的密码哈希值
     * @param storedSalt 存储的盐值
     * @return 密码是否匹配
     */
    public static boolean verifyPassword(String inputPassword, String storedHash, String storedSalt) {
        String hashedInput = hashPassword(inputPassword, storedSalt);
        return hashedInput.equals(storedHash);
    }
}