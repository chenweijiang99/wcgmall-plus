package com.river.aspect;

import com.river.annotation.AccessLimit;
import com.river.common.RedisConstants;
import com.river.exception.ServiceException;
import com.river.utils.IpUtil;
import com.river.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @author: river
 * @description:
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AccessLimitAspect {

    private final RedisUtil redisUtil;

    @Before("@annotation(accessLimit)")
    public void doBefore(JoinPoint joinPoint, AccessLimit accessLimit) throws Throwable {
        int time = accessLimit.time();

        HttpServletRequest request = IpUtil.getRequest();
        // 拼接redis key = IP + Api限流
        String key = RedisConstants.RATE_LIMIT_KEY + IpUtil.getIp() + request.getRequestURI();
        // 获取redis的value
        Integer maxTimes = null;
        Object value = redisUtil.get(key);
        if (value != null) {
            maxTimes = Integer.parseInt(value.toString());
        }
        if (maxTimes == null) {
            // 如果redis中没有该ip对应的时间则表示第一次调用，保存key到redis
            redisUtil.set(key,"1",time, TimeUnit.SECONDS);
        } else if (maxTimes < accessLimit.count()) {
            // 如果redis中的时间比注解上的时间小则表示可以允许访问,这是修改redis的value时间
            redisUtil.set(key, (maxTimes + 1) + "", time, TimeUnit.SECONDS);
        } else {
            // 请求过于频繁
            log.info("API请求限流拦截启动,{} 请求过于频繁", key);
            throw new ServiceException("请求过于频繁,稍后重试");
        }
    }
}
