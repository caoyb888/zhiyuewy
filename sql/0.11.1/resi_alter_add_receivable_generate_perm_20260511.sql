-- ------------------------------------------------------------
-- 应收批量生成权限补充
-- 为 S4-01 批量生成应收新增菜单权限
-- ------------------------------------------------------------

-- 应收批量生成按钮权限（挂靠在应收管理菜单 4400 下）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5406, '应收批量生成', 4400, 6, '', NULL, 'F', '0', '0', 'resi:receivable:generate', '#', 'admin', NOW());
