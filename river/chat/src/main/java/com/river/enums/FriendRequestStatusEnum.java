package com.river.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 好友申请状态枚举
 */
@Getter
@AllArgsConstructor
public enum FriendRequestStatusEnum {
    PENDING(0, "待处理"),
    ACCEPTED(1, "已同意"),
    REJECTED(2, "已拒绝"),
    EXPIRED(3, "已过期");

    private final Integer code;
    private final String desc;

    public static FriendRequestStatusEnum getByCode(Integer code) {
        for (FriendRequestStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
