package com.river.vo.server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "服务器文件信息")
public class SysFile {

    @Schema(description = "盘符路径")
    private String dirName;

    @Schema(description = "文件系统")
    private String typeName;

    @Schema(description = "总大小")
    private long total;

    @Schema(description = "剩余大小")
    private long free;

    @Schema(description = "已用大小")
    private long used;

    @Schema(description = "使用率")
    private double usage;
}
