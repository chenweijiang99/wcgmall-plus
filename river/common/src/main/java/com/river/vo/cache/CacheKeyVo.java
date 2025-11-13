package com.river.vo.cache;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "缓存键信息")
public class CacheKeyVo {

    @Schema(description = "键名")
    private String key;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "大小")
    private Long size;

    @Schema(description = "过期时间")
    private Long ttl;
}
