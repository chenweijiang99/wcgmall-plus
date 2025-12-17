package com.river.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnum {
    TEXT(1, "文本"),
    IMAGE(2, "图片"),
    VIDEO(3, "视频"),
    VOICE(4, "语音"),
    EMOJI(5, "表情"),
    SYSTEM(6, "系统消息");

    private final Integer code;
    private final String desc;

    public static MessageTypeEnum getByCode(Integer code) {
        for (MessageTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return TEXT;
    }
}
