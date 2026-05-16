-- ------------------------------------------------------------
-- S6-01 房屋过户：新增过户按钮权限及过户查询报表权限
-- ------------------------------------------------------------

-- 房间管理按钮权限：过户
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5131, '房间过户', 4103, 6, '', NULL, 'F', '0', '0', 'resi:room:transfer', '#', 'admin', NOW());

-- 过户查询报表页面菜单（放在财务报表目录 4600 下）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4611, '过户查询', 4600, 8, 'transfer-query', 'resi/report/transfer-query/index', 'C', '0', '0', 'resi:room:transferQuery', 'tree-table', 'admin', NOW());

-- 过户查询报表按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5614, '过户查询查询', 4611, 1, '', NULL, 'F', '0', '0', 'resi:room:transferQuery:query', '#', 'admin', NOW()),
(5615, '过户查询导出', 4611, 2, '', NULL, 'F', '0', '0', 'resi:room:transferQuery:export', '#', 'admin', NOW());
