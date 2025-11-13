package com.river.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据表
 */
@Data
@TableName("sys_dict_data")
@Schema(description = "字典数据")
public class SysDictData implements Serializable {


    @TableId(type = IdType.AUTO)
    @Schema(description = "字典数据id")
    private Long id;

    @Schema(description = "字典类型id")
    private Long dictId;

    @Schema(description = "字典标签")
    private String label;

    @Schema(description = "字典键值")
    private String value;

    @Schema(description = "回显样式")
    private String style;

    @Schema(description = "是否默认（1是 0否）")
    private Integer isDefault;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态")
    private Integer status;
}
