package com.river.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.river.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    /**
     * 业务异常
     * @param e
     * @return
     */
    @ExceptionHandler(ServiceException.class)
    public Result<Void> handleServiceException(ServiceException e) {
        log.error(e.getMessage(), e);
        return Result.error(e.getMessage());
    }


    /**
     * 权限不足异常
     * @param e
     * @return
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.error(e.getMessage(), e);
        return Result.error(HttpStatus.FORBIDDEN.value(),e.getMessage());
    }


    /**
     * 未登录异常
     * @param e
     * @return
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("未登录异常：", e);
        return Result.error(HttpStatus.UNAUTHORIZED.value(),"当前用户未登录或 登录已过期");
    }

    /**
     * 系统异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error("系统错误");
    }
}
