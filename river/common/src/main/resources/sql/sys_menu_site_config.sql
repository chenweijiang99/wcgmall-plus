-- =====================================================
-- 网站配置菜单
-- =====================================================

-- 查找系统管理菜单ID
SET @parent_id = (SELECT id FROM sys_menu WHERE menu_name = '系统管理' LIMIT 1);

-- 插入网站配置菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `sort`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`)
SELECT '网站配置', @parent_id, 10, 'siteConfig', 'system/siteConfig/index', NULL, 'SiteConfig', 1, 0, 'C', '0', '0', 'sys:siteConfig:update', 'setting', '网站Logo和Footer配置'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '网站配置' AND `parent_id` = @parent_id);
