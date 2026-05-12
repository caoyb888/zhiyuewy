-- ------------------------------------------------------------
-- 抄表批量导入权限补充
-- 为 S3-02 Excel 批量导入（三步模式）新增菜单权限
-- ------------------------------------------------------------

-- 抄表导入按钮权限（挂靠在抄表管理菜单 4300 下）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5702, '抄表导入', 4300, 6, '', NULL, 'F', '0', '0', 'resi:meter:import', '#', 'admin', NOW());
