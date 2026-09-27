package com.gec.components;

import com.gec.controller.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * 原来每个 Controller 各自 try-catch，漏掉的就直接把堆栈抛给前端：
 * 前端只能拿到一个 500 空页面，什么都看不出来。
 * 这里做兜底 —— 已经自己 catch 的接口不受影响，没 catch 的会走到这里，
 * 统一返回 {result:failed, status:500, cause:原因} 并打日志。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public R handleException(Exception e) {
        log.error("接口未捕获异常", e);
        String msg = e.getMessage();
        if (msg == null || msg.trim().length() == 0) {
            msg = e.getClass().getSimpleName();
        }
        return R.err(new RuntimeException(msg));
    }
}
