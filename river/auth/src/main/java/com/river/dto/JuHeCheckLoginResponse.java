package com.river.dto;

import lombok.Data;

@Data
public class JuHeCheckLoginResponse {
    private Integer code;
    private String msg;
    private String type;
    private String addtime2;
    private String endtime2;
    private String social_uid;
    private String access_token;
    private String faceimg;
    private String nickname;
    private String gender;
    private String location;
    private String ip;
}
