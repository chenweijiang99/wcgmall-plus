-- 订单物流信息表
CREATE TABLE IF NOT EXISTS order_logistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    order_number VARCHAR(64) NOT NULL COMMENT '订单编号',
    waybill_no VARCHAR(64) COMMENT '顺丰运单号',
    sf_order_id VARCHAR(64) COMMENT '顺丰订单号',
    
    -- 发件人信息
    sender_name VARCHAR(64) COMMENT '发件人姓名',
    sender_phone VARCHAR(32) COMMENT '发件人电话',
    sender_province VARCHAR(32) COMMENT '发件省',
    sender_city VARCHAR(32) COMMENT '发件市',
    sender_county VARCHAR(32) COMMENT '发件区/县',
    sender_address VARCHAR(255) COMMENT '发件详细地址',
    
    -- 状态
    status TINYINT DEFAULT 0 COMMENT '状态：0待下单 1已下单 2运输中 3已签收',
    filter_result TINYINT COMMENT '筛单结果：1人工确认 2可收派 3不可收派',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_order_number (order_number),
    INDEX idx_waybill_no (waybill_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单物流信息表';
