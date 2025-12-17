package com.river.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天室类型枚举
 */
@Getter
@AllArgsConstructor
public enum ChatRoomTypeEnum {
    PRIVATE(1, "私聊"),
    GROUP(2, "群聊");

    private final Integer code;
    private final String desc;

    public static ChatRoomTypeEnum getByCode(Integer code) {
        for (ChatRoomTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return PRIVATE;
    }
}
