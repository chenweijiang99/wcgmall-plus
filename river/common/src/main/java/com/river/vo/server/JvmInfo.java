package com.river.vo.server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "服务器jvm信息")
public class JvmInfo {

    @Schema(description = "当前JVM占用的总内存")
    private String total;

    @Schema(description = "JVM最大可用内存")
    private String name;

    @Schema(description = "JVM内存使用率")
    private String usage;

    @Schema(description = "JDK版本")
    private String version;

    @Schema(description = "JDK路径")
    private String home;

    @Schema(description = "JDK启动时间")
    private String startTime;

    @Schema(description = "JDK运行时长")
    private String runTime;
}
