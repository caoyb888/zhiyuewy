# CLAUDE.md — 肇新智慧物业·住宅收费模块开发规范

> 本文件是项目 AI 辅助开发的行为约束文件。所有代码生成、重构、补全操作均须遵守以下规范。
> 每次开始新任务前，必须完整阅读本文件。

---

## 目录

1. [项目概述](#1-项目概述)
2. [技术栈约束](#2-技术栈约束)
3. [代码结构规范](#3-代码结构规范)
4. [数据库规范](#4-数据库规范)
5. [后端开发规范](#5-后端开发规范)
6. [前端开发规范](#6-前端开发规范)
7. [接口设计规范](#7-接口设计规范)
8. [安全与权限规范](#8-安全与权限规范)
9. [错误处理规范](#9-错误处理规范)
10. [测试规范](#10-测试规范)
11. [Git 提交规范](#11-git-提交规范)
12. [禁止行为清单](#12-禁止行为清单)
13. [SQL 脚本管理规范](#13-sql-脚本管理规范)
14. [复用现有能力速查](#14-复用现有能力速查)

---

## 1. 项目概述

### 1.1 系统定位

本项目为**肇新智慧物业系统（zhaoxinwy）**的功能扩展，在现有商业地产收费体系之上，新增**住宅物业收费模块（resi）**。两套收费体系并行运行，互不干扰。

### 1.2 核心约束

```
禁止修改任何以下前缀的表：payment_ / config_ / sys_ / owner_ / park_ / ACT_ / qrtz_
禁止修改任何现有 Controller / Service / Mapper 文件
禁止新增 Maven 子模块（C端 zhaoxinwy-wxmp 除外）
所有新代码必须在隔离的命名空间内（包名 resi，表前缀 resi_，接口前缀 /resi/）
```

### 1.3 模块归属

| 新增内容 | 归属位置 | 说明 |
|---|---|---|
| 住宅收费后端 | `zhaoxinwy-pms` 模块，包 `com.zhaoxinms.resi` | 与现有 `payment/baseconfig` 包并列 |
| C端微信小程序后端 | 新模块 `zhaoxinwy-wxmp` | 独立 Spring Boot 应用，端口 8081 |
| 前端页面 | `pms-web`，路径 `src/views/resi/` | 新增顶级菜单"住宅收费" |

---

## 2. 技术栈约束

### 2.1 版本锁定（不得升级）

| 组件 | 版本 | 备注 |
|---|---|---|
| Java | 1.8 | 源码/目标字节码为 Java 8，不得使用 Java 9+ 特性 |
| Spring Boot | 2.5.6 | 不得升级 |
| 编译 JDK | 11 | **开发/测试环境使用 OpenJDK 11 编译**，生产部署使用 JDK 8 |
| MyBatis-Plus | 3.4.0 | 不得升级 |
| MySQL | 8.0 | 驱动 `com.mysql.cj.jdbc.Driver` |
| Redis | 6 | 客户端 Spring Data Redis |
| Vue | 2.6.12 | 不得迁移 Vue 3 |
| Element UI | 2.15.6 | 不得替换为其他 UI 库 |
| ECharts | 4.9.0 | 图表库，已在项目中引入 |
| Axios | 0.24.0 | HTTP 请求，不得引入其他 HTTP 库 |

### 2.2 允许新引入的依赖

新增依赖须经评审，以下为预批准可直接使用的：

```xml
<!-- 仅在确实需要时引入，且必须在父 pom 中统一声明版本 -->
<!-- WxJava 微信 SDK（zhaoxinwy-wxmp 模块） -->
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>weixin-java-miniapp</artifactId>
    <version>4.4.0</version>
</dependency>
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>weixin-java-pay</artifactId>
    <version>4.4.0</version>
</dependency>
```

**禁止引入**：Lombok（项目未使用）、MapStruct、任何 ORM 框架（只用 MyBatis-Plus）、任何消息中间件（MQ）。

### 2.3 主键策略

```java
// 档案类实体（project/room/customer 等）：数据库自增 BIGINT
@TableId(type = IdType.AUTO)
private Long id;

// 流水类实体（pay_log/receivable 等）：UUID，与现有 payment_pay_log.id 保持一致
@TableId(type = IdType.ASSIGN_UUID)
private String id;
```

---

## 3. 代码结构规范

### 3.1 后端包结构（严格遵守）

```
zhaoxinwy-pms/src/main/java/com/zhaoxinms/resi/
├── archive/          # 基础档案
│   ├── controller/   # 只放 Controller，无业务逻辑
│   ├── service/      # 接口定义
│   ├── service/impl/ # 业务实现
│   ├── mapper/       # MyBatis-Plus Mapper
│   ├── entity/       # 数据库实体（与表一一对应）
│   └── dto/          # 请求/响应 DTO（不直接暴露 entity）
├── feeconfig/        # 费用配置（费用定义/分配/票据/折扣）
├── meter/            # 抄表管理
├── receivable/       # 应收管理
├── cashier/          # 收银台
├── finance/          # 财务台账（流水/预收款/押金/发票）
├── report/           # 数据查询报表
└── dashboard/        # 看板

zhaoxinwy-wxmp/src/main/java/com/zhaoxinms/wxmp/
├── auth/             # 微信授权登录
├── notice/           # 通知公告
├── payment/          # 微信缴费
├── push/             # 消息推送
├── butler/           # 管家展示
└── convenience/      # 便民信息
```

### 3.2 每层职责边界

```
Controller  → 参数校验、调用 Service、返回 AjaxResult。禁止包含业务逻辑和 SQL。
Service     → 业务逻辑、事务管理、调用 Mapper。禁止直接操作 HttpServletRequest。
Mapper      → 只写 SQL（XML 或注解）。禁止包含业务判断。
Entity      → 与表字段一一对应，不含业务方法。
DTO         → 请求/响应数据传输，不含 @TableField 等 MyBatis 注解。
```

### 3.3 命名规范

| 类型 | 规则 | 示例 |
|---|---|---|
| Controller 类 | `Resi{业务名}Controller` | `ResiCashierController` |
| Service 接口 | `Resi{业务名}Service` | `ResiReceivableService` |
| Service 实现 | `Resi{业务名}ServiceImpl` | `ResiReceivableServiceImpl` |
| Mapper 接口 | `Resi{业务名}Mapper` | `ResiRoomMapper` |
| Entity 类 | `Resi{业务名}` | `ResiRoom` |
| 请求 DTO | `Resi{业务名}{动作}Req` | `ResiCollectReq` |
| 响应 DTO | `Resi{业务名}{动作}Vo` | `ResiRoomDetailVo` |
| Mapper XML | `Resi{业务名}Mapper.xml` | `ResiRoomMapper.xml` |
| 常量类 | `Resi{业务名}Constants` | `ResiPayConstants` |

### 3.4 前端文件结构

```
pms-web/src/
├── views/resi/                    # 住宅收费所有页面
│   ├── archive/                   # 基础档案页面
│   │   ├── project/index.vue
│   │   ├── room/index.vue
│   │   ├── customer/index.vue
│   │   ├── meter-device/index.vue
│   │   └── parking/index.vue
│   ├── feeconfig/
│   │   ├── definition/index.vue
│   │   ├── allocation/index.vue
│   │   ├── ticket/index.vue
│   │   └── discount/index.vue
│   ├── cashier/index.vue          # 收银台（单页核心）
│   ├── meter/index.vue
│   ├── receivable/index.vue
│   ├── report/                    # 各报表子页面
│   └── dashboard/index.vue
└── api/resi/                      # 接口封装
    ├── archive.js
    ├── feeconfig.js
    ├── cashier.js
    ├── meter.js
    ├── receivable.js
    ├── finance.js
    └── report.js
```

---

## 4. 数据库规范

### 4.1 表命名

- **新建表**：统一使用 `resi_` 前缀，全小写，下划线分隔
- **禁止**：修改或 ALTER 任何非 `resi_` 前缀的表
- **字符集**：`utf8mb4`，排序规则 `utf8mb4_unicode_ci`
- **存储引擎**：`InnoDB`

### 4.2 公共字段规范

所有 `resi_` 业务表必须包含以下字段（档案类表）：

```sql
`enabled_mark`     TINYINT     NOT NULL DEFAULT 1   COMMENT '有效标志 1有效 0无效',
`create_by`        VARCHAR(64) DEFAULT ''            COMMENT '创建者',
`create_time`      DATETIME                          COMMENT '创建时间',
`update_by`        VARCHAR(64) DEFAULT ''            COMMENT '更新者',
`update_time`      DATETIME                          COMMENT '更新时间'
```

流水类表（pay_log/meter_reading 等）使用：

```sql
`creator_time`        DATETIME    COMMENT '创建时间',
`creator_user_id`     VARCHAR(50) COMMENT '创建用户',
`last_modify_time`    DATETIME    COMMENT '修改时间',
`last_modify_user_id` VARCHAR(50) COMMENT '修改用户'
```

> 与现有 `payment_pay_log`、`config_fee_item` 等表风格一致，两种风格不得混用于同一张表。

### 4.3 软删除规范

- 档案类表：通过 `enabled_mark = 0` 软删除，不物理删除
- 流水类表（pay_log/meter_reading）：不允许删除，只允许状态变更
- 需要物理删除的临时数据（如未收应收）：通过 `delete_time` + `delete_user_id` 字段记录，查询时加 `WHERE delete_time IS NULL`

对应 Entity 配置：

```java
// 档案类
@TableLogic(value = "1", delval = "0")
private Integer enabledMark;

// 流水类（不配置 @TableLogic，禁止删除）
```

### 4.4 索引规范

```sql
-- 必须建索引的场景
-- 1. 高频查询的外键字段
KEY `idx_project_id` (`project_id`)
KEY `idx_resource` (`resource_type`, `resource_id`)

-- 2. 高频筛选组合字段
KEY `idx_project_period` (`project_id`, `bill_period`)
KEY `idx_resource_state` (`resource_type`, `resource_id`, `pay_state`)

-- 3. 防重复的业务唯一键（单据号、编码等）
UNIQUE KEY `uk_pay_no` (`pay_no`)
UNIQUE KEY `uk_meter_period` (`meter_id`, `period`)

-- 禁止：在低基数字段（如 tinyint 状态字段）上单独建索引
-- 禁止：建超过 5 列的联合索引
```

### 4.5 字段类型规范

| 数据类型 | 使用场景 | 示例字段 |
|---|---|---|
| `VARCHAR(50)` | UUID 主键、编码、单号 | `id`, `pay_no`, `fee_code` |
| `BIGINT AUTO_INCREMENT` | 档案类自增主键 | `id` |
| `DECIMAL(12,2)` | 金额（元） | `amount`, `total`, `receivable` |
| `DECIMAL(12,4)` | 单价、用量 | `unit_price`, `billed_usage` |
| `DECIMAL(8,6)` | 利率、比率 | `overdue_rate`, `discount_rate` |
| `TINYINT` | 状态枚举（值域≤10） | `enabled_mark`, `meter_type` |
| `VARCHAR(20)` | 字符串枚举（值域>10） | `pay_state`, `fee_type` |
| `TEXT` | 长文本、公式、JSON字符串 | `formula` |
| `JSON` | 结构化数组数据 | `receivable_ids`, `target_ids` |
| `DATE` | 日期（无时分秒） | `bill_period`起止日期 |
| `DATETIME` | 时间戳 | `create_time`, `pay_time` |

**禁止使用**：`FLOAT`、`DOUBLE`（精度问题），用 `DECIMAL` 代替。

---

## 5. 后端开发规范

### 5.1 必须继承/复用的基础类

```java
// Controller 必须继承
public class ResiXxxController extends BaseController { }

// 统一响应，禁止自定义响应结构
return AjaxResult.success(data);
return AjaxResult.error("错误信息");

// 分页，禁止自定义分页类
startPage();  // BaseController 提供
List<T> list = service.list(queryWrapper);
return getDataTable(list);
```

### 5.2 事务规范

```java
// 涉及多表写操作的 Service 方法，必须加事务注解
@Transactional(rollbackFor = Exception.class)
public CollectResult collect(ResiCollectReq req) { ... }

// 查询方法禁止加 @Transactional
// 禁止在 Controller 层加 @Transactional
// 禁止使用编程式事务（TransactionTemplate），只用声明式
```

### 5.3 并发安全规范

**收款、退款、冲红** 等核心写操作必须防并发：

```java
// 方式一：SELECT FOR UPDATE（适用于单机）
QueryWrapper<ResiReceivable> qw = new QueryWrapper<>();
qw.in("id", req.getReceivableIds())
  .eq("pay_state", "0")
  .last("FOR UPDATE");  // 必须在事务内
List<ResiReceivable> locked = receivableMapper.selectList(qw);

// 方式二：Redis 分布式锁（适用于幂等保证，如微信支付回调）
String lockKey = "resi:pay:notify:" + orderId;
Boolean locked = redisTemplate.opsForValue()
    .setIfAbsent(lockKey, "1", 60, TimeUnit.SECONDS);
if (!locked) {
    return; // 已处理，直接返回成功（幂等）
}
```

**批量生成应收** 防重复：生成前先查询该批次是否已存在，不依赖数据库唯一键冲突处理。

### 5.4 Redis 使用规范

```java
// Key 命名格式：resi:{模块}:{项目ID}:{业务标识}
// 示例
"resi:dashboard:5:overview"          // 看板数据，TTL 5分钟
"resi:pay:notify:ORDER202605100001"  // 支付回调幂等锁，TTL 60秒
"resi:receipt:no:5:20260510"         // 收据号自增，TTL 至当日结束

// 看板缓存 TTL
redisTemplate.expire(key, 300, TimeUnit.SECONDS);

// 收款后主动清除看板缓存
Set<String> keys = redisTemplate.keys("resi:dashboard:" + projectId + ":*");
if (keys != null) redisTemplate.delete(keys);

// 禁止：使用 keys("*") 做全局扫描（性能问题）
// 禁止：将金额、账单等核心业务数据只存 Redis 不落库
```

### 5.5 MyBatis-Plus 使用规范

```java
// ✅ 推荐：简单查询用 QueryWrapper
LambdaQueryWrapper<ResiReceivable> qw = new LambdaQueryWrapper<>();
qw.eq(ResiReceivable::getProjectId, projectId)
  .eq(ResiReceivable::getPayState, "0")
  .orderByAsc(ResiReceivable::getBillPeriod);

// ✅ 推荐：批量插入用 saveBatch，每批 500 条
resiReceivableService.saveBatch(list, 500);

// ✅ 推荐：复杂 SQL（报表、多表关联）写在 XML 中
// resources/mapper/resi/ResiReceivableMapper.xml

// ❌ 禁止：在 Wrapper 中写原生 SQL 字符串（SQL注入风险）
qw.apply("date_format(create_time,'%Y-%m') = '" + month + "'");  // 禁止

// ✅ 正确写法：使用占位符
qw.apply("date_format(create_time,'%Y-%m') = {0}", month);

// ❌ 禁止：select * 查询（必须明确字段或使用实体映射）
// ✅ 报表类查询统一在 XML 中写明字段列表
```

### 5.6 公式引擎调用

费用计算公式（梯度电价等）复用现有引擎，调用方式：

```java
// 注入现有公式计算器（具体类名以现有代码为准）
@Autowired
private FormulaCalculator formulaCalculator;  // 现有 Bean

// 调用：传入公式文本 + 变量值
BigDecimal result = formulaCalculator.calculate(
    feeDef.getFormula(),
    Map.of("单价", feeDef.getUnitPrice(), "数量", usage)
);
```

**禁止**：自行实现公式解析逻辑，必须复用现有引擎。

### 5.7 票据号生成

**必须**复用现有 `base_billrule` 体系：

```java
// 调用现有 BillRuleService（通过 Spring 注入，不走 HTTP）
@Autowired
private BillRuleService billRuleService;  // 现有 Service

String receiptNo = billRuleService.useBillNumber("RESI_RECEIPT");
// 对应 base_billrule 表中 en_code = 'RESI_RECEIPT' 的规则
```

**禁止**：自行用 Redis INCR 或 UUID 生成单据号（破坏现有流水号管理体系）。

### 5.8 操作日志

无需手动写日志，现有 AOP 自动拦截带 `@Log` 注解的 Controller 方法：

```java
@Log(title = "收银台-收款", businessType = BusinessType.UPDATE)
@PostMapping("/collect")
public AjaxResult collect(@RequestBody @Validated ResiCollectReq req) { ... }
```

### 5.9 Excel 导入导出

```java
// 复用现有 POI 工具类（以项目现有工具为准）
// 导入：参考 PaymentMeterImportController 的实现模式
// 导出：参考 DailyReportController 的导出实现

// 导入文件大小限制：单次不超过 10MB
// 导出数据量限制：超过 5万行必须分批或异步，不允许一次性加载全部数据到内存
```

---

## 6. 前端开发规范

### 6.1 API 封装规范

所有接口调用必须封装在 `src/api/resi/` 目录下，禁止在组件中直接调用 `axios`：

```javascript
// src/api/resi/cashier.js
import request from '@/utils/request'

export function collectPayment(data) {
  return request({
    url: '/resi/cashier/collect',
    method: 'post',
    data
  })
}

export function getRoomReceivables(roomId, params) {
  return request({
    url: `/resi/cashier/room/${roomId}/receivables`,
    method: 'get',
    params
  })
}
```

### 6.2 组件规范

```vue
<!-- 页面组件结构（必须遵守顺序） -->
<template>
  <!-- 使用 Element UI 组件，禁止引入其他 UI 库 -->
</template>

<script>
// 导入顺序：API > 公共组件 > 工具函数
import { collectPayment } from '@/api/resi/cashier'

export default {
  name: 'ResiCashier',     // name 必填，用于 keep-alive 缓存
  components: {},
  props: {},
  data() {
    return {
      loading: false,      // 所有异步操作必须有 loading 状态
      tableData: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  methods: {
    async handleCollect() {
      this.loading = true
      try {
        const res = await collectPayment(this.form)
        if (res.code === 200) {
          this.$message.success('收款成功')
          this.loadData()
        }
      } finally {
        this.loading = false  // 无论成功失败都要关闭 loading
      }
    }
  }
}
</script>
```

### 6.3 金额显示规范

```javascript
// 所有金额字段：
// 1. 后端传输单位：元（Number/String，保留2位小数）
// 2. 前端显示：千分位格式化 + 保留2位小数
// 3. 禁止在前端做金额加减计算（全部由后端返回计算结果）

// 使用过滤器或工具函数，统一格式化
// src/utils/resi.js
export function formatMoney(val) {
  if (val === null || val === undefined) return '—'
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}
```

### 6.4 表格规范

```vue
<!-- 列表页标准结构 -->
<el-table v-loading="loading" :data="tableData">
  <!-- 金额列必须右对齐 -->
  <el-table-column label="应收金额" align="right" width="120">
    <template slot-scope="scope">
      {{ scope.row.receivable | formatMoney }}
    </template>
  </el-table-column>
  <!-- 状态列使用 el-tag -->
  <el-table-column label="缴费状态" align="center" width="90">
    <template slot-scope="scope">
      <el-tag :type="payStateTagType(scope.row.payState)">
        {{ payStateLabel(scope.row.payState) }}
      </el-tag>
    </template>
  </el-table-column>
</el-table>
<!-- 分页组件（参数名与后端保持一致） -->
<pagination :total="total" :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize" @pagination="loadData"/>
```

### 6.5 收银台专项规范

收银台是系统最复杂的页面，额外约束：

- **禁止**在前端计算最终应收金额，只展示后端 `payCalc` 接口返回的计算结果
- 收款按钮必须做防重复点击（disabled + loading 双重保护）
- 选中费用变化时，必须调用 `/resi/cashier/collect/calc` 预览接口刷新金额，不得本地计算
- 打印操作必须在收款成功的回调中触发，不得在收款请求前触发

---

## 7. 接口设计规范

### 7.1 路径规范

```
# B端接口
/resi/{模块}/{资源}[/{id}][/{动作}]

# 示例
GET    /resi/archive/room                 # 房间列表
POST   /resi/archive/room                 # 新建房间
GET    /resi/archive/room/{id}            # 房间详情
PUT    /resi/archive/room/{id}            # 更新房间
DELETE /resi/archive/room/{id}            # 删除房间
POST   /resi/archive/room/transfer        # 房屋过户（非标准 CRUD 动作用 POST + 动词）
POST   /resi/cashier/collect              # 收款
GET    /resi/report/collection-rate       # 收费率报表（报表用连字符）

# C端接口
/wxmp/{模块}/{动作}
/wxmp/payment/bills
/wxmp/auth/login
```

**禁止**：在新接口中使用 PascalCase 路径（如 `/resi/PaymentBill`），现有接口历史遗留不做修改。

### 7.2 请求/响应规范

```java
// 统一响应格式（复用现有 AjaxResult）
{ "code": 200, "msg": "操作成功", "data": {...} }
{ "code": 500, "msg": "业务异常描述", "data": null }

// 分页响应（复用现有 TableDataInfo）
{
  "code": 200,
  "msg": "查询成功",
  "rows": [...],
  "total": 100
}

// 列表查询：GET + Query String 参数
// 单记录操作：路径参数传 id
// 创建/批量操作：POST + JSON Body
// 更新：PUT + 路径 id + JSON Body
// 删除：DELETE + 路径 id（支持逗号分隔批量删除，参考现有 /{ids} 模式）
```

### 7.3 参数校验规范

```java
// 必须使用 Bean Validation，禁止手动 if 判空
public class ResiCollectReq {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotEmpty(message = "请至少选择一条应收费用")
    private List<String> receivableIds;

    @NotBlank(message = "支付方式不能为空")
    private String payMethod;

    @NotNull(message = "实收金额不能为空")
    @DecimalMin(value = "0.01", message = "实收金额必须大于0")
    private BigDecimal payAmount;
}

// Controller 方法参数加 @Validated
@PostMapping("/collect")
public AjaxResult collect(@RequestBody @Validated ResiCollectReq req) { ... }
```

### 7.4 报表接口规范

```java
// 所有报表接口支持两种响应模式：
// 1. 普通查询：返回分页数据
// 2. 导出：返回 Excel 文件流（参数 export=true）

@GetMapping("/collection-rate")
public AjaxResult collectionRate(ResiCollectionRateQuery query,
                                  HttpServletResponse response) {
    if (query.isExport()) {
        // 导出逻辑（参考现有 DailyFeeReportController.export()）
        exportExcel(response, data, "收费率报表");
        return null;
    }
    startPage();
    List<ResiCollectionRateVo> list = reportService.collectionRate(query);
    return getDataTable(list);
}
```

---

## 8. 安全与权限规范

### 8.1 接口鉴权

```java
// B端接口：所有接口默认需要登录（Spring Security 全局配置已覆盖）
// 不需要额外加注解，除非是公开接口（白名单由 SecurityConfig 统一配置）

// C端接口：使用独立 JWT，在 SecurityConfig 中配置 /wxmp/** 使用 C端 Token 过滤器
// C端 JWT Payload 示例
{
  "customerId": 1001,
  "openid": "oXxx...",
  "projectId": 5,
  "roomIds": [101, 102],
  "exp": 1748000000
}
```

### 8.2 项目数据隔离（必须实现）

**每个查询必须校验 projectId 归属**，防止跨项目数据泄露：

```java
// Service 实现中，查询前必须校验当前用户是否有权限访问该 projectId
// 复用现有 SecurityUtils 获取当前用户
Long userId = SecurityUtils.getUserId();
// 查询用户可访问的 projectIds（从缓存或DB）
List<Long> allowedProjectIds = getUserAllowedProjectIds(userId);
if (!allowedProjectIds.contains(req.getProjectId())) {
    throw new ServiceException("无权访问该项目数据");
}

// 所有涉及 project_id 的列表查询，Wrapper 中必须加项目过滤
qw.in("project_id", allowedProjectIds);
```

### 8.3 敏感字段处理

```java
// 身份证号：AES-256 加密存储，查询时返回脱敏数据
// Entity 中标注加密字段
private String idCard;  // 存储加密后的值

// VO 中返回脱敏数据
public String getIdCardMasked() {
    return idCard != null ? idCard.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2") : null;
}

// 手机号：展示时脱敏（138****8888）
// 金额：所有金额字段必须用 DECIMAL，禁止用浮点型
```

### 8.4 SQL 注入防护

```java
// ✅ 使用 MyBatis-Plus 参数绑定
qw.apply("date_format(create_time,'%Y-%m') = {0}", month);

// ✅ XML 中使用 #{} 绑定，禁止 ${} 拼接
WHERE project_id = #{projectId}

// ❌ 绝对禁止字符串拼接 SQL
String sql = "WHERE name = '" + name + "'";  // 严禁
```

### 8.5 微信支付安全

```java
// 支付回调必须验签，使用 WxJava SDK 的验签方法
// 回调接口必须做幂等处理（Redis 锁，见 5.3 节）
// 支付金额必须与服务端订单金额二次比对，不信任回调中的金额参数
// 微信支付密钥、AppSecret 等只存配置文件或环境变量，禁止硬编码
```

---

## 9. 错误处理规范

### 9.1 异常层级

```java
// 业务异常：抛出现有 ServiceException（或项目通用业务异常类）
throw new ServiceException("该房间已有未收费用，请先处理");

// 参数异常：由 @Validated 自动抛出 MethodArgumentNotValidException，全局处理
// 系统异常：不捕获，让全局异常处理器处理，自动记录日志
```

### 9.2 禁止的错误处理模式

```java
// ❌ 禁止：catch 后吞掉异常不处理
try {
    service.doSomething();
} catch (Exception e) {
    // 什么都不做
}

// ❌ 禁止：返回 null 代替抛出异常
public ResiRoom getRoom(Long id) {
    return null;  // 应该抛出异常或返回 Optional
}

// ❌ 禁止：在 Controller 层 try-catch 业务逻辑（让全局处理器统一处理）
// ✅ 正确：Service 层抛异常，Controller 层不 try-catch，由全局 GlobalExceptionHandler 处理
```

### 9.3 日志规范

```java
// 使用 Slf4j，不使用 System.out.println
private static final Logger log = LoggerFactory.getLogger(ResiCashierServiceImpl.class);

// INFO：关键业务节点（收款成功、生成应收完成）
log.info("收款完成 projectId={} roomId={} amount={} payNo={}", projectId, roomId, amount, payNo);

// WARN：可恢复的异常（预收款余额不足、折扣已过期）
log.warn("预收款余额不足 accountId={} required={} balance={}", accountId, required, balance);

// ERROR：系统级错误（不在此层 catch，由全局处理器记录）

// 禁止：在日志中打印完整的 JSON 请求体（可能含敏感信息）
// 禁止：在循环内打 INFO 日志（批量生成时会刷爆日志）
```

---

## 10. 测试规范

### 10.1 必须编写单元测试的场景

```
收款金额计算逻辑（含折扣、滞纳金、预收款冲抵）
公式引擎调用（梯度计费验证）
批量生成应收的幂等逻辑
微信支付回调的幂等验证
```

### 10.2 编译与测试环境

开发环境使用 **OpenJDK 11** 编译和运行测试（系统已安装），目标字节码仍为 Java 8：

```bash
# 设置编译环境
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# 全量安装（跳过测试）
mvn install -DskipTests

# 运行档案层全部测试
mvn test -pl zhaoxinwy-pms -Dtest="com.zhaoxinms.resi.**"

# 运行单个测试类
mvn test -pl zhaoxinwy-pms -Dtest=EntityValidationTest

# 生成 JaCoCo 覆盖率报告（test 阶段自动触发）
mvn test -pl zhaoxinwy-pms -Dtest="com.zhaoxinms.resi.**"
# 报告输出：zhaoxinwy-pms/target/site/jacoco/index.html
```

> 环境 JDK 21 因 `maven-compiler-plugin:3.1` 和 Lombok 1.18.20 不兼容无法编译，已修复代码中 `sun.management.Agent` 无用导入并升级插件至 3.8.1。

### 10.3 测试类规范

```java
// 测试类放在对应模块的 src/test/ 目录
// 命名：{被测类名}Test.java
@SpringBootTest
@Transactional  // 测试用例结束后回滚，不污染数据库
class ResiCashierServiceTest {

    @Autowired
    private ResiCashierService cashierService;

    @Test
    void testCollect_success() {
        // Given
        ResiCollectReq req = buildCollectReq();
        // When
        CollectResult result = cashierService.collect(req);
        // Then
        assertNotNull(result.getPayNo());
        assertEquals(new BigDecimal("200.00"), result.getPaidAmount());
    }

    @Test
    void testCollect_duplicateRequest_shouldBeIdempotent() {
        // 验证重复收款请求的幂等处理
    }
}
```

---

## 11. Git 提交规范

### 11.1 分支策略

```
main          # 生产环境，只接受来自 release/* 的 merge
develop       # 开发主干，所有功能分支从此拉取
feature/resi-{模块名}-{简述}   # 功能分支
hotfix/resi-{问题简述}         # 紧急修复

# 示例
feature/resi-cashier-collect    # 收银台收款功能
feature/resi-report-dashboard   # 看板数据接口
feature/resi-wxmp-payment       # 微信支付
```

### 11.2 Commit Message 规范

```
{类型}({范围}): {简述}

{详细描述（可选）}

类型：
  feat      新功能
  fix       Bug 修复
  refactor  重构（不改变功能）
  docs      文档更新
  test      测试用例
  chore     构建/依赖变更

范围（resi 模块相关）：
  resi-archive    基础档案
  resi-cashier    收银台
  resi-meter      抄表
  resi-receivable 应收管理
  resi-report     报表
  resi-dashboard  看板
  resi-wxmp       C端微信
  resi-db         数据库变更

# 示例
feat(resi-cashier): 实现收款核心接口含预收款冲抵

实现 POST /resi/cashier/collect 接口
- 支持多费用勾选合并收款
- 支持预收款自动冲抵（专款专冲）
- 使用 SELECT FOR UPDATE 防并发重复收款
- 收款成功后清除看板 Redis 缓存
```

### 11.3 代码审查要求

每个 PR 合并前检查项：

- [ ] 是否修改了非 `resi_` 前缀的表或现有接口（**不允许**）
- [ ] 收款/退款等核心接口是否有事务注解
- [ ] 核心写操作是否有并发保护（FOR UPDATE 或 Redis 锁）
- [ ] 新增接口是否有 `@Log` 注解
- [ ] 查询接口是否有 projectId 数据隔离校验
- [ ] 金额计算是否全部使用 `BigDecimal`（禁止 double）
- [ ] 是否有 `System.out.println`（禁止）
- [ ] 新增表 DDL 是否已更新到版本管理（Flyway 脚本或 SQL 文件）

---

## 12. 禁止行为清单

以下行为在任何情况下均不允许，AI 生成代码时不得包含：

### 绝对禁止

```
❌ 修改或 ALTER 任何 payment_* / config_* / sys_* / owner_* / park_* / ACT_* / qrtz_* 表
❌ 修改现有任何 Controller / Service / Mapper / Entity 文件
❌ 在接口路径中使用 /payment/ 或 /baseconfig/ 前缀（新接口）
❌ 在循环内执行数据库查询（N+1 问题）
❌ 使用 double / float 类型存储金额或进行金额计算
❌ 在 Controller 层写业务逻辑
❌ 在 Mapper 层写业务判断
❌ 硬编码密钥、密码、AppSecret 等敏感信息
❌ 使用 ${} 拼接 SQL 参数（MyBatis XML 中）
❌ 吞掉异常（catch 后不处理）
❌ 升级任何已有依赖版本
❌ 引入 Lombok 注解（项目未使用 Lombok）
❌ 在前端组件中直接调用 axios（必须通过 api/ 封装）
❌ 在前端进行金额加减计算（必须由后端返回）
❌ 收款按钮没有防重复点击保护
❌ 微信支付回调不做验签
❌ 不在测试类上加 @Transactional（污染测试数据库）
```

### 需要评审才能执行

```
⚠️ 新增 Maven 依赖（需填写理由，说明无替代现有实现）
⚠️ 新增 Quartz 定时任务（需确认 cron 表达式和幂等性）
⚠️ 在 Redis 中存储超过 24 小时的业务数据
⚠️ 单次查询数据量超过 1000 条（需分页或分批）
⚠️ 导出数据量超过 5 万行（需异步处理）
⚠️ 修改 Spring Security 配置（需架构师确认）
```

---

## 13. SQL 脚本管理规范

### 13.1 脚本存放位置

```
sql/{版本号}/
├── resi_init.sql      -- 28 张 resi_ 表 DDL
└── resi_init_data.sql -- 初始化数据（字典/菜单/定时任务/流水规则）
```

### 13.2 版本号规则

- 版本号遵循 `主版本.次版本.修订号` 格式
- 当前住宅收费模块初始版本：`0.11.0`
- 后续表结构变更（ALTER / 新增表）需创建新版本目录，如 `0.11.1`、`0.12.0`

### 13.3 脚本命名

| 脚本类型 | 命名规则 | 示例 |
|---|---|---|
| DDL（建表/索引） | `resi_init.sql` | `sql/0.11.0/resi_init.sql` |
| DML（初始化数据） | `resi_init_data.sql` | `sql/0.11.0/resi_init_data.sql` |
| 表结构变更 | `resi_alter_{描述}_{日期}.sql` | `sql/0.11.1/resi_alter_add_index_20260520.sql` |

### 13.4 执行顺序

1. 先执行 DDL 脚本（创建表、索引、约束）
2. 再执行 DML 脚本（插入初始化数据）
3. 表结构变更脚本按版本号顺序执行

### 13.5 已交付脚本清单

| 版本 | 脚本 | 说明 |
|---|---|---|
| `0.11.0` | `resi_init.sql` | 28 张 `resi_` 表完整 DDL，含索引、唯一键、字段注释。引擎 `InnoDB`，字符集 `utf8mb4` |
| `0.11.0` | `resi_init_data.sql` | `base_billrule` 住宅收据流水规则（`RESI_RECEIPT`）、`sys_dict_type/data` 6 组数据字典（24 条数据）、`sys_job` 6 条定时任务、`sys_menu` 99 项住宅收费完整菜单树（含 `resi:*` 权限标识） |

---

## 14. 复用现有能力速查

> 遇到以下需求时，**不要自己实现**，直接复用现有能力。

| 需求 | 复用方式 | 相关文件/接口 |
|---|---|---|
| 单据流水号 | 注入 `BillRuleService`，调用 `useBillNumber(enCode)` | `base_billrule` 表，`/Base/BillRule/useBillNumber/{enCode}` |
| 操作日志 | 方法上加 `@Log(title="...", businessType=BusinessType.UPDATE)` | AOP 自动写入 `sys_oper_log` |
| 消息推送/短信 | 写入 `sys_sms` 表（`es_send_status='0'`），由现有定时任务发送 | `sys_sms`, `sys_sms_template` |
| 定时任务 | 在 `sys_job` 表插入新任务记录，实现 `Job` 接口 | `zhaoxinwy-quartz` 模块 |
| 文件上传/下载 | 调用 `/common/upload` 和 `/common/download/resource` | `CommonController` |
| Excel 导入模板 | 参考 `PaymentMeterImportController` 的 `/Template` + `/Uploader` + `/Import` 三步模式 | `zhaoxinwy-pms` |
| 统一分页 | `startPage()` + `getDataTable(list)` | `BaseController` |
| 统一响应 | `AjaxResult.success(data)` / `AjaxResult.error(msg)` | `AjaxResult` 类 |
| 当前登录用户 | `SecurityUtils.getUserId()` / `SecurityUtils.getUsername()` | `SecurityUtils` 类 |
| 公式计算引擎 | 注入现有 `FormulaCalculator` Bean | `zhaoxinwy-pms`（具体类名以源码为准）|
| 打印数据格式 | 参考 `PayLogPrintController` 的返回结构 | `/print/printData/*` |
| 前端分页组件 | `<pagination>` 组件，已全局注册 | `src/components/Pagination` |
| 前端权限按钮 | `v-hasPermi="['resi:cashier:collect']"` 指令 | 现有权限指令 |
| 前端富文本 | 现有 `<editor>` 组件 | `src/components/Editor` |

---

*本文件随项目演进持续更新。如有规范变更，须同步更新本文件并在 PR 中注明。*  
*最后更新：2026年5月11日（新增：编译环境使用 OpenJDK 11）*
