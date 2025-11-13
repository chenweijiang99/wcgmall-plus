package com.river.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author: river
 * @description: 存储平台枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum FileOssEnum {
    QINIU("qiniu"),

    ALI("ali"),

    TENCENT("tencent"),

    MINIO("minio"),

    LOCAL("local");

    private String value;

}
