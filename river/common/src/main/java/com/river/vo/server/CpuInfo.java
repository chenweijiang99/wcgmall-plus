package com.river.vo.server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "服务器cpu信息")
public class CpuInfo {

    @Schema(description = "cpu核数")
    private int cpuNum;

    @Schema(description = "cpu用户使用率")
    private double used;

    @Schema(description = "cpu系统使用率")
    private double sys;

    @Schema(description = "当前空闲率")
    private double free;
}
