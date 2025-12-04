package com.mojian.service;

import com.mojian.dto.JuHeLoginResponse;
import com.mojian.dto.user.LoginUserInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface JuHeService {

    JuHeLoginResponse getJuHeAuth(Integer type);

    void checkJuHeLogin(String cxid, HttpServletResponse httpServletResponse) throws IOException;
}
