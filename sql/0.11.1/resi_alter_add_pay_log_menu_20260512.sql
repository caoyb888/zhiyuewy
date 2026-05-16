-- ------------------------------------------------------------
-- S5-01 退款 + 冲红：新增收款流水菜单及权限
-- ------------------------------------------------------------

-- 收款流水页面菜单（放在财务报表目录 4600 下）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4606, '收款流水', 4600, 6, 'pay-log', 'resi/finance/pay-log/index', 'C', '0', '0', 'resi:finance:paylog:list', 'money', 'admin', NOW());

-- 收款流水按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5607, '收款流水查询', 4606, 1, '', NULL, 'F', '0', '0', 'resi:finance:paylog:query', '#', 'admin', NOW()),
(5608, '收款流水复核', 4606, 2, '', NULL, 'F', '0', '0', 'resi:finance:paylog:verify', '#', 'admin', NOW()),
(5609, '收款流水导出', 4606, 3, '', NULL, 'F', '0', '0', 'resi:finance:paylog:export', '#', 'admin', NOW());
