package com.gec.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 密码加盐哈希工具
 *
 * 原实现是明文存储、明文比对（/User/login 直接拿密码去查库），这里改为
 * SHA-256(pepper + 明文) 后比对。
 *
 * 为什么用固定 pepper 而不是每人随机 salt：项目里没有 salt 字段，加字段要动表结构，
 * 风险大。固定 pepper 已经能挡住直接拖库看明文，历史数据用一条 SQL 就能迁移：
 *   UPDATE tbl_user SET password = SHA2(CONCAT('mall-sys-pwd-pepper-2026::', password), 256);
 *
 * 如需更强，后续把 pepper 换 per-user salt 即可，改这一处就够。
 */
public class PasswordUtil {

    /** 全局 pepper，与数据库迁移脚本保持一致 */
    private static final String PEPPER = "mall-sys-pwd-pepper-2026::";

    public static String hash(String plainPassword) {
        if (plainPassword == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((PEPPER + plainPassword).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /** 明文与库中哈希值比对 */
    public static boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        return hash(plainPassword).equalsIgnoreCase(storedHash.trim());
    }

    /**
     * 判断库中存的是不是已经哈希过的值（64 位十六进制）。
     * 用于兼容尚未迁移的历史明文数据。
     */
    public static boolean isHashed(String value) {
        return value != null && value.matches("^[a-fA-F0-9]{64}$");
    }
}
