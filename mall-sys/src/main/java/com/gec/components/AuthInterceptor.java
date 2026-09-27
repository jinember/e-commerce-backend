package com.gec.components;

import com.gec.util.JwtUtil;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 后台接口鉴权拦截器
 *
 * 规则：除白名单外，所有请求必须携带有效 token，否则返回 401。
 * 白名单由 WebConfig 配置（登录接口、C 端商城接口、图片读取接口）。
 *
 * 放行两类请求：
 *   1. OPTIONS 预检请求（CORS 预检不带自定义头，拦了会导致跨域全挂）
 *   2. 非 HandlerMethod 的静态资源请求
 */
public class AuthInterceptor implements HandlerInterceptor {

    /** 放入 request 的属性名，controller 里可取出当前登录用户 */
    public static final String CURRENT_USER = "currentUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. CORS 预检直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 2. 非 controller 方法（静态资源等）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String raw = request.getHeader("token");
        if (raw == null || raw.trim().length() == 0) {
            raw = request.getHeader("Authorization");
        }
        String token = JwtUtil.extractToken(raw);

        try {
            Map<String, Object> payload = JwtUtil.parseToken(token);
            request.setAttribute(CURRENT_USER, payload);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"result\":\"failed\",\"status\":401,\"cause\":\"未登录或登录已过期，请重新登录\"}");
            return false;
        }
    }
}
