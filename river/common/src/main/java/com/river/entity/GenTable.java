package com.river.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.river.utils.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "代码生成业务对象 gen_table")
public class GenTable {

    @Schema(description = "表ID")
    private Long tableId;

    @Schema(description = "表名称")
    private String tableName;

    @Schema(description = "表描述")
    private String tableComment;

    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private Date createTime;


    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private Date updateTime;


    // 主键列信息
    private GenTableColumn pkColumn;

    // 分页参数
    private Integer offset;
    private Integer pageSize;

    @Schema(description = "实体类名称")
    private String className;

    @Schema(description = "生成包路径")
    private String packageName;

    @Schema(description = "生成模块名")
    private String moduleName;

    @Schema(description = "生成业务名")
    private String businessName;
}
