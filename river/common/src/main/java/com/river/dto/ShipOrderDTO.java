package com.river.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 发货请求DTO
 */
@Data
@Schema(description = "发货请求")
public class ShipOrderDTO {

    @NotBlank(message = "订单编号不能为空")
    @Schema(description = "订单编号")
    private String orderNumber;

    @NotBlank(message = "发件人姓名不能为空")
    @Schema(description = "发件人姓名")
    private String senderName;

    @NotBlank(message = "发件人电话不能为空")
    @Schema(description = "发件人电话")
    private String senderPhone;

    @NotBlank(message = "发件省不能为空")
    @Schema(description = "发件省")
    private String senderProvince;

    @NotBlank(message = "发件市不能为空")
    @Schema(description = "发件市")
    private String senderCity;

    @NotBlank(message = "发件区/县不能为空")
    @Schema(description = "发件区/县")
    private String senderCounty;

    @NotBlank(message = "发件详细地址不能为空")
    @Schema(description = "发件详细地址")
    private String senderAddress;
}
