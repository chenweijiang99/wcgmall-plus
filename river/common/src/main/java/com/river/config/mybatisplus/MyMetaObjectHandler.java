package com.river.config.mybatisplus;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.river.common.Constants;
import com.river.dto.user.LoginUserInfo;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUserInfo loginUserInfo = getCurrentUser();
        if (loginUserInfo != null) {
            this.strictInsertFill(metaObject, "createBy", String.class, loginUserInfo.getUsername());
            this.strictInsertFill(metaObject, "updateBy", String.class, loginUserInfo.getUsername());
        }
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LoginUserInfo loginUserInfo = getCurrentUser();
        if (loginUserInfo != null) {
            this.strictInsertFill(metaObject, "updateBy", String.class, loginUserInfo.getUsername());
        }
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    private LoginUserInfo getCurrentUser() {
        try {
            Object obj = StpUtil.getSession().get(Constants.CURRENT_USER);
            if (obj instanceof LoginUserInfo) {
                return (LoginUserInfo) obj;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
} 