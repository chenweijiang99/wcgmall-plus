package com.river.enums;

import lombok.Getter;

/**
 * 菜单类型枚举
 */
@Getter
public enum MenuTypeEnum {
    
    /**
     * 目录
     */
    CATALOG("CATALOG", "目录"),
    
    /**
     * 菜单
     */
    MENU("MENU", "菜单"),
    
    /**
     * 按钮
     */
    BUTTON("BUTTON", "按钮");

    /**
     * 类型编码
     */
    private final String code;
    
    /**
     * 类型描述
     */
    private final String desc;

    MenuTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static MenuTypeEnum getByCode(String code) {
        for (MenuTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 