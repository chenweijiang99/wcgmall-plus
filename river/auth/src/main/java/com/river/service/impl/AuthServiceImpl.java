package com.river.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.river.common.Constants;
import com.river.common.RedisConstants;
import com.river.config.properties.*;
import com.river.dto.Captcha;
import com.river.dto.EmailRegisterDto;
import com.river.dto.EmailResetDto;
import com.river.dto.LoginDTO;
import com.river.dto.user.LoginUserInfo;
import com.river.entity.SysConfig;
import com.river.entity.SysRole;
import com.river.entity.SysUser;
import com.river.enums.MenuTypeEnum;
import com.river.exception.ServiceException;
import com.river.mapper.SysConfigMapper;
import com.river.mapper.SysMenuMapper;
import com.river.mapper.SysRoleMapper;
import com.river.mapper.SysUserMapper;
import com.river.service.AuthService;
import com.river.service.SysSocialConfigService;
import com.river.entity.SysSocialConfig;
import com.river.utils.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.*;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SysConfigMapper sysConfigMapper;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final RedisUtil redisUtil;
    private final EmailUtil emailUtil;
    private final CaptchaConfigProperties captchaConfigProperties;
    private final RandomAvatarConfigProperties randomAvatarConfigProperties;
    private final GiteeConfigProperties giteeConfigProperties;
    private final GithubConfigProperties githubConfigProperties;
    private final QqConfigProperties qqConfigProperties;
    private final WeiboConfigProperties weiboConfigProperties;
    private final FrontConfigProperties frontConfigProperties;
    private final SysSocialConfigService sysSocialConfigService;


    @Override
    public LoginUserInfo login(LoginDTO loginDTO) {
        log.info("用户登录：{}", loginDTO);
        SysConfig verifySwitch = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, "slider_verify_switch"));
        if (verifySwitch != null && verifySwitch.getConfigValue().equals("Y")) {
            //校验验证码
            CaptchaUtil.checkImageCode(loginDTO.getNonceStr(), loginDTO.getValue(), captchaConfigProperties.getAllowDeviation());
        }

        // 查询用户
        SysUser user = sysUserMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            user = sysUserMapper.selectByEmail(loginDTO.getUsername());
        }

        //校验是否能够登录
        validateLogin(loginDTO, user);

        // 执行登录
        StpUtil.login(user.getId());
        String tokenValue = StpUtil.getTokenValue();
        // 返回用户信息
        LoginUserInfo loginUserInfo = BeanUtil.copyProperties(user, LoginUserInfo.class);
        loginUserInfo.setToken(tokenValue);

        StpUtil.getSession().set(Constants.CURRENT_USER, loginUserInfo);
        return loginUserInfo;
    }

    @Override
    public LoginUserInfo getLoginUserInfo(String source) {
        // 获取当前登录用户ID
        Integer userId = StpUtil.getLoginIdAsInt();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        LoginUserInfo loginUserInfo = BeanUtil.copyProperties(user, LoginUserInfo.class);

        //获取菜单权限列表
        if (source.equalsIgnoreCase(Constants.ADMIN)) {
            List<String> permissions;
            List<String> roles = roleMapper.selectRolesCodeByUserId(userId);
            if (roles.contains(Constants.ADMIN)) {
                permissions = menuMapper.getPermissionList(MenuTypeEnum.BUTTON.getCode());
            } else {
                permissions = menuMapper.getPermissionListByUserId(userId, MenuTypeEnum.BUTTON.getCode());
            }
            loginUserInfo.setRoles(roles);
            loginUserInfo.setPermissions(permissions);
        }

        return loginUserInfo;
    }

    @Override
    public Boolean sendEmailCode(String email) throws MessagingException {
        emailUtil.sendCode(email);
        return true;
    }

    @Override
    public Boolean register(EmailRegisterDto dto) {

        validateEmailCode(dto);

        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (sysUser != null) {
            throw new ServiceException("当前用户名已存在");
        }
        sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, dto.getEmail()));
        if (sysUser != null) {
            throw new ServiceException("当前邮箱已注册，请前往登录");
        }

        //获取随机头像
        String avatar = randomAvatarConfigProperties.getAvatarList()[(int) (Math.random() * randomAvatarConfigProperties.getAvatarList().length)];
        sysUser = SysUser.builder()
                .username(dto.getUsername())
                .password(BCrypt.hashpw(dto.getPassword()))
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .avatar(avatar)
                .status(Constants.YES)
                .loginType(Constants.EMAIL)
                .build();
        sysUserMapper.insert(sysUser);

        //添加用户角色信息
        insertRole(sysUser);

        redisUtil.delete(RedisConstants.CAPTCHA_CODE_KEY + dto.getEmail());
        return true;
    }

    @Override
    public Boolean forgot(EmailResetDto dto) {
        validateEmailCode(dto);
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, dto.getEmail()));
        if (sysUser == null) {
            throw new ServiceException("当前邮箱未注册，请前往注册");
        }
        sysUser.setPassword(BCrypt.hashpw(dto.getPassword()));
        sysUserMapper.updateById(sysUser);
        redisUtil.delete(RedisConstants.CAPTCHA_CODE_KEY + dto.getEmail());
        return true;
    }

    @Override
    public String renderAuth(String source) {
        AuthRequest authRequest = getAuthRequest(source);
        return authRequest.authorize(AuthStateUtils.createState());
    }

    @Override
    public void authLogin(AuthCallback callback, String source, HttpServletResponse httpServletResponse) throws IOException {
        AuthRequest authRequest = getAuthRequest(source);
        AuthResponse<AuthUser> response = authRequest.login(callback);

        if (response.getData() == null) {
            log.info("用户取消了 {} 第三方登录", source);
            // 指定跳转的前端地址
            httpServletResponse.sendRedirect(frontConfigProperties.getUrl());
            return;
        }
        String result = com.alibaba.fastjson.JSONObject.toJSONString(response.getData());
        log.info("第三方登录验证结果:{}", result);

        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
        Object uuid = jsonObject.get("uuid");
        // 获取用户ip信息
        String ipAddress = IpUtil.getIp();
        String ipSource = IpUtil.getIp2region(ipAddress);
        // 判断是否已注册
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, uuid));
        if (ObjectUtils.isEmpty(user)) {
            // 保存账号信息
            user = SysUser.builder()
                    .username(uuid.toString())
                    .password(UUID.randomUUID().toString())
                    .loginType(source)
                    .lastLoginTime(LocalDateTime.now())
                    .ipLocation(ipAddress)
                    .ip(ipSource)
                    .status(Constants.YES)
                    .nickname(source + "-" + getRandomString(6))
                    // 获取用户头像，若为空则随机分配默认头像
                    .avatar(jsonObject.get("avatar").toString() != null
                            ? jsonObject.get("avatar").toString()
                            : randomAvatarConfigProperties.getAvatarList()[(int) (Math.random()
                            * randomAvatarConfigProperties.getAvatarList().length)])
                    .build();
            sysUserMapper.insert(user);
            //添加角色信息
            insertRole(user);
        }

        StpUtil.login(user.getId());
        httpServletResponse.sendRedirect(frontConfigProperties.getUrl() + "?token=" + StpUtil.getTokenValue());
    }

    @Override
    public Boolean checkUsername(String username) {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        return sysUser != null;
    }

    @Override
    public Boolean checkEmail(String email) {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email));
        return sysUser != null;
    }

    /**
     * 校验邮箱验证码
     *
     * @param dto
     */
    private void validateEmailCode(EmailRegisterDto dto) {
        Object code = redisUtil.get(RedisConstants.CAPTCHA_CODE_KEY + dto.getEmail());
        if (code == null || !code.equals(dto.getCode())) {
            throw new ServiceException("验证码已过期或输入错误");
        }
    }
    /**
     * 校验邮箱验证码
     *
     * @param dto
     */
    private void validateEmailCode(EmailResetDto dto) {
        Object code = redisUtil.get(RedisConstants.CAPTCHA_CODE_KEY + dto.getEmail());
        if (code == null || !code.equals(dto.getCode())) {
            throw new ServiceException("验证码已过期或输入错误");
        }
    }

    /**
     * 校验登录信息
     *
     * @param loginDTO
     * @param user
     */
    private static void validateLogin(LoginDTO loginDTO, SysUser user) {
        if (user == null) {
            throw new ServiceException("登录用户不存在");
        }

        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }

        // 验证状态
        if (user.getStatus() != 1) {
            throw new ServiceException("账号已被禁用");
        }

        if (user.getUsername().equals(Constants.TEST) && loginDTO.getSource().equalsIgnoreCase("PC")) {
            throw new ServiceException("演示用户不允许门户登录！");
        }
    }

    /**
     * 添加用户角色信息
     *
     * @param user
     */
    private void insertRole(SysUser user) {
        SysRole sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, Constants.USER));
        sysRoleMapper.addRoleUser(user.getId(), Collections.singletonList(sysRole.getId()));
    }

    /**
     * 获取第三方授权地址
     * 优先从数据库读取配置，若数据库无配置则使用配置文件
     *
     * @param source
     * @return
     */
    private @NotNull AuthRequest getAuthRequest(String source) {
        AuthRequest authRequest = null;

        // 优先从数据库获取配置
        SysSocialConfig dbConfig = sysSocialConfigService.selectByType(source);

        switch (source) {
            case "gitee":
                if (dbConfig != null && dbConfig.getAppId() != null && !dbConfig.getAppId().isEmpty()) {
                    authRequest = new AuthGiteeRequest(AuthConfig.builder()
                            .clientId(dbConfig.getAppId())
                            .clientSecret(dbConfig.getAppSecret())
                            .redirectUri(dbConfig.getRedirectUrl())
                            .build());
                } else {
                    authRequest = new AuthGiteeRequest(AuthConfig.builder()
                            .clientId(giteeConfigProperties.getAppId())
                            .clientSecret(giteeConfigProperties.getAppSecret())
                            .redirectUri(giteeConfigProperties.getRedirectUrl())
                            .build());
                }
                break;
            case "qq":
                if (dbConfig != null && dbConfig.getAppId() != null && !dbConfig.getAppId().isEmpty()) {
                    authRequest = new AuthQqRequest(AuthConfig.builder()
                            .clientId(dbConfig.getAppId())
                            .clientSecret(dbConfig.getAppSecret())
                            .redirectUri(dbConfig.getRedirectUrl())
                            .build());
                } else {
                    authRequest = new AuthQqRequest(AuthConfig.builder()
                            .clientId(qqConfigProperties.getAppId())
                            .clientSecret(qqConfigProperties.getAppSecret())
                            .redirectUri(qqConfigProperties.getRedirectUrl())
                            .build());
                }
                break;
            case "weibo":
                if (dbConfig != null && dbConfig.getAppId() != null && !dbConfig.getAppId().isEmpty()) {
                    authRequest = new AuthWeiboRequest(AuthConfig.builder()
                            .clientId(dbConfig.getAppId())
                            .clientSecret(dbConfig.getAppSecret())
                            .redirectUri(dbConfig.getRedirectUrl())
                            .build());
                } else {
                    authRequest = new AuthWeiboRequest(AuthConfig.builder()
                            .clientId(weiboConfigProperties.getAppId())
                            .clientSecret(weiboConfigProperties.getAppSecret())
                            .redirectUri(weiboConfigProperties.getRedirectUrl())
                            .build());
                }
                break;
            case "github":
                if (dbConfig != null && dbConfig.getAppId() != null && !dbConfig.getAppId().isEmpty()) {
                    authRequest = new AuthGithubRequest(AuthConfig.builder()
                            .clientId(dbConfig.getAppId())
                            .clientSecret(dbConfig.getAppSecret())
                            .redirectUri(dbConfig.getRedirectUrl())
                            .build());
                } else {
                    authRequest = new AuthGithubRequest(AuthConfig.builder()
                            .clientId(githubConfigProperties.getAppId())
                            .clientSecret(githubConfigProperties.getAppSecret())
                            .redirectUri(githubConfigProperties.getRedirectUrl())
                            .build());
                }
                break;
            default:
                break;
        }
        if (null == authRequest) {
            throw new AuthException("授权地址无效");
        }
        return authRequest;
    }

    /**
     * 随机生成6位数的字符串
     */
    public static String getRandomString(int length) {
        String str = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(str.length());
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
