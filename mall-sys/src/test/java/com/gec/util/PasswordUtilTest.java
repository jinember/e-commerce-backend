package com.gec.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码哈希工具测试
 *
 * 关键：Java 侧的 hash 必须和数据库迁移脚本
 *   UPDATE tbl_user SET password = SHA2(CONCAT('mall-sys-pwd-pepper-2026::', password), 256)
 * 算出完全一致的值，否则所有账号都登录不了。这里锁定算法行为。
 */
class PasswordUtilTest {

    @Test
    void 相同明文必须得到相同哈希() {
        assertEquals(PasswordUtil.hash("123456"), PasswordUtil.hash("123456"));
        assertNotEquals(PasswordUtil.hash("123456"), PasswordUtil.hash("1234567"));
    }

    @Test
    void 哈希长度是64位十六进制() {
        String h = PasswordUtil.hash("123456");
        assertEquals(64, h.length());
        assertTrue(h.matches("^[0-9a-f]{64}$"));
    }

    @Test
    void 与MySQL迁移脚本的SHA2结果一致() {
        // 必须等于 MySQL 的：
        //   SELECT SHA2('mall-sys-pwd-pepper-2026::123456', 256)
        // 这个值一旦变了，说明 pepper 被改过，库里所有已迁移的密码都会失效
        assertEquals("a7f49fd1643e73086827e0a046d997089b277f308c027d5666386788540a8802",
                PasswordUtil.hash("123456"));
    }

    @Test
    void matches能正确比对() {
        String h = PasswordUtil.hash("abc@123");
        assertTrue(PasswordUtil.matches("abc@123", h));
        assertFalse(PasswordUtil.matches("wrong", h));
        assertFalse(PasswordUtil.matches(null, h));
        assertFalse(PasswordUtil.matches("abc@123", null));
    }

    @Test
    void isHashed只认64位十六进制() {
        assertTrue(PasswordUtil.isHashed(PasswordUtil.hash("x")));
        assertFalse(PasswordUtil.isHashed("123456"));       // 明文
        assertFalse(PasswordUtil.isHashed("admin-1"));      // 旧伪 token
        assertFalse(PasswordUtil.isHashed(null));
    }
}
