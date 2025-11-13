package com.river.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "贡献度数据")
public class ContributionData {

    @Schema(description = "日期")
    private String date;

    @Schema(description = "数量")
    private Integer count;
}
