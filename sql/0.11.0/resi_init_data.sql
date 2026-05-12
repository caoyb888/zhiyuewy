-- ============================================================
-- 肇新智慧物业 · 住宅物业收费模块初始化数据脚本
-- 版本：v0.11.0
-- 日期：2026-05-10
-- 负责人：BE-A
-- 说明：
--   1. base_billrule   住宅收费收据流水规则
--   2. sys_dict_type   6 组数据字典类型
--   3. sys_dict_data   对应字典数据项
--   4. sys_job         6 条定时任务
--   5. sys_menu        "住宅收费"完整菜单树（含权限标识）
-- 执行前提：resi_init.sql 已执行（28 张 resi_ 表已创建）
-- ============================================================

-- ------------------------------------------------------------
-- 1. base_billrule  住宅收费收据流水规则
-- ------------------------------------------------------------
INSERT INTO `base_billrule` (
  `Id`, `FullName`, `EnCode`, `Prefix`, `DateFormat`,
  `Digit`, `StartNumber`, `Example`, `ThisNumber`, `OutputNumber`,
  `SortCode`, `EnabledMark`, `CreatorTime`
) VALUES (
  REPLACE(UUID(), '-', ''),
  '住宅收费收据',
  'RESI_RECEIPT',
  'ZS',
  'yyyyMMdd',
  6,
  '000001',
  'ZS202605100001',
  0,
  'ZS{yyyyMMdd}{000001}',
  10,
  1,
  NOW()
);


-- ------------------------------------------------------------
-- 2. sys_dict_type  6 组数据字典类型
-- ------------------------------------------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('住宅收费-费用类型',   'resi_fee_type',    '0', 'admin', NOW(), 'PERIOD周期费/TEMP临时费/DEPOSIT押金/PRE预收款'),
('住宅收费-计费方式',   'resi_calc_type',   '0', 'admin', NOW(), 'FIXED固定/AREA按面积/USAGE按用量/FORMULA公式'),
('住宅收费-支付方式',   'resi_pay_method',  '0', 'admin', NOW(), 'CASH现金/WECHAT微信/TRANSFER转账/BANK银行'),
('住宅收费-仪表类型',   'resi_meter_type',  '0', 'admin', NOW(), '1水表/2电表/3燃气表/4暖气表'),
('住宅收费-房间类型',   'resi_room_type',   '0', 'admin', NOW(), '1住宅/2商铺/3车库/4储藏室'),
('住宅收费-客户类型',   'resi_customer_type','0','admin', NOW(), '1业主/2租户/3临时');


-- ------------------------------------------------------------
-- 3. sys_dict_data  字典数据项
-- ------------------------------------------------------------
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
-- resi_fee_type (费用类型)
(1, '周期费', 'PERIOD',   'resi_fee_type', 'N', '0', 'admin', NOW(), '周期性收取的费用，如物业费'),
(2, '临时费', 'TEMP',     'resi_fee_type', 'N', '0', 'admin', NOW(), '一次性临时费用'),
(3, '押金',   'DEPOSIT',  'resi_fee_type', 'N', '0', 'admin', NOW(), '装修押金、车位押金等'),
(4, '预收款', 'PRE',      'resi_fee_type', 'N', '0', 'admin', NOW(), '预收款项'),
-- resi_calc_type (计费方式)
(1, '固定金额', 'FIXED',   'resi_calc_type', 'N', '0', 'admin', NOW(), '固定金额计费'),
(2, '按面积',   'AREA',    'resi_calc_type', 'N', '0', 'admin', NOW(), '按建筑面积计费'),
(3, '按用量',   'USAGE',   'resi_calc_type', 'N', '0', 'admin', NOW(), '按水电气用量计费'),
(4, '公式',     'FORMULA', 'resi_calc_type', 'N', '0', 'admin', NOW(), '自定义公式计费（如梯度电价）'),
-- resi_pay_method (支付方式)
(1, '现金',   'CASH',     'resi_pay_method', 'Y', '0', 'admin', NOW(), '现金收款'),
(2, '微信',   'WECHAT',   'resi_pay_method', 'N', '0', 'admin', NOW(), '微信支付'),
(3, '转账',   'TRANSFER', 'resi_pay_method', 'N', '0', 'admin', NOW(), '银行转账'),
(4, '银行',   'BANK',     'resi_pay_method', 'N', '0', 'admin', NOW(), '银行代收'),
(5, '其他',   'OTHER',    'resi_pay_method', 'N', '0', 'admin', NOW(), '其他支付方式'),
-- resi_meter_type (仪表类型)
(1, '水表',   '1', 'resi_meter_type', 'N', '0', 'admin', NOW(), '水表'),
(2, '电表',   '2', 'resi_meter_type', 'N', '0', 'admin', NOW(), '电表'),
(3, '燃气表', '3', 'resi_meter_type', 'N', '0', 'admin', NOW(), '燃气表'),
(4, '暖气表', '4', 'resi_meter_type', 'N', '0', 'admin', NOW(), '暖气表'),
-- resi_room_type (房间类型)
(1, '住宅',   '1', 'resi_room_type', 'Y', '0', 'admin', NOW(), '住宅'),
(2, '商铺',   '2', 'resi_room_type', 'N', '0', 'admin', NOW(), '商铺'),
(3, '车库',   '3', 'resi_room_type', 'N', '0', 'admin', NOW(), '车库'),
(4, '储藏室', '4', 'resi_room_type', 'N', '0', 'admin', NOW(), '储藏室'),
-- resi_customer_type (客户类型)
(1, '业主',   '1', 'resi_customer_type', 'Y', '0', 'admin', NOW(), '业主'),
(2, '租户',   '2', 'resi_customer_type', 'N', '0', 'admin', NOW(), '租户'),
(3, '临时',   '3', 'resi_customer_type', 'N', '0', 'admin', NOW(), '临时客户');


-- ------------------------------------------------------------
-- 4. sys_job  6 条定时任务
-- ------------------------------------------------------------
INSERT INTO `sys_job` (`job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `remark`) VALUES
('住宅收费-管理层日报推送',  'RESI', 'resiDailyReportJob.execute',   '0 0 18 * * ?',    '3', '1', '0', 'admin', NOW(), '每日18:00推送收费日报至管理层'),
('住宅收费-滞纳金自动计算',  'RESI', 'resiOverdueCalcJob.execute',    '0 0 1 * * ?',     '3', '1', '0', 'admin', NOW(), '每日01:00重新计算欠费应收的滞纳金'),
('住宅收费-周期费自动生成',  'RESI', 'resiBillAutoGenJob.execute',    '0 0 2 1 * ?',     '3', '1', '0', 'admin', NOW(), '每月1日02:00自动批量生成当月应收'),
('住宅收费-欠费通知推送',    'RESI', 'resiArrearsNoticeJob.execute',  '0 0 9 10 * ?',    '3', '1', '0', 'admin', NOW(), '每月10日09:00自动发送欠费提醒'),
('住宅收费-推送消息重试',    'RESI', 'resiPushRetryJob.execute',      '0 0 */2 * * ?',   '3', '1', '0', 'admin', NOW(), '每2小时重试失败的推送消息（最多3次）'),
('住宅收费-推送记录清理',    'RESI', 'resiPushCleanJob.execute',      '0 0 3 1 * ?',     '3', '1', '0', 'admin', NOW(), '每月1日清理3个月前已发送成功的推送记录');


-- ------------------------------------------------------------
-- 5. sys_menu  "住宅收费"完整菜单树
-- 说明：
--   menu_id 从 4000 起编，避免与现有菜单冲突
--   权限标识规则：resi:{模块}:{动作}
--   菜单层级：M(目录) → C(页面) → F(按钮)
-- ------------------------------------------------------------

-- === 顶级菜单 ===
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4000, '住宅收费', 0, 11, 'resi', NULL, 'M', '0', '0', NULL, 'home', 'admin', NOW());

-- === 基础档案 ===
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4100, '基础档案', 4000, 1, 'archive', NULL, 'M', '0', '0', NULL, 'tree-table', 'admin', NOW()),
(4101, '项目管理', 4100, 1, 'project', 'resi/archive/project/index', 'C', '0', '0', 'resi:project:list', '#', 'admin', NOW()),
(4102, '楼栋管理', 4100, 2, 'building', 'resi/archive/building/index', 'C', '0', '0', 'resi:building:list', '#', 'admin', NOW()),
(4103, '房间管理', 4100, 3, 'room', 'resi/archive/room/index', 'C', '0', '0', 'resi:room:list', '#', 'admin', NOW()),
(4104, '客户管理', 4100, 4, 'customer', 'resi/archive/customer/index', 'C', '0', '0', 'resi:customer:list', '#', 'admin', NOW()),
(4105, '仪表管理', 4100, 5, 'meter-device', 'resi/archive/meter-device/index', 'C', '0', '0', 'resi:meterDevice:list', '#', 'admin', NOW()),
(4106, '车位管理', 4100, 6, 'parking', 'resi/archive/parking/index', 'C', '0', '0', 'resi:parking:list', '#', 'admin', NOW());

-- === 费用配置 ===
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4200, '费用配置', 4000, 2, 'feeconfig', NULL, 'M', '0', '0', NULL, 'component', 'admin', NOW()),
(4201, '费用定义', 4200, 1, 'definition', 'resi/feeconfig/definition/index', 'C', '0', '0', 'resi:feeDefinition:list', '#', 'admin', NOW()),
(4202, '费用分配', 4200, 2, 'allocation', 'resi/feeconfig/allocation/index', 'C', '0', '0', 'resi:feeAllocation:list', '#', 'admin', NOW()),
(4203, '票据配置', 4200, 3, 'ticket', 'resi/feeconfig/ticket/index', 'C', '0', '0', 'resi:ticketConfig:list', '#', 'admin', NOW()),
(4204, '折扣配置', 4200, 4, 'discount', 'resi/feeconfig/discount/index', 'C', '0', '0', 'resi:discount:list', '#', 'admin', NOW());

-- === 抄表管理 ===
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4300, '抄表管理', 4000, 3, 'meter', 'resi/meter/index', 'C', '0', '0', 'resi:meter:list', 'time-range', 'admin', NOW());

-- === 应收管理 ===
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4400, '应收管理', 4000, 4, 'receivable', 'resi/receivable/index', 'C', '0', '0', 'resi:receivable:list', 'money', 'admin', NOW());

-- === 收银台 ===
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4500, '收银台', 4000, 5, 'cashier', 'resi/cashier/index', 'C', '0', '0', 'resi:cashier:list', 'shopping', 'admin', NOW());

-- === 财务报表 ===
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4600, '财务报表', 4000, 6, 'report', NULL, 'M', '0', '0', NULL, 'chart', 'admin', NOW()),
(4601, '交易汇总', 4600, 1, 'transaction-summary', 'resi/report/transaction-summary/index', 'C', '0', '0', 'resi:report:transactionSummary', '#', 'admin', NOW()),
(4602, '交易明细', 4600, 2, 'transaction-detail', 'resi/report/transaction-detail/index', 'C', '0', '0', 'resi:report:transactionDetail', '#', 'admin', NOW()),
(4603, '收费率报表', 4600, 3, 'collection-rate', 'resi/report/collection-rate/index', 'C', '0', '0', 'resi:report:collectionRate', '#', 'admin', NOW()),
(4604, '欠费明细', 4600, 4, 'arrears-detail', 'resi/report/arrears-detail/index', 'C', '0', '0', 'resi:report:arrearsDetail', '#', 'admin', NOW()),
(4605, '应收管理报表', 4600, 5, 'receivable-mgmt', 'resi/report/receivable-mgmt/index', 'C', '0', '0', 'resi:report:receivableMgmt', '#', 'admin', NOW());

-- === 数据看板 ===
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(4700, '数据看板', 4000, 7, 'dashboard', 'resi/dashboard/index', 'C', '0', '0', 'resi:dashboard:list', 'dashboard', 'admin', NOW());


-- ------------------------------------------------------------
-- 6. sys_menu  按钮权限（F 类型）
-- 每个页面标准配备：query / add / edit / remove / export
-- ------------------------------------------------------------

-- 项目管理按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5101, '项目查询', 4101, 1, '', NULL, 'F', '0', '0', 'resi:project:query', '#', 'admin', NOW()),
(5102, '项目新增', 4101, 2, '', NULL, 'F', '0', '0', 'resi:project:add', '#', 'admin', NOW()),
(5103, '项目修改', 4101, 3, '', NULL, 'F', '0', '0', 'resi:project:edit', '#', 'admin', NOW()),
(5104, '项目删除', 4101, 4, '', NULL, 'F', '0', '0', 'resi:project:remove', '#', 'admin', NOW()),
(5105, '项目导出', 4101, 5, '', NULL, 'F', '0', '0', 'resi:project:export', '#', 'admin', NOW());

-- 楼栋管理按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5106, '楼栋查询', 4102, 1, '', NULL, 'F', '0', '0', 'resi:building:query', '#', 'admin', NOW()),
(5107, '楼栋新增', 4102, 2, '', NULL, 'F', '0', '0', 'resi:building:add', '#', 'admin', NOW()),
(5108, '楼栋修改', 4102, 3, '', NULL, 'F', '0', '0', 'resi:building:edit', '#', 'admin', NOW()),
(5109, '楼栋删除', 4102, 4, '', NULL, 'F', '0', '0', 'resi:building:remove', '#', 'admin', NOW()),
(5110, '楼栋导出', 4102, 5, '', NULL, 'F', '0', '0', 'resi:building:export', '#', 'admin', NOW());

-- 房间管理按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5111, '房间查询', 4103, 1, '', NULL, 'F', '0', '0', 'resi:room:query', '#', 'admin', NOW()),
(5112, '房间新增', 4103, 2, '', NULL, 'F', '0', '0', 'resi:room:add', '#', 'admin', NOW()),
(5113, '房间修改', 4103, 3, '', NULL, 'F', '0', '0', 'resi:room:edit', '#', 'admin', NOW()),
(5114, '房间删除', 4103, 4, '', NULL, 'F', '0', '0', 'resi:room:remove', '#', 'admin', NOW()),
(5115, '房间导出', 4103, 5, '', NULL, 'F', '0', '0', 'resi:room:export', '#', 'admin', NOW());

-- 客户管理按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5116, '客户查询', 4104, 1, '', NULL, 'F', '0', '0', 'resi:customer:query', '#', 'admin', NOW()),
(5117, '客户新增', 4104, 2, '', NULL, 'F', '0', '0', 'resi:customer:add', '#', 'admin', NOW()),
(5118, '客户修改', 4104, 3, '', NULL, 'F', '0', '0', 'resi:customer:edit', '#', 'admin', NOW()),
(5119, '客户删除', 4104, 4, '', NULL, 'F', '0', '0', 'resi:customer:remove', '#', 'admin', NOW()),
(5120, '客户导出', 4104, 5, '', NULL, 'F', '0', '0', 'resi:customer:export', '#', 'admin', NOW());

-- 仪表管理按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5121, '仪表查询', 4105, 1, '', NULL, 'F', '0', '0', 'resi:meterDevice:query', '#', 'admin', NOW()),
(5122, '仪表新增', 4105, 2, '', NULL, 'F', '0', '0', 'resi:meterDevice:add', '#', 'admin', NOW()),
(5123, '仪表修改', 4105, 3, '', NULL, 'F', '0', '0', 'resi:meterDevice:edit', '#', 'admin', NOW()),
(5124, '仪表删除', 4105, 4, '', NULL, 'F', '0', '0', 'resi:meterDevice:remove', '#', 'admin', NOW()),
(5125, '仪表导出', 4105, 5, '', NULL, 'F', '0', '0', 'resi:meterDevice:export', '#', 'admin', NOW());

-- 车位管理按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5126, '车位查询', 4106, 1, '', NULL, 'F', '0', '0', 'resi:parking:query', '#', 'admin', NOW()),
(5127, '车位新增', 4106, 2, '', NULL, 'F', '0', '0', 'resi:parking:add', '#', 'admin', NOW()),
(5128, '车位修改', 4106, 3, '', NULL, 'F', '0', '0', 'resi:parking:edit', '#', 'admin', NOW()),
(5129, '车位删除', 4106, 4, '', NULL, 'F', '0', '0', 'resi:parking:remove', '#', 'admin', NOW()),
(5130, '车位导出', 4106, 5, '', NULL, 'F', '0', '0', 'resi:parking:export', '#', 'admin', NOW());

-- 费用定义按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5201, '费用定义查询', 4201, 1, '', NULL, 'F', '0', '0', 'resi:feeDefinition:query', '#', 'admin', NOW()),
(5202, '费用定义新增', 4201, 2, '', NULL, 'F', '0', '0', 'resi:feeDefinition:add', '#', 'admin', NOW()),
(5203, '费用定义修改', 4201, 3, '', NULL, 'F', '0', '0', 'resi:feeDefinition:edit', '#', 'admin', NOW()),
(5204, '费用定义删除', 4201, 4, '', NULL, 'F', '0', '0', 'resi:feeDefinition:remove', '#', 'admin', NOW()),
(5205, '费用定义导出', 4201, 5, '', NULL, 'F', '0', '0', 'resi:feeDefinition:export', '#', 'admin', NOW());

-- 费用分配按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5206, '费用分配查询', 4202, 1, '', NULL, 'F', '0', '0', 'resi:feeAllocation:query', '#', 'admin', NOW()),
(5207, '费用分配新增', 4202, 2, '', NULL, 'F', '0', '0', 'resi:feeAllocation:add', '#', 'admin', NOW()),
(5208, '费用分配修改', 4202, 3, '', NULL, 'F', '0', '0', 'resi:feeAllocation:edit', '#', 'admin', NOW()),
(5209, '费用分配删除', 4202, 4, '', NULL, 'F', '0', '0', 'resi:feeAllocation:remove', '#', 'admin', NOW()),
(5210, '费用分配导出', 4202, 5, '', NULL, 'F', '0', '0', 'resi:feeAllocation:export', '#', 'admin', NOW());

-- 票据配置按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5211, '票据配置查询', 4203, 1, '', NULL, 'F', '0', '0', 'resi:ticketConfig:query', '#', 'admin', NOW()),
(5212, '票据配置新增', 4203, 2, '', NULL, 'F', '0', '0', 'resi:ticketConfig:add', '#', 'admin', NOW()),
(5213, '票据配置修改', 4203, 3, '', NULL, 'F', '0', '0', 'resi:ticketConfig:edit', '#', 'admin', NOW()),
(5214, '票据配置删除', 4203, 4, '', NULL, 'F', '0', '0', 'resi:ticketConfig:remove', '#', 'admin', NOW()),
(5215, '票据配置导出', 4203, 5, '', NULL, 'F', '0', '0', 'resi:ticketConfig:export', '#', 'admin', NOW());

-- 折扣配置按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5216, '折扣配置查询', 4204, 1, '', NULL, 'F', '0', '0', 'resi:discount:query', '#', 'admin', NOW()),
(5217, '折扣配置新增', 4204, 2, '', NULL, 'F', '0', '0', 'resi:discount:add', '#', 'admin', NOW()),
(5218, '折扣配置修改', 4204, 3, '', NULL, 'F', '0', '0', 'resi:discount:edit', '#', 'admin', NOW()),
(5219, '折扣配置删除', 4204, 4, '', NULL, 'F', '0', '0', 'resi:discount:remove', '#', 'admin', NOW()),
(5220, '折扣配置导出', 4204, 5, '', NULL, 'F', '0', '0', 'resi:discount:export', '#', 'admin', NOW());

-- 抄表管理按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5301, '抄表查询', 4300, 1, '', NULL, 'F', '0', '0', 'resi:meter:query', '#', 'admin', NOW()),
(5302, '抄表录入', 4300, 2, '', NULL, 'F', '0', '0', 'resi:meter:add', '#', 'admin', NOW()),
(5303, '抄表修改', 4300, 3, '', NULL, 'F', '0', '0', 'resi:meter:edit', '#', 'admin', NOW()),
(5304, '抄表删除', 4300, 4, '', NULL, 'F', '0', '0', 'resi:meter:remove', '#', 'admin', NOW()),
(5305, '抄表导出', 4300, 5, '', NULL, 'F', '0', '0', 'resi:meter:export', '#', 'admin', NOW()),
(5306, '抄表入账', 4300, 6, '', NULL, 'F', '0', '0', 'resi:meter:bill', '#', 'admin', NOW()),
(5307, '抄表公摊计算', 4300, 7, '', NULL, 'F', '0', '0', 'resi:meter:share', '#', 'admin', NOW());

-- 应收管理按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5401, '应收查询', 4400, 1, '', NULL, 'F', '0', '0', 'resi:receivable:query', '#', 'admin', NOW()),
(5402, '应收新增', 4400, 2, '', NULL, 'F', '0', '0', 'resi:receivable:add', '#', 'admin', NOW()),
(5403, '应收修改', 4400, 3, '', NULL, 'F', '0', '0', 'resi:receivable:edit', '#', 'admin', NOW()),
(5404, '应收删除', 4400, 4, '', NULL, 'F', '0', '0', 'resi:receivable:remove', '#', 'admin', NOW()),
(5405, '应收导出', 4400, 5, '', NULL, 'F', '0', '0', 'resi:receivable:export', '#', 'admin', NOW());

-- 收银台按钮权限（收银台主要是收款操作，权限设计略有不同）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5501, '收款', 4500, 1, '', NULL, 'F', '0', '0', 'resi:cashier:collect', '#', 'admin', NOW()),
(5502, '退款', 4500, 2, '', NULL, 'F', '0', '0', 'resi:cashier:refund', '#', 'admin', NOW()),
(5503, '冲红', 4500, 3, '', NULL, 'F', '0', '0', 'resi:cashier:writeOff', '#', 'admin', NOW()),
(5504, '预收款', 4500, 4, '', NULL, 'F', '0', '0', 'resi:cashier:prePay', '#', 'admin', NOW()),
(5505, '打印', 4500, 5, '', NULL, 'F', '0', '0', 'resi:cashier:print', '#', 'admin', NOW());

-- 交易汇总按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5601, '交易汇总查询', 4601, 1, '', NULL, 'F', '0', '0', 'resi:report:transactionSummary:query', '#', 'admin', NOW()),
(5602, '交易汇总导出', 4601, 2, '', NULL, 'F', '0', '0', 'resi:report:transactionSummary:export', '#', 'admin', NOW());

-- 交易明细按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5603, '交易明细查询', 4602, 1, '', NULL, 'F', '0', '0', 'resi:report:transactionDetail:query', '#', 'admin', NOW()),
(5604, '交易明细导出', 4602, 2, '', NULL, 'F', '0', '0', 'resi:report:transactionDetail:export', '#', 'admin', NOW());

-- 收费率报表按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5605, '收费率查询', 4603, 1, '', NULL, 'F', '0', '0', 'resi:report:collectionRate:query', '#', 'admin', NOW()),
(5606, '收费率导出', 4603, 2, '', NULL, 'F', '0', '0', 'resi:report:collectionRate:export', '#', 'admin', NOW());

-- 欠费明细按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5607, '欠费明细查询', 4604, 1, '', NULL, 'F', '0', '0', 'resi:report:arrearsDetail:query', '#', 'admin', NOW()),
(5608, '欠费明细导出', 4604, 2, '', NULL, 'F', '0', '0', 'resi:report:arrearsDetail:export', '#', 'admin', NOW());

-- 应收管理报表按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5609, '应收报表查询', 4605, 1, '', NULL, 'F', '0', '0', 'resi:report:receivableMgmt:query', '#', 'admin', NOW()),
(5610, '应收报表导出', 4605, 2, '', NULL, 'F', '0', '0', 'resi:report:receivableMgmt:export', '#', 'admin', NOW());

-- 数据看板按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`) VALUES
(5701, '看板查询', 4700, 1, '', NULL, 'F', '0', '0', 'resi:dashboard:query', '#', 'admin', NOW());
