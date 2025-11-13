package com.river.vo.cache;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "缓存内存信息")
public class CacheMemoryVo {

    @Schema(description = "已用内存")
    private Long used;

    @Schema(description = "总内存")
    private Long total;
}
