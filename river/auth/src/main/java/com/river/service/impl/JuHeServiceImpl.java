package com.river.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.river.common.Constants;
import com.river.common.RedisConstants;
import com.river.config.properties.FrontConfigProperties;
import com.river.config.properties.JuHeLoginConfigProperties;
import com.river.dto.JuHeCheckLoginResponse;
import com.river.dto.JuHeLoginResponse;
import com.river.entity.SysDict;
import com.river.entity.SysDictData;
import com.river.entity.SysRole;
import com.river.entity.SysUser;
import com.river.mapper.SysDictDataMapper;
import com.river.mapper.SysDictMapper;
import com.river.mapper.SysRoleMapper;
import com.river.mapper.SysUserMapper;
import com.river.service.JuHeService;
import com.river.utils.IpUtil;
import com.river.utils.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class JuHeServiceImpl implements JuHeService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysDictMapper sysDictMapper;
    private final SysDictDataMapper sysDictDataMapper;
    private final RedisUtil redisUtil;
    private final JuHeLoginConfigProperties juHeLoginConfigProperties;
    private final FrontConfigProperties frontProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LOGIN_EXPIRED_MESSAGE = "登录过期";
    private static final String LOGIN_FAILED_PREFIX = "登录失败，";
    private static final String REDIRECT_LOGIN_URL_FORMAT = "%slogin?code=400&message=%s";
    private static final String REDIRECT_HOME_URL_FORMAT = "%s?token=%s";

    /**
     * 获取聚合登录链接
     *
     * @param type 登录方式，1=QQ，2=微信，3=支付宝，4=新浪微博，5=百度，6=华为，7=小米，8=微软，9=钉钉，10=Gitee，11=GitHub，12=抖音
     */
    @Override
    public JuHeLoginResponse getJuHeAuth(Integer type) {
        log.info("开始聚合登录，登录类型：{}", getLoginType(type));
        String userUid = IpUtil.getIp() + "_" + type + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID();
        String apiUrl = juHeLoginConfigProperties.getLoginUrl()
                + "?id=" + juHeLoginConfigProperties.getId()
                + "&key=" + juHeLoginConfigProperties.getKey()
                + "&return=" + juHeLoginConfigProperties.getReturnUrl() + userUid
                + "&type=" + type;
        String result = HttpUtil.get(apiUrl);
        JuHeLoginResponse juHeLoginResponse;
        try {
            juHeLoginResponse = objectMapper.readValue(result, JuHeLoginResponse.class);
            if(juHeLoginResponse.getCode() != 200){
                throw new RuntimeException("聚合登录获取登录链接失败，code：" + juHeLoginResponse.getCode() + "，msg：" + juHeLoginResponse.getMsg());
            }
            redisUtil.set(userUid, juHeLoginResponse.getCxid(), RedisConstants.FIVE_MINUTES_EXPIRE, TimeUnit.MINUTES);
            log.info("聚合登录获取登录链接成功，userUid：{}，cxid：{}，logurl：{}", userUid, juHeLoginResponse.getCxid(), juHeLoginResponse.getLogurl());
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败: {}", result, e);
            throw new RuntimeException(e);
        }
        return juHeLoginResponse;
    }

    /**
     * 检查聚合登录是否成功，成功则登录并重定向到首页，失败则重定向到登录页携带失败信息
     * @param userUid 用户uid
     * @param httpServletResponse 响应
     * @throws IOException 异常
     */
    @Override
    public void checkJuHeLogin(String userUid, HttpServletResponse httpServletResponse) throws IOException {
        log.info("开始聚合登录验证，userUid：{}", userUid);
        // 判断是否已过期
        if (!redisUtil.hasKey(userUid)) {
            redirectToLogin(httpServletResponse, LOGIN_EXPIRED_MESSAGE);
            return;
        }
        // 获取cxid
        String cxid;
        try {
            Object cxidObj = redisUtil.get(userUid);
            if (cxidObj == null) {
                redirectToLogin(httpServletResponse, LOGIN_EXPIRED_MESSAGE);
                return;
            }
            cxid = cxidObj.toString();
        } catch (Exception e) {
            log.error("Redis 获取 cxid 异常", e);
            redirectToLogin(httpServletResponse, LOGIN_EXPIRED_MESSAGE);
            return;
        }
        // 判断cxid是否为空
        if (cxid.isEmpty()) {
            redirectToLogin(httpServletResponse, LOGIN_EXPIRED_MESSAGE);
            return;
        }

        String checkUrl = juHeLoginConfigProperties.getCheckLoginUrl()
                + "?id=" + juHeLoginConfigProperties.getId()
                + "&key=" + juHeLoginConfigProperties.getKey()
                + "&cxid=" + cxid;
        String result = HttpUtil.get(checkUrl);
        JuHeCheckLoginResponse juHeCheckLoginResponse;
        try {
            juHeCheckLoginResponse = objectMapper.readValue(result, JuHeCheckLoginResponse.class);
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败: {}", result, e);
            throw new RuntimeException(e);
        }
        if (juHeCheckLoginResponse.getCode() != 200) {
            redirectToLogin(httpServletResponse, LOGIN_FAILED_PREFIX + juHeCheckLoginResponse.getMsg());
            return;
        }
        redisUtil.delete(userUid);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, juHeCheckLoginResponse.getSocial_uid()));
        if (ObjectUtils.isEmpty(user)) {
            user = SysUser.builder()
                    .username(juHeCheckLoginResponse.getSocial_uid())
                    .password(UUID.randomUUID().toString())
                    .loginType(juHeCheckLoginResponse.getType())
                    .lastLoginTime(LocalDateTime.now())
                    .ipLocation(juHeCheckLoginResponse.getLocation())
                    .ip(juHeCheckLoginResponse.getIp())
                    .status(Constants.YES)
                    .nickname(juHeCheckLoginResponse.getNickname())
                    .avatar(juHeCheckLoginResponse.getFaceimg())
                    .build();
            if(juHeCheckLoginResponse.getGender().equals("男")){
                user.setSex(Constants.MALE);
            }else{
                user.setSex(Constants.FEMALE);
            }
            userMapper.insert(user);
            insertRole(user);
        }else {
            user.setLastLoginTime(LocalDateTime.now());
            user.setIpLocation(juHeCheckLoginResponse.getLocation());
            user.setIp(juHeCheckLoginResponse.getIp());
            userMapper.updateById(user);
        }

        StpUtil.login(user.getId());
        log.info("聚合登录验证成功，重定向到前端首页，token：{}", StpUtil.getTokenValue());
        httpServletResponse.sendRedirect(String.format(REDIRECT_HOME_URL_FORMAT, frontProperties.getUrl(), StpUtil.getTokenValue()));
    }

    @Override
    public List<String> getLoginType() {
        SysDict sysDict = sysDictMapper.selectOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getType, "login_type"));
        if (ObjectUtils.isEmpty(sysDict)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysDictData> query = new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictId, sysDict.getId())
                .eq(SysDictData::getStatus, Constants.YES);
        return sysDictDataMapper.selectList(query).stream()
                .map(SysDictData::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * 重定向到登录页，携带登录失败信息
     * @param response 响应
     * @param message 失败信息
     * @throws IOException 异常
     */
    private void redirectToLogin(HttpServletResponse response, String message) throws IOException {
        String encodedMessage = java.net.URLEncoder.encode(message, "UTF-8");
        response.sendRedirect(String.format(REDIRECT_LOGIN_URL_FORMAT, frontProperties.getUrl(), encodedMessage));
    }

    /**
     * 添加用户角色信息
     * @param user  用户
     */
    private void insertRole(SysUser user) {
        SysRole sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, Constants.USER));
        if (sysRole != null) {
            sysRoleMapper.addRoleUser(user.getId(), Collections.singletonList(sysRole.getId()));
        } else {
            log.warn("未找到角色 code 为 {} 的角色", Constants.USER);
        }
    }

    /**
     * 获取登录类型
     * @param type 登录类型
     * @return 登录类型
     */
    private String getLoginType(Integer type) {
        switch (type) {
            case 1:
                return "QQ";
            case 2:
                return "微信";
            case 3:
                return "支付宝";
            case 4:
                return "微博";
            case 5:
                return "百度";
            case 6:
                return "华为";
            case 7:
                return "小米";
            case 8:
                return "微软";
            case 9:
                return "钉钉";
            case 10:
                return "Gitee";
            case 11:
                return "GitHub";
            case 12:
                return "抖音";
            default:
                return "未知";
        }
    }
}
