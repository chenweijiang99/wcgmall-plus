package com.river.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderSubmitDTO {
    private Long addressId;      // 选中的地址ID
    private String remark;       // 订单备注
    private List<Long> cartItemIds;
}