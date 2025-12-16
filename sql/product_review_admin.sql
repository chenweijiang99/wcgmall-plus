-- 商品评价表 - 添加管理员回复标识
-- 执行此SQL为product_review表添加is_admin字段

ALTER TABLE product_review ADD COLUMN is_admin TINYINT(1) DEFAULT 0 COMMENT '是否管理员回复: 0-用户, 1-管理员';
ALTER TABLE product_review ADD COLUMN admin_id BIGINT DEFAULT NULL COMMENT '管理员ID(当is_admin=1时)';

-- 创建索引优化查询
CREATE INDEX idx_product_review_is_admin ON product_review(is_admin);
CREATE INDEX idx_product_review_product_id ON product_review(product_id);
CREATE INDEX idx_product_review_create_time ON product_review(create_time);
