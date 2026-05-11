# DATABASE DESIGN
## 肇新智慧物业 · 住宅物业收费模块（resi）

**文档版本**：V1.0  
**编制日期**：2026-05-10  
**数据库**：MySQL 8.0  
**字符集**：utf8mb4 / utf8mb4_unicode_ci  
**存储引擎**：InnoDB  
**适用模块**：`zhaoxinwy-pms`（包 `com.zhaoxinms.resi`）+ `zhaoxinwy-wxmp`

> **隔离原则**：本文档涵盖的所有表均以 `resi_` 为前缀，与现有系统的 `payment_`、`config_`、`sys_`、`owner_`、`park_`、`ACT_`、`qrtz_` 前缀表完全隔离，不存在外键约束跨越该边界。

---

## 目录

1. [数据库全局规范](#1-数据库全局规范)
2. [表清单总览](#2-表清单总览)
3. [ERD 实体关系图](#3-erd-实体关系图)
   - 3.1 [档案层 ERD](#31-档案层-erd)
   - 3.2 [收费配置层 ERD](#32-收费配置层-erd)
   - 3.3 [计费与收款层 ERD](#33-计费与收款层-erd)
   - 3.4 [C端服务层 ERD](#34-c端服务层-erd)
   - 3.5 [跨层数据流向图](#35-跨层数据流向图)
4. [分层建表 DDL](#4-分层建表-ddl)
   - 4.1 [档案层（9张表）](#41-档案层9张表)
   - 4.2 [收费配置层（4张表）](#42-收费配置层4张表)
   - 4.3 [计费层（2张表）](#43-计费层2张表)
   - 4.4 [收款财务层（6张表）](#44-收款财务层6张表)
   - 4.5 [C端服务层（7张表）](#45-c端服务层7张表)
5. [枚举值域字典](#5-枚举值域字典)
6. [索引策略](#6-索引策略)
7. [分区策略](#7-分区策略)
8. [字段注释规范](#8-字段注释规范)
9. [与现有表的复用关系](#9-与现有表的复用关系)
10. [初始化脚本](#10-初始化脚本)

---

## 1. 数据库全局规范

### 1.1 命名约定

| 对象 | 规则 | 示例 |
|---|---|---|
| 表名 | `resi_` 前缀 + 小写蛇形 | `resi_receivable` |
| 字段名 | 小写蛇形 | `bill_period`, `pay_state` |
| 主键 | 固定命名 `id` | — |
| 外键字段 | `{关联表单数}_id` | `project_id`, `room_id` |
| 唯一键 | `uk_{字段组合}` | `uk_pay_no`, `uk_meter_period` |
| 普通索引 | `idx_{字段组合}` | `idx_project_period` |
| 联合索引 | 高选择性字段在前 | `idx_resource_state(resource_type,resource_id,pay_state)` |

### 1.2 主键策略

```
档案类表（project / building / room / customer 等）
  → BIGINT NOT NULL AUTO_INCREMENT
  → 原因：ID 连续、B-Tree 友好、便于树形结构遍历

流水类表（receivable / pay_log / meter_reading / pre_pay 等）
  → VARCHAR(50) NOT NULL，业务层赋值 UUID（MyBatis-Plus IdType.ASSIGN_UUID）
  → 原因：与现有 payment_pay_log.id 风格一致，便于分布式扩展，防止 ID 暴露
```

### 1.3 公共字段规范

**档案类表**（可编辑、有生命周期）：

```sql
`enabled_mark`  TINYINT      NOT NULL DEFAULT 1   COMMENT '有效标志：1有效 0无效（软删除）',
`create_by`     VARCHAR(64)  NOT NULL DEFAULT ''   COMMENT '创建者账号',
`create_time`   DATETIME     NOT NULL               COMMENT '创建时间',
`update_by`     VARCHAR(64)  NOT NULL DEFAULT ''   COMMENT '最后更新者账号',
`update_time`   DATETIME     NOT NULL               COMMENT '最后更新时间'
```

**流水类表**（只写不改、保留完整历史）：

```sql
`creator_time`         DATETIME    NOT NULL COMMENT '创建时间',
`creator_user_id`      VARCHAR(50) NOT NULL COMMENT '创建人用户ID',
`last_modify_time`     DATETIME             COMMENT '最后修改时间',
`last_modify_user_id`  VARCHAR(50)          COMMENT '最后修改人用户ID'
```

> 两种风格不得混用于同一张表，与现有系统 `payment_pay_log` / `config_fee_item` 风格对齐。

### 1.4 数据类型约束

| 场景 | 类型 | 说明 |
|---|---|---|
| 金额（元） | `DECIMAL(12,2)` | 最大支持 9,999,999,999.99 元 |
| 单价 / 用量 | `DECIMAL(12,4)` | 保留4位小数，支持精细计量 |
| 利率 / 折扣率 | `DECIMAL(8,6)` | 如 0.000500 = 日千分之零点五 |
| 面积（㎡） | `DECIMAL(10,2)` | 如 128.50 ㎡ |
| 状态枚举（有限离散值） | `TINYINT` | 值域 ≤ 10，如 0/1/2 |
| 字符串枚举（值域较多） | `VARCHAR(20)` | 如 `PERIOD/TEMP/DEPOSIT` |
| 长文本 / 公式 | `TEXT` | 计费公式、备注 |
| 结构化数组 | `JSON` | 如 `receivable_ids`、`target_ids` |
| 账单月 | `VARCHAR(7)` | 固定格式 `2026-05` |
| 日期（无时分） | `DATE` | 生效日期、截止日期 |
| 精确时间戳 | `DATETIME` | 收款时间、创建时间 |

**禁止**：使用 `FLOAT`、`DOUBLE` 存储金额或业务计算值（IEEE754 精度丢失）。

---

## 2. 表清单总览

共 **28 张**新增表，全部以 `resi_` 为前缀。

| # | 表名 | 层次 | 主键类型 | 说明 |
|---|---|---|---|---|
| 1 | `resi_project` | 档案层 | BIGINT AI | 项目（小区） |
| 2 | `resi_building` | 档案层 | BIGINT AI | 楼栋 |
| 3 | `resi_room` | 档案层 | BIGINT AI | 房间（核心档案） |
| 4 | `resi_customer` | 档案层 | BIGINT AI | 客户（业主/租户） |
| 5 | `resi_customer_asset` | 档案层 | BIGINT AI | 客户资产绑定 |
| 6 | `resi_meter_device` | 档案层 | BIGINT AI | 仪表档案 |
| 7 | `resi_parking_area` | 档案层 | BIGINT AI | 车场区域 |
| 8 | `resi_parking_space` | 档案层 | BIGINT AI | 车位 |
| 9 | `resi_room_transfer` | 档案层 | BIGINT AI | 房屋过户记录 |
| 10 | `resi_fee_definition` | 配置层 | VARCHAR(50) | 费用定义 |
| 11 | `resi_fee_allocation` | 配置层 | VARCHAR(50) | 费用分配 |
| 12 | `resi_ticket_config` | 配置层 | VARCHAR(50) | 票据配置 |
| 13 | `resi_discount` | 配置层 | VARCHAR(50) | 折扣配置 |
| 14 | `resi_meter_reading` | 计费层 | VARCHAR(50) | 抄表记录 |
| 15 | `resi_receivable` | 计费层 | VARCHAR(50) | 应收账单（核心） |
| 16 | `resi_pay_log` | 收款层 | VARCHAR(50) | 收款流水 |
| 17 | `resi_pre_account` | 收款层 | VARCHAR(50) | 预收款余额账户 |
| 18 | `resi_pre_pay` | 收款层 | VARCHAR(50) | 预收款明细流水 |
| 19 | `resi_deposit` | 收款层 | VARCHAR(50) | 押金台账 |
| 20 | `resi_invoice_record` | 收款层 | VARCHAR(50) | 发票记录 |
| 21 | `resi_adjust_log` | 收款层 | VARCHAR(50) | 调账记录 |
| 22 | `resi_wx_user` | C端层 | BIGINT AI | 微信用户 |
| 23 | `resi_notice` | C端层 | BIGINT AI | 通知公告 |
| 24 | `resi_butler` | C端层 | BIGINT AI | 管家信息 |
| 25 | `resi_butler_room` | C端层 | BIGINT AI | 管家负责房间 |
| 26 | `resi_butler_review` | C端层 | BIGINT AI | 管家评价 |
| 27 | `resi_convenience` | C端层 | BIGINT AI | 便民信息 |
| 28 | `resi_push_record` | C端层 | BIGINT AI | 消息推送记录 |

---

## 3. ERD 实体关系图

### 3.1 档案层 ERD

```
┌─────────────────┐
│  resi_project   │  1 个项目（小区）
│─────────────────│
│ PK id           │──────────────────────────────────────────────┐
│    code (UQ)    │                                              │
│    name         │                                              │
│    address      │                                              │
│    contact_phone│                                              │
│    manager_name │                                              │
│    logo_url     │                                              │
│    seal_url     │                                              │
└────────┬────────┘                                              │
         │ 1                                                     │
         │                                                       │
         │ N                                                     │
┌────────▼────────┐                                             │
│  resi_building  │  楼栋属于项目                               │
│─────────────────│                                             │
│ PK id           │                                             │
│ FK project_id   │                                             │
│    name         │                                             │
│    number       │                                             │
│    floors       │                                             │
└────────┬────────┘                                             │
         │ 1                                                     │
         │                                                       │
         │ N                                                     │
┌────────▼────────┐        ┌──────────────────┐                │
│   resi_room     │  1     │ resi_meter_device│                │
│─────────────────│◄───────│──────────────────│                │
│ PK id           │  N     │ PK id            │                │
│ FK project_id   │        │ FK project_id    │                │
│ FK building_id  │        │ FK room_id (可NULL)               │
│    unit_no      │        │    meter_code(UQ)│                │
│    floor_no     │        │    meter_type    │                │
│    room_no      │        │    multiplier    │                │
│    room_alias   │        │    is_public     │                │
│    building_area│        │    public_group  │                │
│    inner_area   │        └──────────────────┘                │
│    room_type    │                                             │
│    state        │        ┌──────────────────────────┐        │
└────────┬────────┘   1    │  resi_customer_asset     │        │
         │            ◄────│──────────────────────────│        │
         │            N    │ PK id                    │        │
         │                 │ FK customer_id            │        │
         │                 │ FK project_id             │        │
         │                 │    asset_type (1/2/3)     │        │
         │                 │    asset_id               │        │
         │                 │    bind_date              │        │
         │                 │    is_current             │        │
         │                 └───────────┬──────────────┘        │
         │                             │ N                      │
         │                             │ 1                      │
         │                    ┌────────▼────────┐              │
         │                    │  resi_customer  │              │
         │                    │─────────────────│              │
         │                    │ PK id           │              │
         │                    │ FK project_id ──┼──────────────┘
         │                    │    customer_name│
         │                    │    phone        │
         │                    │    id_card(加密)│
         │                    │    customer_type│
         │                    │    openid       │
         │                    └─────────────────┘
         │
         │ 1
         │ N
┌────────▼────────────┐
│  resi_room_transfer │  过户历史
│─────────────────────│
│ PK id               │
│ FK project_id       │
│ FK room_id          │
│ FK old_customer_id  │
│ FK new_customer_id  │
│    transfer_date    │
└─────────────────────┘

┌──────────────────┐   1     ┌──────────────────┐
│ resi_parking_area│◄────────│ resi_parking_space│
│──────────────────│   N     │──────────────────│
│ PK id            │         │ PK id            │
│ FK project_id    │         │ FK project_id    │
│    area_code     │         │ FK area_id       │
│    area_name     │         │    space_code    │
│    total_count   │         │    space_type    │
└──────────────────┘         │    property_type │
                             │ FK owner_id      │ ──→ resi_customer.id
                             │    state         │
                             └──────────────────┘
```

### 3.2 收费配置层 ERD

```
┌──────────────────────┐
│  resi_fee_definition │  1个项目可定义N个费用
│──────────────────────│
│ PK id (VARCHAR)      │
│ FK project_id        │
│    fee_name          │
│    fee_code          │
│    fee_type          │  PERIOD/TEMP/DEPOSIT/PRE
│    calc_type         │  FIXED/AREA/USAGE/FORMULA
│    unit_price        │
│    formula (TEXT)    │
│    cycle_unit        │
│    cycle_value       │
│    overdue_enable    │
│    overdue_rate      │
│    earmark_enable    │  专款专冲标记
│    invoice_title     │
│    tax_rate          │
└──────────┬───────────┘
           │ 1
           │
           │ N
┌──────────▼───────────┐
│  resi_fee_allocation │  费用分配到具体资源
│──────────────────────│
│ PK id (VARCHAR)      │
│ FK fee_id            │
│ FK project_id        │
│    resource_type     │  ROOM/PARKING/STORAGE
│    resource_id       │ ──→ resi_room.id / resi_parking_space.id
│    resource_name     │  冗余
│    custom_price      │  个性化单价（可覆盖定义中的 unit_price）
│    custom_formula    │  个性化公式
│    start_date        │
│    end_date          │
│ UQ(fee_id,           │
│    resource_type,    │
│    resource_id,      │
│    start_date)       │
└──────────────────────┘

┌───────────────────┐      ┌──────────────────┐
│  resi_ticket_config│      │   resi_discount  │
│───────────────────│      │──────────────────│
│ PK id             │      │ PK id            │
│ FK project_id     │      │ FK project_id    │
│    ticket_type    │      │    discount_name │
│    title          │      │    discount_rate │
│    collect_org    │      │    fee_scope(JSON)│  适用费用ID列表
│    paper_size     │      │    valid_start   │
│    field_config   │      │    valid_end     │
│      (JSON)       │      └──────────────────┘
└───────────────────┘
```

### 3.3 计费与收款层 ERD

```
resi_meter_device                  resi_fee_allocation
       │                                  │
       │ 1 (每表每月一条抄表)              │ 1 (费用分配驱动生成)
       │ N                                │ N
       ▼                                  ▼
┌────────────────────────────────────────────┐
│              resi_meter_reading            │  抄表记录（仪表费）
│────────────────────────────────────────────│
│ PK id (VARCHAR)                            │
│ FK meter_id                                │
│ FK project_id, room_id, fee_id             │
│    period          '2026-05'               │
│    last_reading, curr_reading              │
│    raw_usage       (curr-last) × 倍率      │
│    loss_rate, loss_amount                  │
│    share_amount    公摊分摊量              │
│    billed_usage    计费用量                │
│    status          INPUT/BILLED/VERIFIED   │
│    import_batch                            │
│ FK receivable_id   入账后关联              │
│ UQ(meter_id, period)                       │
└──────────────────────┬─────────────────────┘
                       │ 触发生成
                       ▼
┌──────────────────────────────────────────────────┐
│                  resi_receivable                 │  应收账单（核心）
│──────────────────────────────────────────────────│
│ PK id (VARCHAR)                                  │
│ FK project_id                                    │
│    resource_type / resource_id / resource_name   │
│ FK customer_id / customer_name (冗余)             │
│ FK fee_id / fee_name (冗余)                       │
│    fee_type        PERIOD/TEMP/DEPOSIT/PRE       │
│    bill_period     '2026-05'                     │
│    begin_date / end_date                         │
│    num             数量（面积/用量/次数）          │
│    price           单价                          │
│    total           费用金额 = num × price         │
│    overdue_fee     滞纳金                        │
│ FK discount_id / discount_amount                 │
│    receivable      应收合计                      │
│    pay_state       0/1/2/3                       │
│    paid_amount     已收金额                      │
│ FK pay_log_id      最后收款流水                   │
│    gen_batch       批次号                        │
│ FK meter_reading_id 来源抄表ID                   │
│    delete_time     软删除                        │
└────────────────────┬─────────────────────────────┘
                     │ N 条应收 → 1次收款
                     ▼
┌──────────────────────────────────────────────────┐
│                  resi_pay_log                    │  收款流水
│──────────────────────────────────────────────────│
│ PK id (VARCHAR)                                  │
│ FK project_id                                    │
│    pay_no (UQ)     收据号                        │
│    resource_type / resource_id / resource_name   │
│    customer_name   冗余                          │
│    pay_type        COLLECT/REFUND/WRITEOFF        │
│    pay_method      现金/微信/转账等               │
│    receivable_ids  JSON 应收ID列表               │
│    total_amount    应收合计                      │
│    discount_amount 折扣减免                      │
│    overdue_amount  收取滞纳金                    │
│    pre_pay_amount  冲抵预收款                    │
│    pay_amount      实收金额                      │
│    change_amount   找零                          │
│ FK parent_log_id   冲红关联原始流水              │
│    is_verified     复核状态                      │
│    client          1=B端 2=C端                   │
└──────────┬───────────────────────────────────────┘
           │
    ┌──────┴────────────────────────────┐
    │                                   │
    ▼                                   ▼
┌───────────────────┐     ┌──────────────────────┐
│  resi_pre_account │     │    resi_deposit       │
│───────────────────│     │──────────────────────│
│ PK id             │     │ PK id                │
│ FK project_id     │     │ FK project_id        │
│    resource_type  │     │    resource_type      │
│    resource_id    │     │    resource_id        │
│ FK fee_id (可NULL)│     │ FK fee_id             │
│    balance  余额  │     │    amount             │
│ UQ(resource_type, │     │    state              │
│    resource_id,   │     │      COLLECTED/REFUNDED│
│    fee_id)        │     │    refund_amount      │
└────────┬──────────┘     └──────────────────────┘
         │ 1
         │ N
┌────────▼──────────┐
│   resi_pre_pay    │  预收款流水
│───────────────────│
│ PK id             │
│ FK account_id     │
│ FK project_id     │
│    op_type        │  IN/OUT/REFUND
│    amount         │
│    balance_after  │
│ FK ref_log_id     │ ──→ resi_pay_log.id
└───────────────────┘

┌──────────────────────┐     ┌──────────────────┐
│  resi_invoice_record │     │  resi_adjust_log │
│──────────────────────│     │──────────────────│
│ PK id                │     │ PK id            │
│ FK pay_log_id        │     │ FK receivable_id │
│ FK project_id        │     │ FK project_id    │
│    pay_no            │     │    adjust_type   │
│    invoice_no        │     │    before_value  │
│    invoice_type      │     │    after_value   │
│    invoice_title     │     │    reason        │
│    tax_no / amount   │     └──────────────────┘
└──────────────────────┘
```

### 3.4 C端服务层 ERD

```
┌───────────────┐    1     ┌────────────────────────┐
│  resi_wx_user │◄─────────│  微信小程序登录/绑定    │
│───────────────│    绑定   │────────────────────────│
│ PK id         │          │ openid (UQ)             │
│    openid(UQ) │          │ unionid                 │
│    nickname   │          │ FK customer_id ─────────┼──→ resi_customer.id
│    avatar_url │          │ FK project_id  ─────────┼──→ resi_project.id
│    bind_time  │          └────────────────────────┘
└───────────────┘

┌──────────────────────────┐
│       resi_notice        │  B端发布，C端展示
│──────────────────────────│
│ PK id                    │
│ FK project_id            │
│    title / content       │
│    notice_type           │  1普通 2紧急 3活动
│    target_type           │  1全部 2楼栋 3房间
│    target_ids (JSON)     │
│    publish_time / status │
│    view_count            │
└──────────────────────────┘

┌──────────────┐   1   ┌──────────────────┐
│  resi_butler │◄──────│ resi_butler_room │
│──────────────│   N   │──────────────────│
│ PK id        │       │ PK id            │
│ FK project_id│       │ FK butler_id     │
│  butler_name │       │ FK room_id ──────┼──→ resi_room.id
│  phone       │       └──────────────────┘
│  avatar_url  │
│  grid_area   │  1
│              │◄──────────────────────────────┐
└──────────────┘   N                           │
                        ┌──────────────────────┐
                        │  resi_butler_review  │
                        │──────────────────────│
                        │ PK id                │
                        │ FK butler_id         │
                        │ FK customer_id ──────┼──→ resi_customer.id
                        │    score             │
                        │    review_month      │
                        │ UQ(butler_id,        │
                        │    customer_id,      │
                        │    review_month)     │
                        └──────────────────────┘

┌────────────────────┐     ┌──────────────────────┐
│  resi_convenience  │     │   resi_push_record   │
│────────────────────│     │──────────────────────│
│ PK id              │     │ PK id                │
│ FK project_id      │     │ FK project_id        │
│    category        │     │ FK customer_id       │
│    item_name       │     │    push_scene        │
│    phone / address │     │    push_type         │
└────────────────────┘     │    openid            │
                           │    content (JSON)    │
                           │    status / retry_count│
                           └──────────────────────┘
```

### 3.5 跨层数据流向图

```
                    ┌─────────────────┐
                    │【基础档案层】    │
                    │ project         │
                    │  └── building   │
                    │       └── room  │
                    │  customer ◄─────┤ customer_asset
                    │  meter_device   │
                    │  parking_space  │
                    └────────┬────────┘
                             │档案数据驱动
                             ▼
                    ┌─────────────────┐
                    │【收费配置层】    │
                    │ fee_definition  │
                    │  └── fee_alloc  │ ← 分配到 room/parking
                    │ ticket_config   │
                    │ discount        │
                    └────────┬────────┘
                             │配置驱动计费
                    ┌────────▼────────┐
                    │                 │
          ┌─────────┤【计费层】       │
          │         │ meter_reading   │ ← 抄表导入/手录
          │         │  └── receivable │ ← 批量生成/临时创建
          │         │                 │
          │         └────────┬────────┘
          │                  │应收账单
          ▼                  ▼
  ┌───────────────┐ ┌────────────────────┐
  │【C端缴费】    │ │【B端收银台】       │
  │ wx_user 登录  │ │ 收款/退款/冲红     │
  │ 查询账单      │ │ 预收款冲抵         │
  │ 微信支付      │ │ 押金收退           │
  └───────┬───────┘ └────────┬───────────┘
          │                  │
          └────────┬─────────┘
                   ▼
          ┌─────────────────┐
          │【收款财务层】    │
          │ pay_log         │ ← 收款流水（统一入口）
          │ pre_account     │ ← 预收款余额账户
          │ pre_pay         │ ← 预收款明细流水
          │ deposit         │ ← 押金台账
          │ invoice_record  │ ← 发票记录
          │ adjust_log      │ ← 调账记录
          └────────┬────────┘
                   │
                   ▼
          ┌─────────────────┐
          │【报表查询层】    │
          │ 交易汇总/明细   │
          │ 收费率报表      │
          │ 欠费明细        │
          │ 日结/预收/押金  │
          │ 发票/调账等     │
          └─────────────────┘
```

---

## 4. 分层建表 DDL

> 执行顺序：档案层 → 配置层 → 计费层 → 收款层 → C端层。

### 4.1 档案层（9张表）

```sql
-- ============================================================
-- 档案层 DDL
-- 表前缀：resi_
-- 主键：BIGINT AUTO_INCREMENT
-- ============================================================

-- ------------------------------------------------------------
-- 1. resi_project  项目（小区）
-- 作用：住宅物业的顶级组织单元，对应一个小区
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
-- 作用：项目下的楼栋，一个项目含多个楼栋
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
-- 作用：住宅物业核心档案，每行对应一套房/车库/储藏室
--       room_alias 用于收银台快速搜索，应为人工录入的易识别名称
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
-- 作用：记录业主或租户基本信息，openid 在C端绑定后填入
--       id_card 字段存储 AES-256 加密后的密文
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
-- 作用：记录客户与房间/车位/储藏室的绑定关系
--       同一资产可先后属于不同客户（过户后 is_current=0）
--       asset_id 根据 asset_type 分别关联 resi_room.id 或 resi_parking_space.id
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
-- 作用：记录水/电/燃气/暖气表的设备信息
--       is_public=1 的为公摊总表，public_group 标识同一公摊组
--       room_id 为 NULL 时表示公摊总表未绑定到具体房间
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
-- 作用：车位的上级分区，如 A区/地下一层
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
-- 作用：记录单个车位的产权和使用状态
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
-- 作用：记录每次过户的历史，不可删除，保留审计链
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
```

### 4.2 收费配置层（4张表）

```sql
-- ============================================================
-- 收费配置层 DDL
-- 主键：VARCHAR(50) UUID
-- ============================================================

-- ------------------------------------------------------------
-- 10. resi_fee_definition  费用定义
-- 作用：定义小区内所有收费项目的规则，是收费体系的配置入口
--       formula 字段为 TEXT，支持梯度公式（if/elsif/else/return语法）
--       复用现有公式引擎（FormulaCalculator Bean），变量：单价、数量
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
-- 作用：将费用定义分配到具体资源（房间/车位/储藏室）
--       同一费用可对不同房间设置个性化单价（custom_price）
--       唯一键确保同一资源同一费用在同一生效日不重复分配
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
-- 作用：配置收款单/缴费通知单的打印样式和字段排版
--       field_config 为 JSON 数组，存储字段列表及显示顺序
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
-- 作用：定义可用于收银台的优惠折扣，支持限定适用费用类型
--       fee_scope 为 JSON 数组存储费用ID列表，NULL表示不限
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
```

### 4.3 计费层（2张表）

```sql
-- ============================================================
-- 计费层 DDL
-- 主键：VARCHAR(50) UUID
-- ============================================================

-- ------------------------------------------------------------
-- 14. resi_meter_reading  抄表记录
-- 作用：每次抄表的读数数据，唯一键防止同一仪表同月重复录入
--       公摊计算：同一 public_group 的总表用量 - 各分表用量之和 = 公摊量
--       各分户分摊量 = 公摊量 × (本户建筑面积 / 组内所有户面积之和)
--       status 流转：INPUT → BILLED（入账后）→ VERIFIED（复核后）
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
-- 作用：记录每笔应收费用，是收银台的数据来源
--       来源：批量生成周期费（gen_batch 标记同批）/ 抄表入账 / 手动录入临时费
--       pay_state 流转：0未收 → 1部分收 → 2已收 / 3减免
--       receivable 应收合计 = total + overdue_fee - discount_amount
--       软删除：delete_time 不为 NULL 表示已删除（仅未收状态可删除）
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
  `meter_reading_id`    VARCHAR(50)   NULL                   COMMENT '来源抄表记录ID，仅仪表类费用有值',
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
```

### 4.4 收款财务层（6张表）

```sql
-- ============================================================
-- 收款财务层 DDL
-- 主键：VARCHAR(50) UUID（流水类，不可删除）
-- ============================================================

-- ------------------------------------------------------------
-- 16. resi_pay_log  收款流水
-- 作用：记录每一笔收款/退款/冲红操作，不可删除，永久保留
--       receivable_ids 为 JSON 数组，记录本次收款涉及的所有应收ID
--       WRITEOFF（冲红）时 parent_log_id 关联被冲红的原始流水
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
-- 作用：维护每个资源的预收款余额快照
--       earmark_enable=1 的费用对应的预收款有专款专冲约束：
--         fee_id=NULL → 通用预收款，可冲抵任何费用
--         fee_id=具体值 → 专款，只能冲该费用的应收
--       balance 由 resi_pre_pay 流水加减维护，需保持一致性
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
-- 作用：记录预收款账户的每次增减明细，不可删除
--       op_type=IN：收取预收款（balance+）
--       op_type=OUT：冲抵应收（balance-）
--       op_type=REFUND：退还预收款（balance-）
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
-- 作用：记录装修押金、车位押金等的收取和退还
--       state=COLLECTED：已收取未退还
--       state=REFUNDED：已退还（全额，refund_amount=amount）
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
-- 作用：将收款单与发票号码对应，支持一张收款单开一张发票
--       换开发票时更新 invoice_no，原始收款信息不变
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
-- 作用：记录对应收账单的人工调整操作，全部保留，不可删除
--       before_value / after_value 存储调整前后的值（文本快照）
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
```

### 4.5 C端服务层（7张表）

```sql
-- ============================================================
-- C端服务层 DDL
-- 模块：zhaoxinwy-wxmp
-- ============================================================

-- ------------------------------------------------------------
-- 22. resi_wx_user  微信用户
-- 作用：存储微信小程序/公众号用户，通过 customer_id 与业主绑定
--       一个微信用户只能绑定一个业主（一对一），可解绑重绑
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
-- 作用：B端发布，C端展示；支持全部推送或定向推送（楼栋/房间）
--       target_ids 为 JSON 数组，存储楼栋ID或房间ID列表
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
-- 作用：管家与房间的多对多关联，C端通过房间查找专属管家
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
-- 作用：业主对管家的月度评价，每月每管家每业主只能评一次
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
-- 作用：物业维护的周边服务商电话，按分类展示
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
-- 作用：记录每条推送消息的发送状态，支持失败重试（最多3次）
--       push_scene 对应业务场景，用于前端区分消息类型
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
```

---

## 5. 枚举值域字典

> 所有 VARCHAR 和 TINYINT 枚举字段的合法值域，代码中使用常量类 `ResiXxxConstants`，禁止硬编码字符串。

### 5.1 费用相关

| 字段 | 所在表 | 合法值 | 说明 |
|---|---|---|---|
| `fee_type` | `resi_fee_definition`, `resi_receivable` | `PERIOD` | 周期费（物业费、停车费等） |
| | | `TEMP` | 临时费（维修费、罚款等） |
| | | `DEPOSIT` | 押金 |
| | | `PRE` | 预收款 |
| `calc_type` | `resi_fee_definition` | `FIXED` | 固定金额 |
| | | `AREA` | 按建筑面积计费（单价×面积） |
| | | `USAGE` | 按用量计费（单价×用量） |
| | | `FORMULA` | 自定义公式（梯度计费） |
| `cycle_unit` | `resi_fee_definition` | `MONTH` | 按月 |
| | | `QUARTER` | 按季度 |
| | | `YEAR` | 按年 |
| `overdue_type` | `resi_fee_definition` | `DAY` | 按天计滞纳金 |
| | | `MONTH` | 按月计滞纳金 |
| `round_type` | `resi_fee_definition` | `ROUND` | 四舍五入 |
| | | `CEIL` | 向上取整 |
| | | `FLOOR` | 截尾（向下取整） |

### 5.2 账单与状态

| 字段 | 所在表 | 合法值 | 说明 |
|---|---|---|---|
| `pay_state` | `resi_receivable` | `'0'` | 未收款 |
| | | `'1'` | 部分收款（分期支付） |
| | | `'2'` | 已全额收清 |
| | | `'3'` | 已减免（无需缴纳） |
| `status` | `resi_meter_reading` | `INPUT` | 已录入，未入账 |
| | | `BILLED` | 已入账，生成应收 |
| | | `VERIFIED` | 已复核 |
| `pay_type` | `resi_pay_log` | `COLLECT` | 收款 |
| | | `REFUND` | 退款 |
| | | `WRITEOFF` | 冲红 |
| `pay_method` | `resi_pay_log`, `resi_deposit` | `CASH` | 现金 |
| | | `WECHAT` | 微信支付 |
| | | `TRANSFER` | 银行转账 |
| | | `BANK` | 银行代收 |
| | | `OTHER` | 其他 |
| `op_type` | `resi_pre_pay` | `IN` | 存入预收款 |
| | | `OUT` | 冲抵应收 |
| | | `REFUND` | 退还预收款 |
| `state` | `resi_deposit` | `COLLECTED` | 已收取，待退还 |
| | | `REFUNDED` | 已全额退还 |

### 5.3 档案状态

| 字段 | 所在表 | 合法值 | 说明 |
|---|---|---|---|
| `state` | `resi_room` | `NORMAL` | 正常入住 |
| | | `VACANT` | 空置 |
| | | `DECORATING` | 装修中 |
| | | `TRANSFERRED` | 已过户（过渡状态） |
| `room_type` | `resi_room` | `1` | 住宅 |
| | | `2` | 商铺 |
| | | `3` | 车库 |
| | | `4` | 储藏室 |
| `meter_type` | `resi_meter_device` | `1` | 水表 |
| | | `2` | 电表 |
| | | `3` | 燃气表 |
| | | `4` | 暖气表 |
| `customer_type` | `resi_customer` | `1` | 业主 |
| | | `2` | 租户 |
| | | `3` | 临时 |
| `resource_type` | 多表 | `ROOM` | 房间 |
| | | `PARKING` | 车位 |
| | | `STORAGE` | 储藏室 |
| `asset_type` | `resi_customer_asset` | `1` | 房间 |
| | | `2` | 车位 |
| | | `3` | 储藏室 |
| `state` | `resi_parking_space` | `IDLE` | 空闲 |
| | | `OCCUPIED` | 已占用（租用） |
| | | `SOLD` | 已出售（产权车位） |

### 5.4 C端相关

| 字段 | 所在表 | 合法值 | 说明 |
|---|---|---|---|
| `notice_type` | `resi_notice` | `1` | 普通通知 |
| | | `2` | 紧急通知 |
| | | `3` | 活动公告 |
| `target_type` | `resi_notice` | `1` | 推送给全部业主 |
| | | `2` | 推送给指定楼栋 |
| | | `3` | 推送给指定房间 |
| `status` | `resi_notice` | `0` | 草稿 |
| | | `1` | 已发布 |
| | | `2` | 已撤回 |
| `push_scene` | `resi_push_record` | `ARREARS` | 欠费通知 |
| | | `PAY_SUCCESS` | 缴费成功确认 |
| | | `NOTICE` | 公告推送 |
| | | `DAILY_REPORT` | 管理层日报 |
| `push_type` | `resi_push_record` | `1` | 微信模板消息 |
| | | `2` | 短信 |
| | | `3` | 站内消息 |
| `invoice_type` | `resi_invoice_record` | `VAT_NORMAL` | 普通增值税发票 |
| | | `VAT_SPECIAL` | 增值税专用发票 |
| | | `E_INVOICE` | 电子发票 |
| `adjust_type` | `resi_adjust_log` | `AMOUNT` | 调整金额 |
| | | `PERIOD` | 调整账期 |
| | | `STATUS` | 调整状态 |
| | | `OVERDUE_WAIVE` | 减免滞纳金 |
| `client` | `resi_pay_log` | `1` | B端手工操作 |
| | | `2` | C端微信自助缴费 |

---

## 6. 索引策略

### 6.1 索引总览

| 表名 | 索引名 | 字段 | 类型 | 驱动场景 |
|---|---|---|---|---|
| `resi_room` | `uk_room` | `(project_id, building_id, unit_no, room_no)` | 唯一 | 防重复创建 |
| `resi_room` | `idx_project_building` | `(project_id, building_id)` | 普通 | 楼栋下房间列表 |
| `resi_room` | `idx_room_alias` | `(room_alias)` | 普通 | 收银台模糊搜索 |
| `resi_customer` | `idx_project_phone` | `(project_id, phone)` | 普通 | 手机号查客户 |
| `resi_customer` | `idx_openid` | `(openid)` | 普通 | C端登录绑定查询 |
| `resi_customer_asset` | `idx_customer` | `(customer_id)` | 普通 | 客户资产列表 |
| `resi_customer_asset` | `idx_asset` | `(asset_type, asset_id, is_current)` | 普通 | 资产当前业主查询 |
| `resi_meter_device` | `uk_meter_code` | `(project_id, meter_code)` | 唯一 | 防仪表编号重复 |
| `resi_meter_device` | `idx_room_id` | `(room_id)` | 普通 | 房间挂表查询 |
| `resi_fee_allocation` | `uk_alloc` | `(fee_id, resource_type, resource_id, start_date)` | 唯一 | 防重复分配 |
| `resi_fee_allocation` | `idx_resource` | `(resource_type, resource_id)` | 普通 | 资源已分配费用 |
| `resi_meter_reading` | `uk_meter_period` | `(meter_id, period)` | 唯一 | 防重复抄表 |
| `resi_meter_reading` | `idx_project_period` | `(project_id, period)` | 普通 | 期间抄表汇总 |
| `resi_meter_reading` | `idx_room_period` | `(room_id, period)` | 普通 | 房间抄表历史 |
| `resi_meter_reading` | `idx_status` | `(status)` | 普通 | 查待入账记录 |
| `resi_receivable` | `idx_project_period` | `(project_id, bill_period)` | 普通 | **收费率报表（最高频）** |
| `resi_receivable` | `idx_resource_state` | `(resource_type, resource_id, pay_state)` | 普通 | **收银台查待缴费用** |
| `resi_receivable` | `idx_customer` | `(customer_id)` | 普通 | C端查我的账单 |
| `resi_receivable` | `idx_gen_batch` | `(gen_batch)` | 普通 | 批量删除重生成 |
| `resi_receivable` | `idx_pay_state_period` | `(project_id, pay_state, bill_period)` | 普通 | 欠费统计报表 |
| `resi_pay_log` | `uk_pay_no` | `(pay_no)` | 唯一 | 收据号唯一 |
| `resi_pay_log` | `idx_project_time` | `(project_id, creator_time)` | 普通 | **日结/交易汇总** |
| `resi_pay_log` | `idx_resource` | `(resource_type, resource_id)` | 普通 | 资源收款历史 |
| `resi_pre_account` | `uk_pre_account` | `(resource_type, resource_id, fee_id)` | 唯一 | 预收款账户唯一 |
| `resi_push_record` | `idx_status_retry` | `(status, retry_count)` | 普通 | 定时任务扫描待发 |
| `resi_butler_room` | `uk_butler_room` | `(butler_id, room_id)` | 唯一 | 防重复关联 |
| `resi_butler_room` | `idx_room_id` | `(room_id)` | 普通 | 房间找管家 |

### 6.2 索引使用原则

```sql
-- 原则一：高选择性字段在联合索引左侧
-- ✅ (project_id, bill_period, pay_state) — project_id 选择性高
-- ❌ (pay_state, project_id, bill_period) — pay_state 只有4个值，选择性低

-- 原则二：等值条件字段在范围条件字段之前
-- ✅ (project_id, pay_state, bill_period) — bill_period 做范围查询
-- 对应 SQL: WHERE project_id=? AND pay_state='0' AND bill_period>='2026-01'

-- 原则三：覆盖索引避免回表（报表高频查询）
-- 交易汇总报表：SELECT pay_method, SUM(pay_amount) ... GROUP BY pay_method
-- 建议在 (project_id, creator_time, pay_method, pay_amount) 上建覆盖索引（按需添加）

-- 原则四：不为低基数字段单独建索引
-- ❌ KEY `idx_pay_state` (`pay_state`)   -- 只有4个值，MySQL 全表扫更快
-- ✅ 放在联合索引的后续位置使用

-- 原则五：JSON 字段不建索引
-- resi_receivable.receivable_ids 等 JSON 字段不建索引
-- 若需按 JSON 内容查询，应改用应用层查询或抽取为关联表
```

### 6.3 慢查询预防

以下场景在设计初期就需防范：

```sql
-- 场景一：收费率报表（月度汇总，数据量大）
-- 问题 SQL: SELECT COUNT(*), SUM(receivable), SUM(paid_amount) 
--           FROM resi_receivable WHERE project_id=? AND bill_period=?
-- 优化：idx_project_period 覆盖，强制传 bill_period 参数（不允许全量报表）

-- 场景二：欠费明细（跨月欠费汇总）
-- 问题 SQL: SELECT * FROM resi_receivable 
--           WHERE project_id=? AND pay_state='0' AND delete_time IS NULL
-- 优化：idx_pay_state_period，限制返回行数（分页），最多展示最近12个月

-- 场景三：收银台房间搜索（模糊查询）
-- 问题 SQL: WHERE room_alias LIKE '%101%'
-- 优化：idx_room_alias，前缀模糊可用索引；全文搜索退化为全表时考虑 FULLTEXT 索引
-- CREATE FULLTEXT INDEX ft_room_alias ON resi_room(room_alias) WITH PARSER ngram;

-- 场景四：批量生成应收（月初并发）
-- 优化：异步 Job 执行，分批 INSERT（500条/批），非高峰时段（01:00-02:00）触发
```

---

## 7. 分区策略

### 7.1 分区适用场景评估

| 表名 | 数据规模估算 | 增长速度 | 是否分区 | 分区方案 |
|---|---|---|---|---|
| `resi_receivable` | 1000户×12月×N费用 ≈ 数十万/年 | 月初批量增长 | **建议** | 按年 RANGE 分区 |
| `resi_pay_log` | 与 receivable 同级 | 日常持续增长 | **建议** | 按年 RANGE 分区 |
| `resi_meter_reading` | 仪表数×12期/年 | 月均匀增长 | 可选 | 按年 RANGE 分区 |
| `resi_pre_pay` | 较小 | 低 | 不需要 | — |
| 档案类表 | 小（千级） | 极低 | 不需要 | — |
| C端表 | 推送记录可能较大 | 中 | 可选 | 按月清理历史 |

### 7.2 resi_receivable 分区方案

```sql
-- 按创建年份 RANGE 分区（应收账单历史保留3年）
-- 注意：MySQL RANGE 分区需要分区键在主键或唯一键中
-- 方案：将 creator_time 的年份提取为虚拟列作为分区键

ALTER TABLE `resi_receivable`
  ADD COLUMN `create_year` SMALLINT GENERATED ALWAYS AS (YEAR(creator_time)) STORED COMMENT '创建年份（分区键，由creator_time生成）',
  ADD INDEX `idx_create_year` (`create_year`);

-- 分区定义（需在建表时指定，不能在现有表上直接改）
-- 建表时在末尾添加：
PARTITION BY RANGE (`create_year`) (
  PARTITION p2025 VALUES LESS THAN (2026) COMMENT '2025年及以前数据',
  PARTITION p2026 VALUES LESS THAN (2027) COMMENT '2026年数据',
  PARTITION p2027 VALUES LESS THAN (2028) COMMENT '2027年数据',
  PARTITION p2028 VALUES LESS THAN (2029) COMMENT '2028年数据',
  PARTITION p_future VALUES LESS THAN MAXVALUE COMMENT '未来数据'
);

-- 年度维护：每年初添加新分区（在 Quartz 任务中执行）
ALTER TABLE `resi_receivable`
  ADD PARTITION (PARTITION p2029 VALUES LESS THAN (2030));

-- 历史数据归档（超过3年的分区可删除或导出后删除）
ALTER TABLE `resi_receivable` DROP PARTITION p2025;
```

### 7.3 resi_pay_log 分区方案

```sql
-- 同样按年 RANGE 分区
-- 分区键：creator_time 年份

ALTER TABLE `resi_pay_log`
  ADD COLUMN `create_year` SMALLINT GENERATED ALWAYS AS (YEAR(creator_time)) STORED COMMENT '创建年份（分区键）';

PARTITION BY RANGE (`create_year`) (
  PARTITION p2025 VALUES LESS THAN (2026),
  PARTITION p2026 VALUES LESS THAN (2027),
  PARTITION p2027 VALUES LESS THAN (2028),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 7.4 resi_push_record 历史清理策略

推送记录无需长期保留，采用定期清理替代分区：

```sql
-- 每月清理3个月前已发送成功的推送记录（Quartz 定时任务执行）
DELETE FROM `resi_push_record`
WHERE `status` = 1
  AND `create_time` < DATE_SUB(NOW(), INTERVAL 3 MONTH);

-- 保留失败记录用于问题排查（保留6个月）
DELETE FROM `resi_push_record`
WHERE `status` = 2
  AND `retry_count` >= 3
  AND `create_time` < DATE_SUB(NOW(), INTERVAL 6 MONTH);
```

### 7.5 分区注意事项

```
1. 分区键必须包含在主键或唯一键中（MySQL 限制）
   → 因此添加 create_year 虚拟列，不影响现有业务查询

2. 分区裁剪（Partition Pruning）生效条件：
   WHERE 条件中包含分区键 create_year 或 creator_time
   → 所有报表查询必须传时间范围参数，否则退化为全表扫描

3. 跨分区 JOIN 性能较差：
   → 收款财务相关的多表 JOIN 查询，确保 WHERE 中都带时间条件

4. 当前数据量未达到分区阈值（< 100万行）时，可暂不实施分区
   → 优先通过索引解决性能问题，在数据量达到 50万行 时再评估分区
```

---

## 8. 字段注释规范

### 8.1 COMMENT 书写规范

```sql
-- ✅ 正确：说明字段含义 + 合法值域 + 业务规则
`pay_state` CHAR(1) NOT NULL DEFAULT '0'
  COMMENT '缴费状态：0未收 1部分收 2已收 3减免',

`overdue_rate` DECIMAL(8,6) NULL
  COMMENT '滞纳金日利率，0.000500表示日万分之五（0.05%/天）',

`formula` TEXT NULL
  COMMENT '计费公式，FORMULA类型时有效，语法：if/elsif/else/return，变量：单价、数量',

`gen_batch` VARCHAR(50) NULL
  COMMENT '批量生成批次号，格式 GEN-{projectId}-{period}，临时费为NULL',

-- ❌ 错误：注释等于没有，不说明业务含义
`pay_state` CHAR(1) COMMENT '支付状态',
`formula` TEXT COMMENT '公式',
`gen_batch` VARCHAR(50) COMMENT '批次号',

-- ✅ 正确：冗余字段说明为何冗余
`fee_name` VARCHAR(100) NOT NULL
  COMMENT '费用名称（冗余，防止改名影响历史数据可读性）',

`resource_name` VARCHAR(100) NULL
  COMMENT '资源名称（冗余，避免列表页联表查询）',

-- ✅ 正确：关联字段说明关联目标
`pay_log_id` VARCHAR(50) NULL
  COMMENT '最后一次收款流水ID，关联 resi_pay_log.id',

`meter_reading_id` VARCHAR(50) NULL
  COMMENT '来源抄表记录ID，仅仪表类费用有值，关联 resi_meter_reading.id',
```

### 8.2 特殊字段必须注释的内容

| 字段场景 | COMMENT 必须包含 |
|---|---|
| 枚举/状态字段 | 所有合法值及含义 |
| 计算字段 | 计算公式 |
| 冗余字段 | 说明"冗余"原因 |
| 外键字段 | 关联的目标表和字段 |
| 加密字段 | 加密算法说明 |
| 时间格式约定字段 | 格式说明，如 `yyyy-MM` |
| NULL 含义特殊的字段 | NULL 的业务语义 |
| 生成列 | 生成规则说明 |

### 8.3 表级 COMMENT

每张表的 `COMMENT` 必须简明描述业务用途（不超过20个中文字）：

```sql
-- ✅
COMMENT='应收账单（住宅收费核心表）'
COMMENT='收款流水（不可删除）'
COMMENT='抄表记录'
COMMENT='管家评价（每月限评一次）'

-- ❌
COMMENT='resi_receivable'
COMMENT='数据表'
```

---

## 9. 与现有表的复用关系

| 新模块需求 | 复用现有表 | 说明 |
|---|---|---|
| 票据流水号 | `base_billrule` | 新增规则记录 `en_code='RESI_RECEIPT'`，不修改表结构 |
| 操作日志 | `sys_oper_log` | AOP 自动写入，零修改 |
| 消息推送 | `sys_sms` + `sys_sms_template` | 写入消息队列，现有发送任务消费 |
| 定时任务 | `sys_job` + `sys_job_log` | 新增任务记录，复用 Quartz 调度器 |
| 用户权限 | `sys_user` + `sys_role` + `sys_menu` | 新增菜单项，复用现有 RBAC |
| 数据字典 | `sys_dict_type` + `sys_dict_data` | 新增字典类型 `resi_fee_type` 等 |
| 单位/部门 | `sys_dept` | 项目负责人从现有用户体系选取 |

**新增数据字典**（需在 `sys_dict_type` 和 `sys_dict_data` 中初始化）：

| dict_type | dict_name | 用途 |
|---|---|---|
| `resi_fee_type` | 收费类型 | PERIOD/TEMP/DEPOSIT/PRE |
| `resi_calc_type` | 计费方式 | FIXED/AREA/USAGE/FORMULA |
| `resi_pay_method` | 支付方式 | CASH/WECHAT/TRANSFER/BANK |
| `resi_meter_type` | 仪表类型 | 1水表/2电表/3燃气/4暖气 |
| `resi_room_type` | 房间类型 | 1住宅/2商铺/3车库/4储藏室 |

---

## 10. 初始化脚本

### 10.1 base_billrule 新增记录

```sql
-- 住宅收费收据流水规则
-- 格式示例：ZS202605100001（前缀ZS + 日期8位 + 6位流水）
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
```

### 10.2 sys_dict_type 初始化

```sql
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('住宅收费-费用类型',   'resi_fee_type',    '0', 'admin', NOW(), 'PERIOD周期费/TEMP临时费/DEPOSIT押金/PRE预收款'),
('住宅收费-计费方式',   'resi_calc_type',   '0', 'admin', NOW(), 'FIXED固定/AREA按面积/USAGE按用量/FORMULA公式'),
('住宅收费-支付方式',   'resi_pay_method',  '0', 'admin', NOW(), 'CASH现金/WECHAT微信/TRANSFER转账/BANK银行'),
('住宅收费-仪表类型',   'resi_meter_type',  '0', 'admin', NOW(), '1水表/2电表/3燃气表/4暖气表'),
('住宅收费-房间类型',   'resi_room_type',   '0', 'admin', NOW(), '1住宅/2商铺/3车库/4储藏室'),
('住宅收费-客户类型',   'resi_customer_type','0','admin', NOW(), '1业主/2租户/3临时');
```

### 10.3 sys_job 定时任务初始化

```sql
INSERT INTO `sys_job` (`job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `remark`) VALUES
('住宅收费-管理层日报推送',  'RESI', 'resiDailyReportJob.execute',   '0 0 18 * * ?',    '3', '1', '0', 'admin', NOW(), '每日18:00推送收费日报至管理层'),
('住宅收费-滞纳金自动计算',  'RESI', 'resiOverdueCalcJob.execute',    '0 0 1 * * ?',     '3', '1', '0', 'admin', NOW(), '每日01:00重新计算欠费应收的滞纳金'),
('住宅收费-周期费自动生成',  'RESI', 'resiBillAutoGenJob.execute',    '0 0 2 1 * ?',     '3', '1', '0', 'admin', NOW(), '每月1日02:00自动批量生成当月应收'),
('住宅收费-欠费通知推送',    'RESI', 'resiArrearsNoticeJob.execute',  '0 0 9 10 * ?',    '3', '1', '0', 'admin', NOW(), '每月10日09:00自动发送欠费提醒'),
('住宅收费-推送消息重试',    'RESI', 'resiPushRetryJob.execute',      '0 0 */2 * * ?',   '3', '1', '0', 'admin', NOW(), '每2小时重试失败的推送消息（最多3次）'),
('住宅收费-推送记录清理',    'RESI', 'resiPushCleanJob.execute',      '0 0 3 1 * ?',     '3', '1', '0', 'admin', NOW(), '每月1日清理3个月前已发送成功的推送记录');
```

---

*本文档与技术方案文档（物业收费系统-新增收费模块技术方案.md）及项目规范（CLAUDE.md）共同构成开发基准文件，三者保持一致。如有变更须同步更新。*  
*最后更新：2026-05-10*
