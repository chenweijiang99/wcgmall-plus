package com.river.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description  = "代码生成业务字段信息")
public class GenTableColumn {

    @Schema(description = "字段主键")
    private Long columnId;

    @Schema(description = "表Id")
    private Long tableId;

    @Schema(description = "字段名称")
    private String columnName;

    @Schema(description = "字段描述")
    private String columnComment;

    @Schema(description = "物理类型")
    private String columnType;

    @Schema(description = "Java属性类型")
    private String javaType;

    @Schema(description = "Java属性名")
    private String javaField;

    @Schema(description = "是否主键（1是）")
    private String isPk;

    @Schema(description = "是否必填（1是）")
    private String isRequired;

    @Schema(description = "是否为插入字段（1是）")
    private String isInsert;

    @Schema(description = "是否编辑字段（1是）")
    private String isEdit;

    @Schema(description = "是否列表字段（1是）")
    private String isList;

    @Schema(description = "是否查询字段（1是）")
    private String isQuery;

    @Schema(description = "查询方式（EQ等于、NE不等于、GT大于、LT小于、LIKE模糊、BETWEEN范围）")
    private String queryType;

    @Schema(description = "显示类型（input文本框、textarea文本域、select下拉框、checkbox复选框、radio单选框、datetime日期控件、img图片上传控件、file文件上传控件、editor富文本控件）")
    private String htmlType;

    @Schema(description = "排序")
    private Integer sort;

    private String columnKey;
}
