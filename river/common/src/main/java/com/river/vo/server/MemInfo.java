package com.river.vo.server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "服务器内存信息")
public class MemInfo {

    @Schema(description = "总内存")
    private long total;

    @Schema(description = "已用内存")
    private long used;

    @Schema(description = "剩余内存")
    private long free;

    @Schema(description = "使用率")
    private double usage;
}
