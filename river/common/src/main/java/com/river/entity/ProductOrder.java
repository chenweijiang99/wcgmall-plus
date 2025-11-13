package com.river.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

@Data
@TableName("product_order")
@Schema(description = "对象")
public class ProductOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNumber;

    @Schema(description = "订单状态 1待付款 2待发货 3已发货 4已完成 5已取消 6已退款")
    private Integer status;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "收货人姓名")
    private String consignee;

    @Schema(description = "收货地址")
    private String consigneeAddress;

    @Schema(description = "收货人电话")
    private String consigneePhone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "支付方式")
    private Integer payMethod;

    @Schema(description = "支付状态 0未支付 1已支付 2退款")
    private Integer payStatus;

    @Schema(description = "订单金额")
    private BigDecimal amount;

    @Schema(description = "下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime orderTime;

    @Schema(description = "结账时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkoutTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
