-- =====================================================
-- 添加第三方登录配置菜单
-- =====================================================

-- 添加第三方登录配置菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `create_time`, `update_time`, `redirect`, `name`, `hidden`, `perm`, `is_external`) VALUES
(101, 1, 'socialConfig', '\system\socialConfig\index', '第三方登录配置', 7, 'Connection', 'MENU', NOW(), NOW(), '', '', 0, '', 0);

-- 添加第三方登录配置按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `create_time`, `update_time`, `redirect`, `name`, `hidden`, `perm`, `is_external`) VALUES
(102, 101, NULL, NULL, '查询', 1, '', 'BUTTON', NOW(), NOW(), '', '', 1, 'sys:socialConfig:list', 0),
(103, 101, NULL, NULL, '新增', 2, '', 'BUTTON', NOW(), NOW(), '', '', 1, 'sys:socialConfig:add', 0),
(104, 101, NULL, NULL, '修改', 3, '', 'BUTTON', NOW(), NOW(), '', '', 1, 'sys:socialConfig:update', 0),
(105, 101, NULL, NULL, '删除', 4, '', 'BUTTON', NOW(), NOW(), '', '', 1, 'sys:socialConfig:delete', 0);

-- 更新自增ID
ALTER TABLE `sys_menu` AUTO_INCREMENT = 200;