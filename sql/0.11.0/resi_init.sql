-- ============================================================
-- 肇新智慧物业 · 住宅物业收费模块数据库初始化脚本
-- 版本：v0.11.0
-- 日期：2026-05-10
-- 负责人：BE-A
-- 说明：本脚本包含全部 28 张 resi_ 前缀表的 DDL，按层次顺序执行
--       档案层 → 配置层 → 计费层 → 收款层 → C端层
-- 字符集：utf8mb4 / utf8mb4_unicode_ci
-- 引擎：InnoDB
-- ============================================================

-- ============================================================
-- 1. 档案层（9张表）
-- 主键：BIGINT AUTO_INCREMENT
-- 公共字段：enabled_mark, create_by, create_time, update_by, update_time
-- ============================================================

-- ------------------------------------------------------------
-- 1. resi_project  项目（小区）
-- ------------------------------------------------------------
CREATE TABLE `resi_project` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '项目ID，自增主键',
  `code`          VARCHAR(50)  NOT NULL                 COMMENT '项目编号，全局唯一，如 PRJ-001',
  `name`          VARCHAR(100) NOT NULL                 COMMENT '项目名称，如 阳光花园小区',
  `address`       VARCHAR(255) NULL                     COMMENT '详细地址',
  `contact_phone` VARCHAR(20)  NULL                     COMMENT '项目服务热线',
  `manager_name`  VARCHAR(50)  NULL                     COMMENT '项目负责人姓名',
  `manager_phone` VARCHAR(20)  NULL                     COMMENT '项目负责人手机',
  `logo_url`      VARCHAR(500) NULL                     COMMENT '项目Logo图片路径，用于C端展示',
  `seal_url`      VARCHAR(500) NULL                     COMMENT '公章图片路径，用于收据打印',
  `enabled_mark`  TINYINT      NOT NULL DEFAULT 1       COMMENT '有效标志：1有效 0无效',
  `create_by`     VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '创建者账号',
  `create_time`   DATETIME     NOT NULL                 COMMENT '创建时间',
  `update_by`     VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '最后更新者账号',
  `update_time`   DATETIME     NOT NULL                 COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_code` (`code`)                 COMMENT '项目编号唯一约束'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='住宅项目（小区）';

-- ------------------------------------------------------------
-- 2. resi_building  楼栋
-- ------------------------------------------------------------
CREATE TABLE `resi_building` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '楼栋ID，自增主键',
  `project_id`   BIGINT       NOT NULL                 COMMENT '所属项目ID，关联 resi_project.id',
  `name`         VARCHAR(100) NOT NULL                 COMMENT '楼栋名称，如 1号楼、A栋',
  `number`       VARCHAR(26)  NOT NULL                 COMMENT '楼栋编号，用于简码检索',
  `floors`       INT          NULL                     COMMENT '总楼层数',
  `enabled_mark` TINYINT      NOT NULL DEFAULT 1       COMMENT '有效标志：1有效 0无效',
  `create_by`    VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '创建者账号',
  `create_time`  DATETIME     NOT NULL                 COMMENT '创建时间',
  `update_by`    VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '最后更新者账号',
  `update_time`  DATETIME     NOT NULL                 COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)                  COMMENT '按项目查楼栋'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='楼栋管理';

-- ------------------------------------------------------------
-- 3. resi_room  房间档案
-- ------------------------------------------------------------
CREATE TABLE `resi_room` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '房间ID，自增主键',
  `project_id`    BIGINT       NOT NULL                 COMMENT '所属项目ID',
  `building_id`   BIGINT       NOT NULL                 COMMENT '所属楼栋ID，关联 resi_building.id',
  `unit_no`       VARCHAR(20)  NULL                     COMMENT '单元号，如 1单元、A单元，NULL表示无单元结构',
  `floor_no`      INT          NULL                     COMMENT '楼层，负数表示地下层',
  `room_no`       VARCHAR(50)  NOT NULL                 COMMENT '房号，如 101、1001',
  `room_alias`    VARCHAR(100) NULL                     COMMENT '房间简称，收银台搜索关键词，如 1栋1单元101',
  `building_area` DECIMAL(10,2) NULL                    COMMENT '建筑面积（㎡），用于按面积计费',
  `inner_area`    DECIMAL(10,2) NULL                    COMMENT '套内面积（㎡）',
  `room_type`     TINYINT      NOT NULL DEFAULT 1       COMMENT '房间类型：1住宅 2商铺 3车库 4储藏室',
  `garage_no`     VARCHAR(50)  NULL                     COMMENT '附属车库编号，用于与车库档案关联',
  `storage_no`    VARCHAR(50)  NULL                     COMMENT '附属储藏室编号',
  `state`         VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT '使用状态：NORMAL正常 VACANT空置 DECORATING装修 TRANSFERRED已过户',
  `remark`        VARCHAR(500) NULL                     COMMENT '备注',
  `enabled_mark`  TINYINT      NOT NULL DEFAULT 1       COMMENT '有效标志：1有效 0无效',
  `create_by`     VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '创建者账号',
  `create_time`   DATETIME     NOT NULL                 COMMENT '创建时间',
  `update_by`     VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '最后更新者账号',
  `update_time`   DATETIME     NOT NULL                 COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room` (`project_id`, `building_id`, `unit_no`, `room_no`)  COMMENT '同楼栋同单元房号不重复',
  KEY `idx_project_building` (`project_id`, `building_id`)                   COMMENT '按项目楼栋查房间',
  KEY `idx_room_alias` (`room_alias`)                                         COMMENT '收银台模糊搜索'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='房间档案（住宅/车库/储藏室）';

-- ------------------------------------------------------------
-- 4. resi_customer  客户（业主/租户）档案
-- ------------------------------------------------------------
CREATE TABLE `resi_customer` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '客户ID，自增主键',
  `project_id`    BIGINT       NOT NULL                 COMMENT '所属项目ID',
  `customer_name` VARCHAR(100) NOT NULL                 COMMENT '客户姓名',
  `phone`         VARCHAR(20)  NOT NULL                 COMMENT '联系电话',
  `id_card`       VARCHAR(64)  NULL                     COMMENT '身份证号（AES-256加密存储，展示时脱敏）',
  `gender`        TINYINT      NULL                     COMMENT '性别：0未知 1男 2女',
  `customer_type` TINYINT      NOT NULL DEFAULT 1       COMMENT '客户类型：1业主 2租户 3临时',
  `openid`        VARCHAR(128) NULL                     COMMENT '微信openid，C端扫码绑定后填入，用于推送消息',
  `enabled_mark`  TINYINT      NOT NULL DEFAULT 1       COMMENT '有效标志：1有效 0无效',
  `create_by`     VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '创建者账号',
  `create_time`   DATETIME     NOT NULL                 COMMENT '创建时间',
  `update_by`     VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '最后更新者账号',
  `update_time`   DATETIME     NOT NULL                 COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_phone` (`project_id`, `phone`)       COMMENT '按项目和手机号查客户',
  KEY `idx_openid` (`openid`)                           COMMENT 'C端通过openid快速定位客户'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='客户（业主/租户）档案';

-- ------------------------------------------------------------
-- 5. resi_customer_asset  客户资产绑定
-- ------------------------------------------------------------
CREATE TABLE `resi_customer_asset` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '绑定记录ID',
  `customer_id` BIGINT      NOT NULL                 COMMENT '客户ID，关联 resi_customer.id',
  `project_id`  BIGINT      NOT NULL                 COMMENT '项目ID',
  `asset_type`  TINYINT     NOT NULL                 COMMENT '资产类型：1房间 2车位 3储藏室',
  `asset_id`    BIGINT      NOT NULL                 COMMENT '资产ID，根据 asset_type 关联对应表主键',
  `bind_date`   DATE        NOT NULL                 COMMENT '绑定生效日期',
  `unbind_date` DATE        NULL                     COMMENT '解除绑定日期，NULL表示当前有效',
  `is_current`  TINYINT     NOT NULL DEFAULT 1       COMMENT '是否当前有效绑定：1是 0否（过户后置0）',
  `create_by`   VARCHAR(64) NOT NULL DEFAULT ''      COMMENT '创建者账号',
  `create_time` DATETIME    NOT NULL                 COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer` (`customer_id`)                  COMMENT '按客户查其所有资产',
  KEY `idx_asset` (`asset_type`, `asset_id`, `is_current`) COMMENT '按资产查当前业主'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='客户资产绑定关系';

-- ------------------------------------------------------------
-- 6. resi_meter_device  仪表档案
-- ------------------------------------------------------------
CREATE TABLE `resi_meter_device` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '仪表ID，自增主键',
  `project_id`   BIGINT        NOT NULL                 COMMENT '所属项目ID',
  `room_id`      BIGINT        NULL                     COMMENT '所属房间ID，公摊总表可为NULL',
  `meter_code`   VARCHAR(50)   NOT NULL                 COMMENT '仪表编号，物理铭牌号，项目内唯一',
  `meter_type`   TINYINT       NOT NULL                 COMMENT '仪表类型：1水表 2电表 3燃气表 4暖气表',
  `install_date` DATE          NULL                     COMMENT '安装日期',
  `init_reading` DECIMAL(12,4) NOT NULL DEFAULT 0       COMMENT '初始读数（安装时的底数）',
  `multiplier`   DECIMAL(10,4) NOT NULL DEFAULT 1       COMMENT '倍率，实际用量=读数差×倍率，默认1.0000',
  `is_public`    TINYINT       NOT NULL DEFAULT 0       COMMENT '是否公摊总表：0分户表 1公摊总表',
  `public_group` VARCHAR(50)   NULL                     COMMENT '公摊组编号，同一楼栋/单元的表用相同编号，用于公摊计算',
  `enabled_mark` TINYINT       NOT NULL DEFAULT 1       COMMENT '有效标志：1有效 0无效',
  `create_by`    VARCHAR(64)   NOT NULL DEFAULT ''      COMMENT '创建者账号',
  `create_time`  DATETIME      NOT NULL                 COMMENT '创建时间',
  `update_by`    VARCHAR(64)   NOT NULL DEFAULT ''      COMMENT '最后更新者账号',
  `update_time`  DATETIME      NOT NULL                 COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meter_code` (`project_id`, `meter_code`) COMMENT '同项目仪表编号唯一',
  KEY `idx_room_id` (`room_id`)                            COMMENT '按房间查挂表'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='仪表档案（水/电/燃气/暖气）';

-- ------------------------------------------------------------
-- 7. resi_parking_area  车场区域
-- ------------------------------------------------------------
CREATE TABLE `resi_parking_area` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '区域ID，自增主键',
  `project_id`   BIGINT       NOT NULL                 COMMENT '所属项目ID',
  `area_code`    VARCHAR(50)  NOT NULL                 COMMENT '区域编号，如 A区、B1层',
  `area_name`    VARCHAR(100) NULL                     COMMENT '区域名称',
  `total_count`  INT          NOT NULL DEFAULT 0       COMMENT '区域内车位总数（统计用）',
  `enabled_mark` TINYINT      NOT NULL DEFAULT 1       COMMENT '有效标志：1有效 0无效',
  `create_by`    VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '创建者账号',
  `create_time`  DATETIME     NOT NULL                 COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)  COMMENT '按项目查区域'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='车场区域';

-- ------------------------------------------------------------
-- 8. resi_parking_space  车位
-- ------------------------------------------------------------
CREATE TABLE `resi_parking_space` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '车位ID，自增主键',
  `project_id`    BIGINT      NOT NULL                 COMMENT '所属项目ID',
  `area_id`       BIGINT      NOT NULL                 COMMENT '所属区域ID，关联 resi_parking_area.id',
  `space_code`    VARCHAR(50) NOT NULL                 COMMENT '车位编号，如 A-001',
  `space_type`    TINYINT     NULL                     COMMENT '车位类型：1地上 2地下 3机械',
  `property_type` TINYINT     NULL                     COMMENT '产权类型：1产权 2租赁 3公共',
  `owner_id`      BIGINT      NULL                     COMMENT '产权人客户ID，关联 resi_customer.id',
  `state`         VARCHAR(20) NOT NULL DEFAULT 'IDLE'  COMMENT '使用状态：IDLE空闲 OCCUPIED占用 SOLD出售',
  `enabled_mark`  TINYINT     NOT NULL DEFAULT 1       COMMENT '有效标志：1有效 0无效',
  `create_by`     VARCHAR(64) NOT NULL DEFAULT ''      COMMENT '创建者账号',
  `create_time`   DATETIME    NOT NULL                 COMMENT '创建时间',
  `update_by`     VARCHAR(64) NOT NULL DEFAULT ''      COMMENT '最后更新者账号',
  `update_time`   DATETIME    NOT NULL                 COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_area_id` (`area_id`)         COMMENT '按区域查车位',
  KEY `idx_project_id` (`project_id`)   COMMENT '按项目查车位'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='车位管理';

-- ------------------------------------------------------------
-- 9. resi_room_transfer  房屋过户记录
-- ------------------------------------------------------------
CREATE TABLE `resi_room_transfer` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '过户记录ID',
  `project_id`      BIGINT       NOT NULL                 COMMENT '所属项目ID',
  `room_id`         BIGINT       NOT NULL                 COMMENT '房间ID，关联 resi_room.id',
  `old_customer_id` BIGINT       NOT NULL                 COMMENT '原业主客户ID',
  `new_customer_id` BIGINT       NOT NULL                 COMMENT '新业主客户ID',
  `transfer_date`   DATE         NOT NULL                 COMMENT '过户生效日期',
  `transfer_remark` VARCHAR(500) NULL                     COMMENT '过户备注',
  `operator`        VARCHAR(64)  NULL                     COMMENT '操作员账号',
  `create_time`     DATETIME     NOT NULL                 COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`),                          
  KEY `idx_project_date` (`project_id`, `transfer_date`)  COMMENT '过户查询报表'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='房屋过户记录（不可删除，审计用）';


-- ============================================================
-- 2. 收费配置层（4张表）
-- 主键：VARCHAR(50) UUID
-- ============================================================

-- ------------------------------------------------------------
-- 10. resi_fee_definition  费用定义
-- ------------------------------------------------------------
CREATE TABLE `resi_fee_definition` (
  `id`                  VARCHAR(50)   NOT NULL               COMMENT '费用定义ID，UUID',
  `project_id`          BIGINT        NOT NULL               COMMENT '所属项目ID',
  `fee_name`            VARCHAR(100)  NOT NULL               COMMENT '费用名称，如 物业管理费、水费',
  `fee_code`            VARCHAR(50)   NOT NULL               COMMENT '费用编码，项目内唯一，用于系统内部标识',
  `fee_type`            VARCHAR(20)   NOT NULL               COMMENT '费用类型：PERIOD周期费 TEMP临时费 DEPOSIT押金 PRE预收款',
  `calc_type`           VARCHAR(20)   NOT NULL               COMMENT '计费方式：FIXED固定金额 AREA按面积 USAGE按用量 FORMULA自定义公式',
  `unit_price`          DECIMAL(12,4) NULL                   COMMENT '单价（元），FIXED时为总金额，AREA时为元/㎡，USAGE时为元/单位用量',
  `formula`             TEXT          NULL                   COMMENT '自定义计费公式（FORMULA类型时有效），支持梯度，语法：if/elsif/else/return，变量：单价、数量',
  `cycle_unit`          VARCHAR(10)   NULL                   COMMENT '计费周期单位：MONTH月 QUARTER季 YEAR年，TEMP/DEPOSIT类型为NULL',
  `cycle_value`         INT           NOT NULL DEFAULT 1     COMMENT '周期数，如每2个月收一次则填2，配合 cycle_unit 使用',
  `overdue_enable`      TINYINT       NOT NULL DEFAULT 0     COMMENT '是否启用滞纳金：0否 1是',
  `overdue_days`        INT           NOT NULL DEFAULT 0     COMMENT '逾期起算天数，到期后超过N天开始计滞纳金',
  `overdue_type`        VARCHAR(10)   NULL                   COMMENT '滞纳金计算类型：DAY按天 MONTH按月',
  `overdue_rate`        DECIMAL(8,6)  NULL                   COMMENT '滞纳金利率，如 0.000500 = 日万分之五（0.05%/天）',
  `overdue_max`         DECIMAL(12,2) NULL                   COMMENT '滞纳金累计上限（元），NULL表示不限上限',
  `round_type`          VARCHAR(10)   NOT NULL DEFAULT 'ROUND' COMMENT '金额取整方式：ROUND四舍五入 CEIL向上取整 FLOOR截尾',
  `earmark_enable`      TINYINT       NOT NULL DEFAULT 0     COMMENT '专款专冲标记：0否 1是（预收款只能冲该费用）',
  `invoice_title`       VARCHAR(200)  NULL                   COMMENT '发票项目名称，用于开票',
  `tax_rate`            DECIMAL(5,2)  NULL                   COMMENT '增值税税率（%），如 3.00、6.00、9.00',
  `sort_code`           INT           NOT NULL DEFAULT 0     COMMENT '排序码，数字越小越靠前',
  `enabled_mark`        TINYINT       NOT NULL DEFAULT 1     COMMENT '有效标志：1有效 0无效',
  `creator_time`        DATETIME      NOT NULL               COMMENT '创建时间',
  `creator_user_id`     VARCHAR(50)   NOT NULL               COMMENT '创建人用户ID',
  `last_modify_time`    DATETIME      NULL                   COMMENT '最后修改时间',
  `last_modify_user_id` VARCHAR(50)   NULL                   COMMENT '最后修改人用户ID',
  `delete_time`         DATETIME      NULL                   COMMENT '软删除时间',
  `delete_user_id`      VARCHAR(50)   NULL                   COMMENT '执行软删除的用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_project_type` (`project_id`, `fee_type`)           COMMENT '按项目和费用类型查询'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='费用定义（收费项规则配置）';

-- ------------------------------------------------------------
-- 11. resi_fee_allocation  费用分配
-- ------------------------------------------------------------
CREATE TABLE `resi_fee_allocation` (
  `id`                  VARCHAR(50)   NOT NULL               COMMENT '分配记录ID，UUID',
  `project_id`          BIGINT        NOT NULL               COMMENT '所属项目ID',
  `fee_id`              VARCHAR(50)   NOT NULL               COMMENT '费用定义ID，关联 resi_fee_definition.id',
  `resource_type`       VARCHAR(20)   NOT NULL               COMMENT '资源类型：ROOM房间 PARKING车位 STORAGE储藏室',
  `resource_id`         BIGINT        NOT NULL               COMMENT '资源ID，根据 resource_type 关联对应表主键',
  `resource_name`       VARCHAR(100)  NULL                   COMMENT '资源名称（冗余）,如 1栋101，避免关联查询',
  `custom_price`        DECIMAL(12,4) NULL                   COMMENT '个性化单价，NULL则使用 resi_fee_definition.unit_price',
  `custom_formula`      TEXT          NULL                   COMMENT '个性化公式，NULL则使用费用定义中的公式',
  `start_date`          DATE          NOT NULL               COMMENT '费用分配生效日期',
  `end_date`            DATE          NULL                   COMMENT '费用分配截止日期，NULL表示长期有效',
  `enabled_mark`        TINYINT       NOT NULL DEFAULT 1     COMMENT '有效标志：1有效 0无效',
  `creator_time`        DATETIME      NOT NULL               COMMENT '创建时间',
  `creator_user_id`     VARCHAR(50)   NOT NULL               COMMENT '创建人用户ID',
  `last_modify_time`    DATETIME      NULL                   COMMENT '最后修改时间',
  `last_modify_user_id` VARCHAR(50)   NULL                   COMMENT '最后修改人用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alloc` (`fee_id`, `resource_type`, `resource_id`, `start_date`) COMMENT '同资源同费用同生效日唯一',
  KEY `idx_resource` (`resource_type`, `resource_id`)                             COMMENT '按资源查已分配费用',
  KEY `idx_project_fee` (`project_id`, `fee_id`)                                  COMMENT '按项目和费用查分配'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='费用分配（费用定义→具体资源）';

-- ------------------------------------------------------------
-- 12. resi_ticket_config  票据模板配置
-- ------------------------------------------------------------
CREATE TABLE `resi_ticket_config` (
  `id`           VARCHAR(50)  NOT NULL               COMMENT '配置ID，UUID',
  `project_id`   BIGINT       NOT NULL               COMMENT '所属项目ID',
  `ticket_type`  TINYINT      NOT NULL               COMMENT '票据类型：1收款单 2缴费通知单',
  `title`        VARCHAR(200) NULL                   COMMENT '票据标题，如 XX物业服务中心收款凭证',
  `collect_org`  VARCHAR(200) NULL                   COMMENT '收款单位全称，打印在票据上',
  `paper_size`   VARCHAR(20)  NULL                   COMMENT '纸张规格：A4 / A5 / ROLL（连续纸）',
  `logo_url`     VARCHAR(500) NULL                   COMMENT '公司Logo图片路径',
  `seal_url`     VARCHAR(500) NULL                   COMMENT '公章图片路径（打印在收款单底部）',
  `remark`       VARCHAR(500) NULL                   COMMENT '收据固定备注文字',
  `field_config` JSON         NULL                   COMMENT '表单字段配置，JSON数组，每项包含字段名、显示名、是否显示、排序',
  `enabled_mark` TINYINT      NOT NULL DEFAULT 1     COMMENT '有效标志：1有效 0无效',
  `create_by`    VARCHAR(64)  NOT NULL DEFAULT ''    COMMENT '创建者账号',
  `create_time`  DATETIME     NOT NULL               COMMENT '创建时间',
  `update_by`    VARCHAR(64)  NOT NULL DEFAULT ''    COMMENT '最后更新者账号',
  `update_time`  DATETIME     NOT NULL               COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_type` (`project_id`, `ticket_type`)  COMMENT '按项目和票据类型查配置'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='票据模板配置（收款单/缴费通知单）';

-- ------------------------------------------------------------
-- 13. resi_discount  折扣配置
-- ------------------------------------------------------------
CREATE TABLE `resi_discount` (
  `id`            VARCHAR(50)  NOT NULL               COMMENT '折扣ID，UUID',
  `project_id`    BIGINT       NOT NULL               COMMENT '所属项目ID',
  `discount_name` VARCHAR(100) NOT NULL               COMMENT '折扣名称，如 一次性年缴9.5折',
  `discount_rate` DECIMAL(5,4) NOT NULL               COMMENT '折扣比例：0.9500表示95折，1.0000表示无折扣',
  `fee_scope`     JSON         NULL                   COMMENT '适用费用ID列表（JSON数组），NULL表示适用全部费用',
  `valid_start`   DATE         NULL                   COMMENT '折扣有效期开始日，NULL表示即时生效',
  `valid_end`     DATE         NULL                   COMMENT '折扣有效期截止日，NULL表示长期有效',
  `enabled_mark`  TINYINT      NOT NULL DEFAULT 1     COMMENT '有效标志：1有效 0无效',
  `create_by`     VARCHAR(64)  NOT NULL DEFAULT ''    COMMENT '创建者账号',
  `create_time`   DATETIME     NOT NULL               COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_valid` (`project_id`, `valid_start`, `valid_end`)  COMMENT '按项目和有效期查可用折扣'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='折扣配置';


-- ============================================================
-- 3. 计费层（2张表）
-- 主键：VARCHAR(50) UUID
-- ============================================================

-- ------------------------------------------------------------
-- 14. resi_meter_reading  抄表记录
-- ------------------------------------------------------------
CREATE TABLE `resi_meter_reading` (
  `id`                  VARCHAR(50)   NOT NULL               COMMENT '抄表记录ID，UUID',
  `project_id`          BIGINT        NOT NULL               COMMENT '所属项目ID',
  `meter_id`            BIGINT        NOT NULL               COMMENT '仪表ID，关联 resi_meter_device.id',
  `room_id`             BIGINT        NULL                   COMMENT '所属房间ID（冗余，避免关联查询）',
  `fee_id`              VARCHAR(50)   NULL                   COMMENT '关联费用定义ID，入账时关联',
  `period`              VARCHAR(7)    NOT NULL               COMMENT '抄表期间，格式 yyyy-MM，如 2026-05',
  `last_reading`        DECIMAL(12,4) NULL                   COMMENT '上次表读数（自动从上期记录带入）',
  `last_date`           DATE          NULL                   COMMENT '上次抄表日期',
  `curr_reading`        DECIMAL(12,4) NOT NULL               COMMENT '本次表读数（录入值）',
  `curr_date`           DATE          NOT NULL               COMMENT '本次抄表日期',
  `raw_usage`           DECIMAL(12,4) NULL                   COMMENT '原始用量 = (curr_reading - last_reading) × 倍率',
  `loss_rate`           DECIMAL(5,4)  NOT NULL DEFAULT 0     COMMENT '损耗比率，0表示无损耗，0.0300表示3%损耗',
  `loss_amount`         DECIMAL(12,4) NOT NULL DEFAULT 0     COMMENT '损耗量 = raw_usage × loss_rate',
  `share_amount`        DECIMAL(12,4) NOT NULL DEFAULT 0     COMMENT '公摊分摊量（从公摊计算结果写入）',
  `billed_usage`        DECIMAL(12,4) NULL                   COMMENT '实际计费用量 = raw_usage - loss_amount + share_amount',
  `status`              VARCHAR(20)   NOT NULL DEFAULT 'INPUT' COMMENT '状态：INPUT已录入 BILLED已入账 VERIFIED已复核',
  `import_batch`        VARCHAR(50)   NULL                   COMMENT '批量导入批次号，手动录入时为NULL',
  `reader_id`           VARCHAR(64)   NULL                   COMMENT '抄表员用户ID',
  `receivable_id`       VARCHAR(50)   NULL                   COMMENT '入账后对应的应收记录ID，关联 resi_receivable.id',
  `creator_time`        DATETIME      NOT NULL               COMMENT '创建时间',
  `creator_user_id`     VARCHAR(50)   NOT NULL               COMMENT '创建人用户ID',
  `last_modify_time`    DATETIME      NULL                   COMMENT '最后修改时间',
  `last_modify_user_id` VARCHAR(50)   NULL                   COMMENT '最后修改人用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meter_period` (`meter_id`, `period`)            COMMENT '同仪表同期间只能有一条记录',
  KEY `idx_project_period` (`project_id`, `period`)              COMMENT '按项目和期间查抄表',
  KEY `idx_room_period` (`room_id`, `period`)                    COMMENT '按房间和期间查抄表',
  KEY `idx_status` (`status`)                                    COMMENT '按状态查待入账记录'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='抄表记录';

-- ------------------------------------------------------------
-- 15. resi_receivable  应收账单（系统核心表）
-- ------------------------------------------------------------
CREATE TABLE `resi_receivable` (
  `id`                  VARCHAR(50)   NOT NULL               COMMENT '应收记录ID，UUID',
  `project_id`          BIGINT        NOT NULL               COMMENT '所属项目ID',
  `resource_type`       VARCHAR(20)   NOT NULL               COMMENT '资源类型：ROOM房间 PARKING车位 STORAGE储藏室',
  `resource_id`         BIGINT        NOT NULL               COMMENT '资源ID',
  `resource_name`       VARCHAR(100)  NULL                   COMMENT '资源名称（冗余）',
  `customer_id`         BIGINT        NULL                   COMMENT '客户ID（冗余）',
  `customer_name`       VARCHAR(100)  NULL                   COMMENT '客户姓名（冗余）',
  `fee_id`              VARCHAR(50)   NOT NULL               COMMENT '费用定义ID',
  `fee_name`            VARCHAR(100)  NOT NULL               COMMENT '费用名称（冗余，防止改名影响历史数据）',
  `fee_type`            VARCHAR(20)   NOT NULL               COMMENT '费用类型：PERIOD周期 TEMP临时 DEPOSIT押金 PRE预收',
  `bill_period`         VARCHAR(7)    NULL                   COMMENT '账单月份，格式 yyyy-MM，临时费此字段为NULL',
  `begin_date`          DATE          NULL                   COMMENT '计费周期开始日',
  `end_date`            DATE          NULL                   COMMENT '计费周期结束日',
  `num`                 DECIMAL(12,4) NOT NULL DEFAULT 1     COMMENT '计费数量（面积/用量/次数），FIXED类型时为1',
  `price`               DECIMAL(12,4) NOT NULL               COMMENT '单价（元），生成时快照',
  `total`               DECIMAL(12,2) NOT NULL               COMMENT '费用金额 = num × price（或公式计算结果）',
  `overdue_fee`         DECIMAL(12,2) NOT NULL DEFAULT 0     COMMENT '当前累计滞纳金（动态更新）',
  `discount_id`         VARCHAR(50)   NULL                   COMMENT '使用的折扣ID，关联 resi_discount.id',
  `discount_amount`     DECIMAL(12,2) NOT NULL DEFAULT 0     COMMENT '折扣减免金额',
  `receivable`          DECIMAL(12,2) NOT NULL               COMMENT '应收合计 = total + overdue_fee - discount_amount',
  `pay_state`           CHAR(1)       NOT NULL DEFAULT '0'   COMMENT '缴费状态：0未收 1部分收 2已收 3减免',
  `paid_amount`         DECIMAL(12,2) NOT NULL DEFAULT 0     COMMENT '已实收金额',
  `pay_log_id`          VARCHAR(50)   NULL                   COMMENT '最后一次收款流水ID',
  `pay_time`            DATETIME      NULL                   COMMENT '最后收款时间',
  `gen_batch`           VARCHAR(50)   NULL                   COMMENT '批量生成批次号，格式 GEN-{projectId}-{period}，临时费为NULL',
  `meter_reading_id`    VARCHAR(50)   NULL                   COMMENT '来源抄表记录ID，仅仪表类费用有值，关联 resi_meter_reading.id',
  `remark`              VARCHAR(500)  NULL                   COMMENT '备注',
  `enabled_mark`        INT           NOT NULL DEFAULT 1     COMMENT '有效标志（此表用INT兼容老代码）',
  `creator_time`        DATETIME      NOT NULL               COMMENT '创建时间',
  `creator_user_id`     VARCHAR(50)   NOT NULL               COMMENT '创建人用户ID',
  `last_modify_time`    DATETIME      NULL                   COMMENT '最后修改时间',
  `last_modify_user_id` VARCHAR(50)   NULL                   COMMENT '最后修改人用户ID',
  `delete_time`         DATETIME      NULL                   COMMENT '软删除时间，NULL表示未删除（只允许删除pay_state=0的记录）',
  `delete_user_id`      VARCHAR(50)   NULL                   COMMENT '执行软删除的用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_project_period`  (`project_id`, `bill_period`)              COMMENT '按项目和账单月查询（报表高频）',
  KEY `idx_resource_state`  (`resource_type`, `resource_id`, `pay_state`) COMMENT '收银台按资源查待缴费用',
  KEY `idx_customer`        (`customer_id`)                            COMMENT '按客户查账单（C端缴费）',
  KEY `idx_gen_batch`       (`gen_batch`)                              COMMENT '按批次操作（批量删除重生成）',
  KEY `idx_pay_state_period` (`project_id`, `pay_state`, `bill_period`) COMMENT '欠费报表'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='应收账单（住宅收费核心表）';


-- ============================================================
-- 4. 收款财务层（6张表）
-- 主键：VARCHAR(50) UUID（流水类，不可删除）
-- ============================================================

-- ------------------------------------------------------------
-- 16. resi_pay_log  收款流水
-- ------------------------------------------------------------
CREATE TABLE `resi_pay_log` (
  `id`              VARCHAR(50)   NOT NULL               COMMENT '流水ID，UUID',
  `project_id`      BIGINT        NOT NULL               COMMENT '所属项目ID',
  `pay_no`          VARCHAR(50)   NOT NULL               COMMENT '收据号/流水号，来自 base_billrule，全局唯一',
  `resource_type`   VARCHAR(20)   NOT NULL               COMMENT '资源类型：ROOM/PARKING/STORAGE',
  `resource_id`     BIGINT        NOT NULL               COMMENT '资源ID',
  `resource_name`   VARCHAR(100)  NULL                   COMMENT '资源名称（冗余）',
  `customer_name`   VARCHAR(100)  NULL                   COMMENT '客户姓名（冗余）',
  `pay_type`        VARCHAR(20)   NOT NULL               COMMENT '操作类型：COLLECT收款 REFUND退款 WRITEOFF冲红',
  `pay_method`      VARCHAR(50)   NOT NULL               COMMENT '支付方式：CASH现金 WECHAT微信 TRANSFER转账 BANK银行 OTHER其他',
  `receivable_ids`  JSON          NOT NULL               COMMENT '本次操作关联的应收记录ID列表（JSON数组）',
  `total_amount`    DECIMAL(12,2) NOT NULL               COMMENT '本次应收合计（含滞纳金，未扣折扣）',
  `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT 0     COMMENT '折扣减免金额',
  `overdue_amount`  DECIMAL(12,2) NOT NULL DEFAULT 0     COMMENT '本次实收滞纳金',
  `pre_pay_amount`  DECIMAL(12,2) NOT NULL DEFAULT 0     COMMENT '本次冲抵的预收款金额',
  `pay_amount`      DECIMAL(12,2) NOT NULL               COMMENT '实收金额（业主实际支付，不含预收冲抵）',
  `change_amount`   DECIMAL(12,2) NOT NULL DEFAULT 0     COMMENT '找零金额（现金收款时）',
  `certificate`     VARCHAR(200)  NULL                   COMMENT '支付凭证号（转账/银行流水号）',
  `note`            VARCHAR(500)  NULL                   COMMENT '收据备注',
  `discount_id`     VARCHAR(50)   NULL                   COMMENT '使用的折扣ID',
  `parent_log_id`   VARCHAR(50)   NULL                   COMMENT '冲红操作时关联的原始收款流水ID',
  `is_verified`     TINYINT       NOT NULL DEFAULT 0     COMMENT '复核状态：0未复核 1已复核',
  `verified_by`     VARCHAR(64)   NULL                   COMMENT '复核人账号',
  `verified_time`   DATETIME      NULL                   COMMENT '复核时间',
  `client`          TINYINT       NOT NULL DEFAULT 1     COMMENT '操作来源：1B端手工 2C端微信自助',
  `creator_time`    DATETIME      NOT NULL               COMMENT '创建时间（收款时间）',
  `creator_user_id` VARCHAR(50)   NOT NULL               COMMENT '操作人用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`)                                   COMMENT '收据号全局唯一',
  KEY `idx_project_time`  (`project_id`, `creator_time`)              COMMENT '按项目和时间查流水（日结/对账）',
  KEY `idx_resource`      (`resource_type`, `resource_id`)            COMMENT '按资源查收款历史',
  KEY `idx_pay_type_time` (`project_id`, `pay_type`, `creator_time`)  COMMENT '按操作类型统计'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='收款流水（不可删除）';

-- ------------------------------------------------------------
-- 17. resi_pre_account  预收款余额账户
-- ------------------------------------------------------------
CREATE TABLE `resi_pre_account` (
  `id`            VARCHAR(50)   NOT NULL               COMMENT '预收款账户ID，UUID',
  `project_id`    BIGINT        NOT NULL               COMMENT '所属项目ID',
  `resource_type` VARCHAR(20)   NOT NULL               COMMENT '资源类型：ROOM/PARKING/STORAGE',
  `resource_id`   BIGINT        NOT NULL               COMMENT '资源ID',
  `fee_id`        VARCHAR(50)   NULL                   COMMENT '专款费用ID，NULL表示通用预收款（可冲任意费用）',
  `balance`       DECIMAL(12,2) NOT NULL DEFAULT 0     COMMENT '当前余额（元），不得为负数',
  `create_time`   DATETIME      NOT NULL               COMMENT '创建时间',
  `update_time`   DATETIME      NOT NULL               COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pre_account` (`resource_type`, `resource_id`, `fee_id`) COMMENT '每个资源每类专款只有一个账户',
  KEY `idx_project` (`project_id`)                                         COMMENT '按项目汇总预收款'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='预收款余额账户';

-- ------------------------------------------------------------
-- 18. resi_pre_pay  预收款明细流水
-- ------------------------------------------------------------
CREATE TABLE `resi_pre_pay` (
  `id`              VARCHAR(50)   NOT NULL               COMMENT '流水ID，UUID',
  `project_id`      BIGINT        NOT NULL               COMMENT '所属项目ID',
  `account_id`      VARCHAR(50)   NOT NULL               COMMENT '预收款账户ID，关联 resi_pre_account.id',
  `resource_name`   VARCHAR(100)  NULL                   COMMENT '资源名称（冗余）',
  `op_type`         VARCHAR(20)   NOT NULL               COMMENT '操作类型：IN收入（存入）OUT支出（冲抵）REFUND退还',
  `amount`          DECIMAL(12,2) NOT NULL               COMMENT '本次操作金额（绝对值，正数）',
  `balance_after`   DECIMAL(12,2) NULL                   COMMENT '操作后账户余额（快照）',
  `ref_log_id`      VARCHAR(50)   NULL                   COMMENT '关联收款流水ID，关联 resi_pay_log.id',
  `remark`          VARCHAR(200)  NULL                   COMMENT '操作备注',
  `creator_time`    DATETIME      NOT NULL               COMMENT '创建时间',
  `creator_user_id` VARCHAR(50)   NOT NULL               COMMENT '操作人用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_account` (`account_id`)  COMMENT '按账户查流水'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='预收款明细流水（不可删除）';

-- ------------------------------------------------------------
-- 19. resi_deposit  押金台账
-- ------------------------------------------------------------
CREATE TABLE `resi_deposit` (
  `id`                  VARCHAR(50)   NOT NULL               COMMENT '押金记录ID，UUID',
  `project_id`          BIGINT        NOT NULL               COMMENT '所属项目ID',
  `resource_type`       VARCHAR(20)   NOT NULL               COMMENT '资源类型：ROOM/PARKING/STORAGE',
  `resource_id`         BIGINT        NOT NULL               COMMENT '资源ID',
  `resource_name`       VARCHAR(100)  NULL                   COMMENT '资源名称（冗余）',
  `fee_id`              VARCHAR(50)   NOT NULL               COMMENT '押金费用定义ID',
  `fee_name`            VARCHAR(100)  NULL                   COMMENT '费用名称（冗余）',
  `customer_name`       VARCHAR(100)  NULL                   COMMENT '缴纳人姓名（冗余）',
  `amount`              DECIMAL(12,2) NOT NULL               COMMENT '押金金额（元）',
  `pay_method`          VARCHAR(50)   NULL                   COMMENT '收款方式：CASH/WECHAT/TRANSFER',
  `pay_no`              VARCHAR(50)   NULL                   COMMENT '收款单号',
  `state`               VARCHAR(20)   NOT NULL DEFAULT 'COLLECTED' COMMENT '押金状态：COLLECTED已收 REFUNDED已退',
  `refund_amount`       DECIMAL(12,2) NOT NULL DEFAULT 0     COMMENT '已退还金额',
  `refund_method`       VARCHAR(50)   NULL                   COMMENT '退款方式',
  `refund_no`           VARCHAR(50)   NULL                   COMMENT '退款单号',
  `refund_time`         DATE          NULL                   COMMENT '退款日期',
  `remark`              VARCHAR(200)  NULL                   COMMENT '备注',
  `creator_time`        DATETIME      NOT NULL               COMMENT '收取时间',
  `creator_user_id`     VARCHAR(50)   NOT NULL               COMMENT '经办人用户ID',
  `last_modify_time`    DATETIME      NULL                   COMMENT '最后修改时间',
  `last_modify_user_id` VARCHAR(50)   NULL                   COMMENT '最后修改人用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_project_state` (`project_id`, `state`)      COMMENT '查询待退还押金',
  KEY `idx_resource`      (`resource_type`, `resource_id`) COMMENT '按资源查押金'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='押金台账';

-- ------------------------------------------------------------
-- 20. resi_invoice_record  发票记录
-- ------------------------------------------------------------
CREATE TABLE `resi_invoice_record` (
  `id`              VARCHAR(50)   NOT NULL               COMMENT '发票记录ID，UUID',
  `project_id`      BIGINT        NOT NULL               COMMENT '所属项目ID',
  `pay_log_id`      VARCHAR(50)   NOT NULL               COMMENT '关联收款流水ID',
  `pay_no`          VARCHAR(50)   NOT NULL               COMMENT '收据号（冗余，便于展示）',
  `invoice_no`      VARCHAR(100)  NULL                   COMMENT '发票号码',
  `invoice_type`    VARCHAR(20)   NULL                   COMMENT '发票类型：VAT_NORMAL普通增值税 VAT_SPECIAL专用增值税 E_INVOICE电子发票',
  `invoice_title`   VARCHAR(200)  NULL                   COMMENT '开票抬头',
  `tax_no`          VARCHAR(50)   NULL                   COMMENT '税号',
  `amount`          DECIMAL(12,2) NULL                   COMMENT '发票金额（元）',
  `invoice_time`    DATE          NULL                   COMMENT '开票日期',
  `remark`          VARCHAR(200)  NULL                   COMMENT '备注',
  `creator_time`    DATETIME      NOT NULL               COMMENT '记录创建时间',
  `creator_user_id` VARCHAR(50)   NOT NULL               COMMENT '开票操作人用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_pay_log`      (`pay_log_id`)                     COMMENT '通过收款流水查发票',
  KEY `idx_project_time` (`project_id`, `creator_time`)     COMMENT '发票统计报表'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='发票记录';

-- ------------------------------------------------------------
-- 21. resi_adjust_log  调账记录
-- ------------------------------------------------------------
CREATE TABLE `resi_adjust_log` (
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


-- ============================================================
-- 5. C端服务层（7张表）
-- ============================================================

-- ------------------------------------------------------------
-- 22. resi_wx_user  微信用户
-- ------------------------------------------------------------
CREATE TABLE `resi_wx_user` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '微信用户ID，自增主键',
  `openid`          VARCHAR(128) NOT NULL                 COMMENT '微信 openid，小程序唯一标识',
  `unionid`         VARCHAR(128) NULL                     COMMENT '微信 unionid，同一主体下跨应用唯一',
  `nickname`        VARCHAR(100) NULL                     COMMENT '微信昵称',
  `avatar_url`      VARCHAR(500) NULL                     COMMENT '微信头像URL',
  `customer_id`     BIGINT       NULL                     COMMENT '绑定的业主客户ID，关联 resi_customer.id，NULL表示未绑定',
  `project_id`      BIGINT       NULL                     COMMENT '绑定的项目ID（冗余，便于推送查询）',
  `bind_time`       DATETIME     NULL                     COMMENT '绑定业主的时间',
  `last_login_time` DATETIME     NULL                     COMMENT '最后登录时间',
  `create_time`     DATETIME     NOT NULL                 COMMENT '首次登录（注册）时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)                       COMMENT 'openid 全局唯一',
  KEY `idx_customer_id` (`customer_id`)                   COMMENT '通过业主查微信用户'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='C端微信用户（小程序/公众号）';

-- ------------------------------------------------------------
-- 23. resi_notice  通知公告
-- ------------------------------------------------------------
CREATE TABLE `resi_notice` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '公告ID，自增主键',
  `project_id`   BIGINT       NOT NULL                 COMMENT '所属项目ID',
  `title`        VARCHAR(200) NOT NULL                 COMMENT '公告标题',
  `content`      LONGTEXT     NULL                     COMMENT '公告正文（富文本HTML）',
  `cover_image`  VARCHAR(500) NULL                     COMMENT '封面图片路径',
  `notice_type`  TINYINT      NOT NULL DEFAULT 1       COMMENT '公告类型：1普通通知 2紧急通知 3活动公告',
  `target_type`  TINYINT      NOT NULL DEFAULT 1       COMMENT '推送范围：1全部业主 2指定楼栋 3指定房间',
  `target_ids`   JSON         NULL                     COMMENT '目标ID列表（JSON数组），target_type=1时为NULL',
  `publish_time` DATETIME     NULL                     COMMENT '发布时间，NULL表示草稿或定时发布',
  `view_count`   INT          NOT NULL DEFAULT 0       COMMENT '总浏览次数',
  `status`       TINYINT      NOT NULL DEFAULT 0       COMMENT '状态：0草稿 1已发布 2已撤回',
  `create_by`    VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '创建者账号',
  `create_time`  DATETIME     NOT NULL                 COMMENT '创建时间',
  `update_by`    VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '最后更新者账号',
  `update_time`  DATETIME     NOT NULL                 COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_status` (`project_id`, `status`)         COMMENT 'C端按项目查已发布公告',
  KEY `idx_publish_time`   (`project_id`, `publish_time`)   COMMENT '按发布时间排序'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='C端通知公告';

-- ------------------------------------------------------------
-- 24. resi_butler  管家信息
-- ------------------------------------------------------------
CREATE TABLE `resi_butler` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '管家ID，自增主键',
  `project_id`   BIGINT       NOT NULL                 COMMENT '所属项目ID',
  `butler_name`  VARCHAR(50)  NOT NULL                 COMMENT '管家姓名',
  `phone`        VARCHAR(20)  NOT NULL                 COMMENT '联系电话（业主一键拨打）',
  `avatar_url`   VARCHAR(500) NULL                     COMMENT '管家头像图片路径',
  `grid_area`    VARCHAR(200) NULL                     COMMENT '负责区域描述，如 1号楼-3号楼',
  `introduction` VARCHAR(500) NULL                     COMMENT '管家简介',
  `sort_code`    INT          NOT NULL DEFAULT 0       COMMENT '排序码，数字越小越靠前',
  `enabled_mark` TINYINT      NOT NULL DEFAULT 1       COMMENT '有效标志：1有效 0无效',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)  COMMENT '按项目查管家'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='管家信息';

-- ------------------------------------------------------------
-- 25. resi_butler_room  管家负责房间
-- ------------------------------------------------------------
CREATE TABLE `resi_butler_room` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT  COMMENT '关联记录ID',
  `butler_id` BIGINT NOT NULL                 COMMENT '管家ID，关联 resi_butler.id',
  `room_id`   BIGINT NOT NULL                 COMMENT '房间ID，关联 resi_room.id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_butler_room` (`butler_id`, `room_id`)  COMMENT '防止重复关联',
  KEY `idx_room_id` (`room_id`)                          COMMENT '通过房间快速查管家'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='管家负责房间关联';

-- ------------------------------------------------------------
-- 26. resi_butler_review  管家评价
-- ------------------------------------------------------------
CREATE TABLE `resi_butler_review` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '评价记录ID，自增主键',
  `butler_id`    BIGINT       NOT NULL                 COMMENT '被评价管家ID',
  `customer_id`  BIGINT       NOT NULL                 COMMENT '评价业主客户ID',
  `score`        TINYINT      NOT NULL                 COMMENT '评分：1-5分',
  `content`      VARCHAR(500) NULL                     COMMENT '评价内容',
  `review_month` VARCHAR(7)   NOT NULL                 COMMENT '评价月份，格式 yyyy-MM，每月每管家每业主限评一次',
  `create_time`  DATETIME     NOT NULL                 COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review` (`butler_id`, `customer_id`, `review_month`)  COMMENT '每月限评一次',
  KEY `idx_butler_id` (`butler_id`)                                       COMMENT '查管家评价列表'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='管家评价（每月限评一次）';

-- ------------------------------------------------------------
-- 27. resi_convenience  便民信息
-- ------------------------------------------------------------
CREATE TABLE `resi_convenience` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '便民信息ID，自增主键',
  `project_id`   BIGINT       NOT NULL                 COMMENT '所属项目ID',
  `category`     VARCHAR(50)  NULL                     COMMENT '分类，如 交通、生活、快递、维修',
  `item_name`    VARCHAR(100) NOT NULL                 COMMENT '服务名称，如 火车站、顺丰快递',
  `phone`        VARCHAR(20)  NULL                     COMMENT '联系电话',
  `address`      VARCHAR(255) NULL                     COMMENT '地址',
  `sort_code`    INT          NOT NULL DEFAULT 0       COMMENT '排序码',
  `enabled_mark` TINYINT      NOT NULL DEFAULT 1       COMMENT '有效标志：1有效 0无效',
  PRIMARY KEY (`id`),
  KEY `idx_project_category` (`project_id`, `category`)  COMMENT '按项目和分类查便民信息'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='便民信息';

-- ------------------------------------------------------------
-- 28. resi_push_record  消息推送记录
-- ------------------------------------------------------------
CREATE TABLE `resi_push_record` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '推送记录ID，自增主键',
  `project_id`  BIGINT       NULL                     COMMENT '所属项目ID',
  `customer_id` BIGINT       NULL                     COMMENT '接收客户ID',
  `push_scene`  VARCHAR(50)  NULL                     COMMENT '推送场景：ARREARS欠费通知 PAY_SUCCESS缴费成功 NOTICE公告 DAILY_REPORT日报',
  `push_type`   TINYINT      NOT NULL                 COMMENT '推送方式：1微信模板消息 2短信 3站内消息',
  `openid`      VARCHAR(128) NULL                     COMMENT '接收方微信openid',
  `content`     JSON         NULL                     COMMENT '推送内容（JSON，根据场景不同结构不同）',
  `status`      TINYINT      NOT NULL DEFAULT 0       COMMENT '发送状态：0待发送 1成功 2失败',
  `retry_count` INT          NOT NULL DEFAULT 0       COMMENT '已重试次数，达到3次不再重试',
  `send_time`   DATETIME     NULL                     COMMENT '实际发送时间',
  `fail_reason` VARCHAR(500) NULL                     COMMENT '失败原因（微信接口错误信息）',
  `create_time` DATETIME     NOT NULL                 COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_retry` (`status`, `retry_count`)  COMMENT '定时任务扫描待发/重试记录',
  KEY `idx_customer`     (`customer_id`)             COMMENT '按客户查推送历史'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='消息推送记录';
