package com.gec.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具类测试
 *
 * 这是项目里第一份单元测试 —— 鉴权是安全相关的基础设施，
 * 一旦签发/校验逻辑被改坏，所有接口都会挂，必须有用例兜底。
 */
class JwtUtilTest {

    @Test
    void 签发的token能被正确解析() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("account", "admin");
        claims.put("userId", 1);

        String token = JwtUtil.createToken("1", claims);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "JWT 应该是三段结构");

        Map<String, Object> payload = JwtUtil.parseToken(token);
        assertEquals("1", payload.get("sub"));
        assertEquals("admin", payload.get("account"));
        assertNotNull(payload.get("exp"));
    }

    @Test
    void 空token与非法格式都要被拒绝() {
        assertThrows(RuntimeException.class, () -> JwtUtil.parseToken(null));
        assertThrows(RuntimeException.class, () -> JwtUtil.parseToken(""));
        assertThrows(RuntimeException.class, () -> JwtUtil.parseToken("abc"));
        assertThrows(RuntimeException.class, () -> JwtUtil.parseToken("a.b"));
    }

    @Test
    void 篡改签名必须失败() {
        String token = JwtUtil.createToken("1", null);
        String[] parts = token.split("\\.");
        // 把签名段换掉
        String forged = parts[0] + "." + parts[1] + ".AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        assertThrows(RuntimeException.class, () -> JwtUtil.parseToken(forged));
    }

    @Test
    void 过期的token必须失败() {
        String token = JwtUtil.createToken("1", null, -1000L);
        assertThrows(RuntimeException.class, () -> JwtUtil.parseToken(token));
    }

    @Test
    void 能从Bearer头里提取token() {
        assertEquals("abc", JwtUtil.extractToken("Bearer abc"));
        assertEquals("abc", JwtUtil.extractToken("abc"));
        assertNull(JwtUtil.extractToken(""));
        assertNull(JwtUtil.extractToken(null));
    }
}
