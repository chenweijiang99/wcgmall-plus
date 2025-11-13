package com.river.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

@Data
@TableName("product")
@Schema(description = "商品对象")
public class Product implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "创建用户ID")
    private Long userId;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "品牌ID")
    private Long brandId;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "库存")
    private Integer inventory;

    @Schema(description = "主图")
    private String image;

    @Schema(description = "详情图片")
    private String detailImages;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "描述图片")
    private String descriptionImage;

    @Schema(description = "标签")
    private String label;

    @Schema(description = "状态 0:下架 1:上架")
    private Integer status;
}
