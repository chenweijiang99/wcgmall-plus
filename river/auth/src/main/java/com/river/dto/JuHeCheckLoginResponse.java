package com.river.dto;

import lombok.Data;

@Data
public class JuHeCheckLoginResponse {
    // 状态码
    private Integer code;
//    提示信息
    private String msg;
//    登录方式：1=QQ，2=微信，3=支付宝，4=新浪微博，5=百度，6=华为，7=小米，8=微软，9=钉钉，10=Gitee，11=GitHub，12=抖音。
    private String type;
//    发起登录的时间
    private String addtime;
//    完成登录的时间
    private String endtime;
//    第三方登录账号的UID，请与自己的账号系统绑定，
//    后期用户使用三方账号登录判断是否与该字段的信息一致即可。
//    如果该字段不为空则表示登录成功，为空则表示用户方可能已完成登录操作正在获取信息中或获取失败。
//    某些三方账号的ID较长，建议字段长度设置为255.
    private String openid;
//    三方用户头像，不一定返回。该信息为异步获取，如果返回了三方账号ID，可能存在一定延迟才会返回该字段内容。
    private String faceimg;
//    三方用户昵称，不一定返回。该信息为异步获取，如果返回了三方账号ID，可能存在一定延迟才会返回该字段内容。
    private String nickname;
//    三方用户性别，不一定返回。该信息为异步获取，如果返回了三方账号ID，可能存在一定延迟才会返回该字段内容。
    private String gender;
}
