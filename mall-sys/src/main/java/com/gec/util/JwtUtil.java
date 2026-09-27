package com.gec.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 轻量 JWT 工具（HS256）
 *
 * 说明：完全基于 JDK 自带的 javax.crypto 实现，不引入 jjwt 等第三方依赖，
 * 避免改动 pom 后本地仓库下载失败导致项目编译不过。
 * 结构与标准 JWT 一致：header.payload.signature，payload 内带 exp 过期时间。
 */
public class JwtUtil {

    /** 签名密钥（生产环境应放到配置文件/环境变量） */
    private static final String SECRET = "mall-sys-jwt-secret-2026";

    /** 默认有效期：12 小时 */
    public static final long DEFAULT_EXPIRE_MILLIS = 12L * 60 * 60 * 1000;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 签发 token
     * @param subject 主体（一般是用户ID）
     * @param claims  附加信息（账号、角色等）
     */
    public static String createToken(String subject, Map<String, Object> claims) {
        return createToken(subject, claims, DEFAULT_EXPIRE_MILLIS);
    }

    public static String createToken(String subject, Map<String, Object> claims, long expireMillis) {
        try {
            String headerJson = MAPPER.writeValueAsString(
                    new HashMap<String, Object>() {{
                        put("alg", "HS256");
                        put("typ", "JWT");
                    }});

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", subject);
            payload.put("iat", System.currentTimeMillis());
            payload.put("exp", System.currentTimeMillis() + expireMillis);
            if (claims != null) {
                payload.putAll(claims);
            }
            String payloadJson = MAPPER.writeValueAsString(payload);

            String unsigned = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8))
                    + "." + base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            return unsigned + "." + sign(unsigned);
        } catch (Exception e) {
            throw new RuntimeException("生成 token 失败", e);
        }
    }

    /**
     * 校验并解析 token
     * @return payload 内容
     * @throws RuntimeException token 为空 / 签名不对 / 已过期
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToken(String token) {
        if (token == null || token.trim().length() == 0) {
            throw new RuntimeException("token 为空");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new RuntimeException("token 格式非法");
        }
        String unsigned = parts[0] + "." + parts[1];
        if (!sign(unsigned).equals(parts[2])) {
            throw new RuntimeException("token 签名校验失败");
        }
        try {
            String json = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payload = MAPPER.readValue(json, Map.class);
            Object exp = payload.get("exp");
            if (exp != null && Long.parseLong(String.valueOf(exp)) < System.currentTimeMillis()) {
                throw new RuntimeException("token 已过期");
            }
            return payload;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("token 解析失败", e);
        }
    }

    /** 从 Authorization / token 头里提取纯 token（兼容 "Bearer xxx" 写法） */
    public static String extractToken(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.toLowerCase().startsWith("bearer ")) {
            t = t.substring(7).trim();
        }
        return t.length() == 0 ? null : t;
    }

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64UrlEncode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("签名失败", e);
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(String s) {
        return Base64.getUrlDecoder().decode(s);
    }
}
