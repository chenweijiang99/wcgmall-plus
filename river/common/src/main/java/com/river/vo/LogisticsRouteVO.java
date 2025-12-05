package com.river.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 物流轨迹VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "物流轨迹信息")
public class LogisticsRouteVO {

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime acceptTime;

    @Schema(description = "操作描述")
    private String remark;

    @Schema(description = "操作码")
    private String opCode;

    @Schema(description = "操作城市")
    private String acceptAddress;
}
