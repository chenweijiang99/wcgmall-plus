package com.river.vo.server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "服务器信息")
public class SysInfo {

    @Schema(description = "服务器名称")
    private String computerName;

    @Schema(description = "服务器IP")
    private String computerIp;

    @Schema(description = "操作系统")
    private String osName;

    @Schema(description = "系统架构")
    private String osArch;

    @Schema(description = "项目路径")
    private String userDir;
}
