package com.river.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成员角色枚举
 */
@Getter
@AllArgsConstructor
public enum MemberRoleEnum {
    MEMBER(0, "普通成员"),
    ADMIN(1, "管理员"),
    OWNER(2, "群主");

    private final Integer code;
    private final String desc;

    public static MemberRoleEnum getByCode(Integer code) {
        for (MemberRoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return MEMBER;
    }
}
