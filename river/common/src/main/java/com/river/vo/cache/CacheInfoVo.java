package com.river.vo.cache;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "缓存基本信息")
public class CacheInfoVo {

    @Schema(description = "Redis版本")
    private String version;

    @Schema(description = "运行模式")
    private String mode;

    @Schema(description = "端口")
    private String port;

    @Schema(description = "运行时间(天)")
    private String uptime;

    @Schema(description = "连接客户端数")
    private String clients;

    @Schema(description = "已使用内存")
    private String usedMemory;

    @Schema(description = "内存配置")
    private String maxmemory;

    @Schema(description = "AOF是否开启")
    private String aofEnabled;

    @Schema(description = "RDB是否成功")
    private String rdbLastSaveStatus;

    @Schema(description = "Key数量")
    private String keys;

    @Schema(description = "网络入口/出口")
    private String instantaneousInputKbps;
    private String instantaneousOutputKbps;
}
