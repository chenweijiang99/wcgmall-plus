package com.river.service;

import com.river.dto.JuHeLoginResponse;
import com.river.entity.SysDictData;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface JuHeService {

    JuHeLoginResponse getJuHeAuth(Integer type);

    void checkJuHeLogin(String cxid, HttpServletResponse httpServletResponse) throws IOException;

    List<String> getLoginType();
}
