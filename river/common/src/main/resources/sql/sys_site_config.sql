-- =====================================================
-- 网站配置表
-- 用于管理网站Logo、Footer等配置
-- =====================================================

DROP TABLE IF EXISTS `sys_site_config`;

CREATE TABLE `sys_site_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text DEFAULT NULL COMMENT '配置值',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `config_group` varchar(50) DEFAULT 'default' COMMENT '配置分组(logo/footer/basic)',
  `config_type` varchar(20) DEFAULT 'text' COMMENT '配置类型(text/image/textarea)',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网站配置表';

-- 插入默认配置数据
INSERT INTO `sys_site_config` (`config_key`, `config_value`, `config_name`, `config_group`, `config_type`, `sort`, `remark`) VALUES
-- Logo配置
('site_admin_logo', '', '后台Logo', 'logo', 'image', 1, '后台管理系统Logo图片URL'),
('site_admin_title', 'WCG管理系统', '后台网站名称', 'logo', 'text', 2, '后台管理系统名称'),
('site_user_header_logo', '', '用户端Header Logo', 'logo', 'image', 3, '用户端网站头部Logo图片URL'),
('site_user_footer_logo', '', '用户端Footer Logo', 'logo', 'image', 4, '用户端网站底部Logo图片URL'),
('site_user_title', 'WCG Store', '用户端网站名称', 'logo', 'text', 5, '用户端网站名称'),

-- Footer配置
('site_footer_description', '"文创购"(英文:WCG,亦称文创购物商城、文创商城),是一个综合性文创购物购物网站。"文创购"是长江师范学院计科2301陈韦江毕业设计。提供100%品质保证的商品,7天无理由退货的售后服务,以及购物积分返现等优质服务。', '网站描述', 'footer', 'textarea', 10, '网站底部描述文字'),
('site_footer_copyright', 'WCG Store. All rights reserved.', '版权信息', 'footer', 'text', 11, '网站底部版权信息'),
('site_footer_address', '长江师范学院', '联系地址', 'footer', 'text', 12, '网站底部联系地址'),
('site_footer_email', '1774532899@qq.com', '联系邮箱', 'footer', 'text', 13, '网站底部联系邮箱'),
('site_footer_icp', '', 'ICP备案号', 'footer', 'text', 14, 'ICP备案号');
