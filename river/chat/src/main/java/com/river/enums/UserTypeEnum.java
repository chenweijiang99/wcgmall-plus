package com.river.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {
    USER(1, "普通用户"),
    ADMIN(2, "管理员");

    private final Integer code;
    private final String desc;

    public static UserTypeEnum getByCode(Integer code) {
        for (UserTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return USER;
    }
}
