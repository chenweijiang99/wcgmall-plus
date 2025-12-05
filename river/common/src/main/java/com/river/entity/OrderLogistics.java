package com.river.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单物流信息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_logistics")
@Schema(description = "订单物流信息")
public class OrderLogistics implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNumber;

    @Schema(description = "顺丰运单号")
    private String waybillNo;

    @Schema(description = "顺丰订单号")
    private String sfOrderId;

    @Schema(description = "发件人姓名")
    private String senderName;

    @Schema(description = "发件人电话")
    private String senderPhone;

    @Schema(description = "发件省")
    private String senderProvince;

    @Schema(description = "发件市")
    private String senderCity;

    @Schema(description = "发件区/县")
    private String senderCounty;

    @Schema(description = "发件详细地址")
    private String senderAddress;

    @Schema(description = "状态：0待下单 1已下单 2运输中 3已签收")
    private Integer status;

    @Schema(description = "筛单结果：1人工确认 2可收派 3不可收派")
    private Integer filterResult;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
