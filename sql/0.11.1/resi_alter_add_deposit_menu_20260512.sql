-- ------------------------------------------------------------
-- S5-03 押金：新增押金台账菜单及权限
-- ------------------------------------------------------------

-- 押金台账页面菜单（放在财务报表目录 4600 下）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4610, '押金台账', 4600, 7, 'deposit', 'resi/finance/deposit/index', 'C', '0', '0', 'resi:finance:deposit:list', 'money', 'admin', NOW());

-- 押金台账按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5611, '押金台账查询', 4610, 1, '', NULL, 'F', '0', '0', 'resi:finance:deposit:query', '#', 'admin', NOW()),
(5612, '押金台账退还', 4610, 2, '', NULL, 'F', '0', '0', 'resi:finance:deposit:refund', '#', 'admin', NOW()),
(5613, '押金台账导出', 4610, 3, '', NULL, 'F', '0', '0', 'resi:finance:deposit:export', '#', 'admin', NOW());
