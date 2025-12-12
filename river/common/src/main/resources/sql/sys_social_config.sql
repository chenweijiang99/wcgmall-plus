-- =====================================================
-- 第三方登录配置表 (优化版)
-- 支持 OAuth2 和聚合登录两种模式的分离管理
-- =====================================================

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS `sys_social_config`;

-- 创建第三方登录配置表
CREATE TABLE `sys_social_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `social_type` varchar(50) NOT NULL COMMENT '登录类型标识(qq/wechat/alipay/weibo/gitee/github等)',
  `social_name` varchar(100) NOT NULL COMMENT '显示名称',
  `login_mode` varchar(20) DEFAULT 'juhe' COMMENT '当前使用的登录模式(juhe/oauth2)',
  `juhe_type_code` int(11) DEFAULT NULL COMMENT '聚合登录类型编号(1=QQ,2=微信,3=支付宝等)',
  `support_oauth2` tinyint(1) DEFAULT 0 COMMENT '是否支持OAuth2(0否 1是)',
  `support_juhe` tinyint(1) DEFAULT 1 COMMENT '是否支持聚合登录(0否 1是)',
  `app_id` varchar(255) DEFAULT NULL COMMENT 'OAuth2 AppId/ClientId',
  `app_secret` varchar(255) DEFAULT NULL COMMENT 'OAuth2 AppSecret/ClientSecret',
  `redirect_url` varchar(500) DEFAULT NULL COMMENT 'OAuth2 回调地址',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态(0禁用 1启用)',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `icon` text DEFAULT NULL COMMENT '图标(存储SVG内容)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_social_type` (`social_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方登录配置表';

-- 插入默认的第三方登录配置数据
-- 聚合登录支持的类型: 1=QQ,2=微信,3=支付宝,4=微博,5=百度,6=华为,7=小米,8=微软,9=钉钉,10=Gitee,11=GitHub,12=抖音
INSERT INTO `sys_social_config` (`social_type`, `social_name`, `juhe_type_code`, `support_oauth2`, `support_juhe`, `status`, `sort`, `icon`, `remark`) VALUES
('qq', 'QQ', 1, 1, 1, 1, 1, 'qq', 'QQ登录，同时支持OAuth2和聚合登录'),
('wechat', '微信', 2, 0, 1, 1, 2, 'wechat', '微信登录，仅支持聚合登录'),
('alipay', '支付宝', 3, 0, 1, 0, 3, 'alipay', '支付宝登录，仅支持聚合登录'),
('weibo', '微博', 4, 1, 1, 0, 4, 'weibo', '微博登录，同时支持OAuth2和聚合登录'),
('baidu', '百度', 5, 0, 1, 0, 5, 'baidu', '百度登录，仅支持聚合登录'),
('huawei', '华为', 6, 0, 1, 0, 6, 'huawei', '华为登录，仅支持聚合登录'),
('xiaomi', '小米', 7, 0, 1, 0, 7, 'xiaomi', '小米登录，仅支持聚合登录'),
('microsoft', '微软', 8, 0, 1, 0, 8, 'microsoft', '微软登录，仅支持聚合登录'),
('dingtalk', '钉钉', 9, 0, 1, 0, 9, 'dingtalk', '钉钉登录，仅支持聚合登录'),
('gitee', 'Gitee', 10, 1, 1, 1, 10, 'gitee', 'Gitee登录，同时支持OAuth2和聚合登录'),
('github', 'GitHub', 11, 1, 1, 1, 11, 'github', 'GitHub登录，同时支持OAuth2和聚合登录'),
('douyin', '抖音', 12, 0, 1, 0, 12, 'douyin', '抖音登录，仅支持聚合登录');

-- 插入全局登录模式配置（如果不存在）
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `remark`)
SELECT '第三方登录模式', 'social_login_mode', 'juhe', 'Y', 'juhe: 聚合登录; oauth2: OAuth2登录'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key` = 'social_login_mode');

-- 插入第三方登录开关配置（如果不存在）
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `remark`)
SELECT '第三方登录开关', 'social_login_enabled', 'Y', 'Y', 'Y: 开启; N: 关闭'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key` = 'social_login_enabled');
