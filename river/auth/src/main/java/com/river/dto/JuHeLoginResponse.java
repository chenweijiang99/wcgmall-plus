package com.mojian.dto;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class JuHeLoginResponse {
    private Integer code;
    private String msg;
    private String cxid;
    private String logurl;
    private String logqrcode;
}
