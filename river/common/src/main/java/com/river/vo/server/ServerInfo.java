package com.river.vo.server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "服务器信息")
public class ServerInfo {

    @Schema(description = "cpu信息")
    private CpuInfo cpu;

    @Schema(description = "内存信息")
    private MemInfo mem;

    @Schema(description = "系统信息")
    private SysInfo sys;

    @Schema(description = "JVM信息")
    private JvmInfo jvm;

    @Schema(description = "磁盘信息")
    private List<SysFile> sysFiles;
}

