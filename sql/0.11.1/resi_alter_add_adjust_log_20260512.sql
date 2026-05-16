-- ------------------------------------------------------------
-- S6-02 调账记录：新增 resi_adjust_log 表、菜单及权限
-- ------------------------------------------------------------

-- 1. 创建调账记录表
CREATE TABLE IF NOT EXISTS `resi_adjust_log` (
  `id`              VARCHAR(50)  NOT NULL               COMMENT '调账记录ID，UUID',
  `project_id`      BIGINT       NOT NULL               COMMENT '所属项目ID',
  `receivable_id`   VARCHAR(50)  NOT NULL               COMMENT '被调整的应收记录ID',
  `adjust_type`     VARCHAR(20)  NOT NULL               COMMENT '调整类型：AMOUNT金额 PERIOD账期 STATUS状态 OVERDUE_WAIVE减免滞纳金',
  `before_value`    VARCHAR(200) NULL                   COMMENT '调整前的值（字符串快照）',
  `after_value`     VARCHAR(200) NULL                   COMMENT '调整后的值',
  `reason`          VARCHAR(500) NULL                   COMMENT '调整原因',
  `creator_time`    DATETIME     NOT NULL               COMMENT '操作时间',
  `creator_user_id` VARCHAR(50)  NOT NULL               COMMENT '操作人用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_receivable` (`receivable_id`)                COMMENT '查看应收单的调账历史',
  KEY `idx_project_time` (`project_id`, `creator_time`) COMMENT '调账记录报表'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='调账记录（不可删除）';

-- 2. 调账记录页面菜单（放在财务报表目录 4600 下）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4611, '调账记录', 4600, 8, 'adjust-log', 'resi/finance/adjust-log/index', 'C', '0', '0', 'resi:finance:adjustLog:list', 'edit', 'admin', NOW());

-- 3. 调账记录按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5711, '调账记录查询', 4611, 1, '', NULL, 'F', '0', '0', 'resi:finance:adjustLog:query', '#', 'admin', NOW()),
(5712, '调账记录导出', 4611, 2, '', NULL, 'F', '0', '0', 'resi:finance:adjustLog:export', '#', 'admin', NOW());

-- 4. 应收管理-调账权限按钮
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5406, '应收调账', 4400, 6, '', NULL, 'F', '0', '0', 'resi:receivable:adjust', '#', 'admin', NOW());

-- 5. 收银台-减免滞纳金权限按钮
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5506, '减免滞纳金', 4500, 6, '', NULL, 'F', '0', '0', 'resi:cashier:waiveOverdue', '#', 'admin', NOW());
