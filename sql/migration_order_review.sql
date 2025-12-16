-- ========================================
-- 订单确认收货和商品评价功能 - 数据库迁移脚本
-- 创建时间: 2025-12-15
-- ========================================

-- 1. 修改订单详情表，增加确认收货状态字段
ALTER TABLE `order_detail`
ADD COLUMN `confirm_status` TINYINT(2) DEFAULT 0 COMMENT '商品确认收货状态: 0=未确认, 1=已确认' AFTER `product_number`,
ADD COLUMN `confirm_time` DATETIME DEFAULT NULL COMMENT '商品确认收货时间' AFTER `confirm_status`;

-- 2. 修改订单表，增加部分收货状态注释
-- 订单状态说明:
-- 0: 待付款
-- 1: 待发货
-- 2: 待收货
-- 3: 待评价
-- 4: 已完成
-- 5: 已取消
-- 6: 已退款
-- 7: 部分收货 (新增)

-- 3. 创建商品评价表
CREATE TABLE IF NOT EXISTS `product_review` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_number` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '评价用户ID',
  `product_score` TINYINT(1) NOT NULL COMMENT '商品评分 1-5分',
  `logistics_score` TINYINT(1) NOT NULL COMMENT '物流评分 1-5分',
  `merchant_score` TINYINT(1) NOT NULL COMMENT '商家评分 1-5分',
  `content` TEXT COMMENT '评价内容(Markdown格式)',
  `images` TEXT COMMENT '评价图片URL，多个用逗号隔开',
  `parent_review_id` BIGINT(20) DEFAULT NULL COMMENT '父评价ID(回复时使用)',
  `root_review_id` BIGINT(20) DEFAULT NULL COMMENT '根评价ID(树形结构)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_number` (`order_number`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_root_review_id` (`root_review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- 4. 创建评价图片表(可选，如果需要更灵活的图片管理)
CREATE TABLE IF NOT EXISTS `review_images` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `review_id` BIGINT(20) NOT NULL COMMENT '关联评价ID',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `sort_order` INT(11) DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价图片表';

-- 5. 数据迁移：将现有商品评论从comments表迁移到product_review表
-- 注意：此步骤需要根据实际情况调整，因为comments表结构与product_review不完全匹配
-- INSERT INTO `product_review` (`order_number`, `product_id`, `user_id`, `product_score`, `logistics_score`, `merchant_score`, `content`, `parent_review_id`, `root_review_id`, `create_time`, `update_time`)
-- SELECT
--   '' as order_number,  -- 需要关联订单号
--   fid as product_id,
--   user_id,
--   5 as product_score,   -- 默认5分，需要手动调整
--   5 as logistics_score,
--   5 as merchant_score,
--   comment as content,
--   pid as parent_review_id,
--   root_id as root_review_id,
--   create_time,
--   update_time
-- FROM `comments`
-- WHERE module = 'product';

-- 6. 添加索引优化查询性能
CREATE INDEX `idx_confirm_status` ON `order_detail` (`confirm_status`);
CREATE INDEX `idx_order_product` ON `order_detail` (`order_number`, `product_id`);

-- ========================================
-- 脚本执行完成
-- ========================================
