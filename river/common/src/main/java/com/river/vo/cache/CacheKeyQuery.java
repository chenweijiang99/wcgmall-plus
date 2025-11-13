package com.river.vo.cache;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "缓存键查询参数")
public class CacheKeyQuery {

    @Schema(description = "键名关键字")
    private String key;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
