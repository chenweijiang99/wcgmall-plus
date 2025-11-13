package com.river.common;

/**
 * Redis常量
 */
public class RedisConstants {

    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN = "login:token:";



    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_code:";

    /**
     * 滑块验证码 redis key
     */
    public static final String SLIDER_CAPTCHA_CODE_KEY = "slider_captcha_code:";


    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";


    /**
     * 过期时间 1分钟
     */
    public static final long MINUTE_EXPIRE = 60;

    /**
     * 过期时间 5分钟
     */
    public static final long FIVE_MINUTES_EXPIRE = 5 * 60;

    /**
     * 过期时间 30分钟
     */
    public static final long HALF_HOUR_EXPIRE = 30 * 60;

    /**
     * 过期时间 1小时
     */
    public static final long HOUR_EXPIRE = 60 * 60;

    /**
     * 过期时间 1天
     */
    public static final long DAY_EXPIRE = 24 * 60 * 60;

    /**
     * 过期时间 7天
     */
    public static final long WEEK_EXPIRE = 7 * 24 * 60 * 60;

    /**
     * 过期时间 15天
     */
    public static final long FIFTEEN_DAYS_EXPIRE = 15 * 24 * 60 * 60;

    /**
     * 过期时间 30天
     */
    public static final long MONTH_EXPIRE = 30 * 24 * 60 * 60;

    /**
     * 网站配置
     */
    public static final String WEB_CONFIG_KEY = "sys_webconfig";

    /**
     * 首页轮播图列表
     */
    public static final String INDEX_SLIDER_LIST = "index_slider_list";

    /**
     * 商城轮播图列表
     */
    public static final String SHOP_SLIDER_LIST = "shop_slider_list";
    /**
     * 官方收藏列表
     */
    public static final String OFFICIAL_COLLECTION_LIST = "official_collection_list";
}
