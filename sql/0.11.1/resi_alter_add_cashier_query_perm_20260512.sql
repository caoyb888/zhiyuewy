-- ------------------------------------------------------------
-- 收银台查询权限补充
-- 为 S4-03 收银台查询新增按钮权限
-- ------------------------------------------------------------

-- 收银台查询按钮权限（挂靠在收银台菜单 4500 下）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5506, '收银台查询', 4500, 6, '', NULL, 'F', '0', '0', 'resi:cashier:query', '#', 'admin', NOW());
