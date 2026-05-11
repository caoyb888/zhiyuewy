# TEST PLAN
## 肇新智慧物业 · 住宅物业收费模块

**文档版本**：V1.0  
**编制日期**：2026-05-10  
**编制人**：QA  
**审核人**：PM  
**适用范围**：`zhaoxinwy-pms`（resi 包）+ `zhaoxinwy-wxmp`（C端模块）  
**测试周期**：2026-06-01 ～ 2026-10-09（随 Sprint 节奏滚动执行）

---

## 目录

1. [测试策略总览](#1-测试策略总览)
2. [测试用例设计策略](#2-测试用例设计策略)
   - 2.1 [设计方法论](#21-设计方法论)
   - 2.2 [分模块用例设计](#22-分模块用例设计)
   - 2.3 [边界与异常场景矩阵](#23-边界与异常场景矩阵)
   - 2.4 [安全测试用例](#24-安全测试用例)
3. [测试环境规划](#3-测试环境规划)
   - 3.1 [环境架构](#31-环境架构)
   - 3.2 [环境配置清单](#32-环境配置清单)
   - 3.3 [测试数据规划](#33-测试数据规划)
4. [自动化测试范围](#4-自动化测试范围)
   - 4.1 [分层自动化策略](#41-分层自动化策略)
   - 4.2 [单元测试规范](#42-单元测试规范)
   - 4.3 [接口自动化测试](#43-接口自动化测试)
   - 4.4 [回归测试套件](#44-回归测试套件)
5. [性能测试基准指标](#5-性能测试基准指标)
   - 5.1 [性能目标定义](#51-性能目标定义)
   - 5.2 [各接口性能基准](#52-各接口性能基准)
   - 5.3 [压测场景设计](#53-压测场景设计)
   - 5.4 [性能测试执行计划](#54-性能测试执行计划)
6. [缺陷管理规范](#6-缺陷管理规范)
7. [各 Sprint 测试计划](#7-各-sprint-测试计划)
8. [测试交付物清单](#8-测试交付物清单)

---

## 1. 测试策略总览

### 1.1 测试目标

| 目标 | 具体指标 |
|---|---|
| 功能正确性 | 所有 Sprint 验收标准 100% 通过，无 P0 遗留 |
| 数据准确性 | 金额计算误差为零（BigDecimal 全链路），报表数据与 SQL 直查结果一致 |
| 并发安全 | 收款核心接口 50 并发下无数据重复、无账目错误 |
| 性能达标 | 核心接口 P99 < 1s，看板接口缓存命中 < 200ms |
| 安全合规 | 权限隔离无穿透，敏感字段全脱敏，支付回调验签 100% |
| 上线质量 | 生产冒烟通过，上线后 4 小时无 P0 错误日志 |

### 1.2 测试类型与执行阶段

```
开发阶段          集成阶段          验收阶段          上线阶段
────────────────────────────────────────────────────────────
单元测试           接口测试           UAT               冒烟测试
（开发自测）       （QA 执行）        （业务人员）       （QA 执行）
                   功能测试
                   安全测试           性能测试
                   回归测试
```

### 1.3 测试优先级框架

```
P0（阻塞级）：系统崩溃 / 数据错误 / 账目不平 / 权限穿透 / 支付异常
              → 发现即停测，当日必须修复，修复后重测

P1（严重级）：核心功能不可用 / 业务流程断裂 / 数据丢失风险
              → 当 Sprint 内必须修复，不得带入下个 Sprint

P2（一般级）：非核心功能异常 / 交互体验缺陷 / 非关键字段错误
              → 优先级排期修复，可跨 Sprint

P3（轻微级）：UI 显示问题 / 文案错误 / 非必要功能缺失
              → 收集后统一处理，不阻断上线
```

### 1.4 测试入口与出口标准

**入口标准（每 Sprint 开始测试前）**：
- 开发完成，代码已提交 develop 分支并通过 CI 构建
- 测试环境已部署最新版本，Swagger 文档可访问
- QA 完成该 Sprint 用例编写

**出口标准（每 Sprint 测试结束）**：
- P0 Bug：0 个未关闭
- P1 Bug：0 个未关闭
- P2 Bug：已记录，有修复计划
- 用例执行率：≥ 95%，通过率：≥ 95%
- 测试报告已输出并经 PM 确认

---

## 2. 测试用例设计策略

### 2.1 设计方法论

#### 方法一：等价类 + 边界值（适用于字段输入类测试）

针对所有输入字段，划分有效等价类和无效等价类，并在边界值处各取一个测试点。

```
示例：建筑面积字段（buildingArea，DECIMAL(10,2)，必填，> 0）

有效等价类：0.01 ～ 99999999.99
无效等价类：null / 0 / 负数 / 超精度（0.001）/ 超长（100000000）/ 非数字

边界值：0.01（有效下界）/ 0.00（无效）/ 99999999.99（有效上界）/ 100000000（超界）
```

#### 方法二：状态迁移（适用于有状态流转的业务对象）

对有明确状态机的对象，覆盖所有合法迁移路径和非法迁移路径。

**应收账单（resi_receivable）状态机**：
```
                 收全款
         0未收 ────────────► 2已收
           │                   ▲
           │ 部分收款          │ 补收尾款
           ▼                   │
         1部分收 ──────────────┘
           │
           │ 减免操作
           ▼
         3减免

非法迁移（必须测试拒绝）：
  2已收 → 直接收款（应提示"已缴清"）
  3减免 → 收款（应提示"已减免"）
  已复核流水 → 冲红（应提示"已复核不可撤销"）
```

**抄表记录（resi_meter_reading）状态机**：
```
INPUT（已录入）── 入账操作 ──► BILLED（已入账）── 复核 ──► VERIFIED（已复核）

非法迁移：
  BILLED → 修改读数（应拒绝：已入账不可修改）
  VERIFIED → 删除（应拒绝：已复核不可删除）
```

#### 方法三：因果图 / 决策表（适用于多条件组合业务规则）

**收款金额计算决策表**：

| 条件 | 场景A | 场景B | 场景C | 场景D | 场景E |
|---|---|---|---|---|---|
| 是否有滞纳金 | 否 | 是 | 是 | 是 | 否 |
| 是否使用折扣 | 否 | 否 | 是 | 否 | 是 |
| 是否冲抵预收款 | 否 | 否 | 否 | 是 | 是 |
| **期望：应收合计** | total | total+overdue | total+overdue-disc | total+overdue-pre | total-disc-pre |
| **期望：实收金额** | =应收 | =应收 | =应收 | =应收-预收 | =应收 |

**专款专冲决策表**：

| 条件 | 情况1 | 情况2 | 情况3 | 情况4 |
|---|---|---|---|---|
| earmark_enable | 0（否） | 1（是） | 1（是） | 1（是） |
| 预收款账户 fee_id | NULL（通用） | = 本次费用 | ≠ 本次费用 | NULL（通用） |
| **期望结果** | 可冲抵 | 可冲抵 | 拒绝（专款专冲） | 拒绝（专款专冲） |

#### 方法四：场景法（适用于端到端业务流程）

设计完整业务场景，覆盖从档案录入到收款打印的完整链路：

**场景 S-001：新业主入驻完整流程**
```
前置条件：项目、楼栋已配置，物业费费用定义已创建

步骤：
  1. 新建房间（1栋1单元101，建筑面积128.5㎡）
  2. 新建客户（张三，手机138XXXX8888）
  3. 客户绑定房间
  4. 费用分配（物业费2.8元/㎡ → 101室）
  5. 批量生成2026-05应收（物业费=2.8×128.5=359.80元）
  6. 收银台搜索"101"，找到该房间
  7. 勾选物业费，实收359.80元（现金）
  8. 打印收款单

期望结果：
  - 应收记录金额359.80元（2.8×128.5，精度正确）
  - pay_state='2'（已收清）
  - pay_log 有流水记录，pay_no 符合 ZSyyyyMMddNNNNNN 格式
  - sys_oper_log 有"收银台-收款"操作记录
  - 打印数据接口返回正确字段
```

**场景 S-002：欠费 + 滞纳金 + 减免流程**
```
前置条件：某房间2026-03物业费359.80元未收

步骤：
  1. 等待/Mock 滞纳金计算 Job（逾期15天，日利率0.05%）
  2. 验证 overdue_fee = 359.80 × 0.0005 × 15 = 2.70（四舍五入）
  3. 收银台执行减免违约金操作（减免全部2.70元）
  4. 验证调账记录存在（adjust_type=OVERDUE_WAIVE）
  5. 收款359.80元

期望结果：
  - overdue_fee=0.00，receivable=359.80
  - resi_adjust_log 有 before_value=2.70，after_value=0.00
  - 最终 paid_amount=359.80，pay_state='2'
```

### 2.2 分模块用例设计

#### 模块1：基础档案（S1）

**TC-ARCH-001 项目管理**

| 用例ID | 用例标题 | 前置条件 | 步骤 | 期望结果 | 优先级 |
|---|---|---|---|---|---|
| TC-ARCH-001-01 | 创建项目-正常 | 管理员已登录 | POST /resi/archive/project，传完整合法数据 | HTTP 200，data.id 有值，DB 中 resi_project 有记录 | P0 |
| TC-ARCH-001-02 | 创建项目-编号重复 | 编号 PRJ-001 已存在 | 再次 POST 相同 code | HTTP 500，msg 含"编号已存在"，DB 无新记录 | P0 |
| TC-ARCH-001-03 | 创建项目-名称为空 | — | POST，name 为空字符串 | HTTP 400，msg 含"项目名称不能为空" | P1 |
| TC-ARCH-001-04 | 软删除项目 | 项目无关联数据 | DELETE /resi/archive/project/1 | HTTP 200，DB 中 enabled_mark=0，GET 列表不含该项目 | P0 |
| TC-ARCH-001-05 | 删除有下级数据的项目 | 项目下有楼栋 | DELETE 该项目 | HTTP 500，msg 含"存在关联楼栋，无法删除" | P1 |
| TC-ARCH-001-06 | 非该项目权限用户访问 | 用户A仅有项目1权限 | 用户A GET 项目2的房间列表 | HTTP 200 但返回空数据（data=[]/total=0），不报错不穿透 | P0 |

**TC-ARCH-002 房间管理**

| 用例ID | 用例标题 | 步骤摘要 | 期望结果 | 优先级 |
|---|---|---|---|---|
| TC-ARCH-002-01 | 创建房间-正常 | 传完整合法数据 | 200，room_alias 自动生成（"1号楼1单元101"） | P0 |
| TC-ARCH-002-02 | 创建房间-同楼栋同单元同房号重复 | 相同 building_id+unit_no+room_no 再次创建 | 500，唯一键约束错误提示 | P0 |
| TC-ARCH-002-03 | 批量导入-正常500行 | 上传合法Excel，500条数据 | 200，成功数=500，耗时<10秒 | P0 |
| TC-ARCH-002-04 | 批量导入-含错误行 | Excel含10行缺少必填字段 | 预览阶段返回错误行列表（含行号+原因），正常行可继续导入 | P0 |
| TC-ARCH-002-05 | 批量导入-超大文件 | 上传>10MB的Excel | 400，msg 含"文件大小超出限制10MB" | P1 |
| TC-ARCH-002-06 | 收银台搜索-模糊匹配 | GET /resi/cashier/room/search?keyword=101 | 返回所有 room_alias 含"101"的房间，按项目分组 | P0 |
| TC-ARCH-002-07 | 房屋过户-事务原子性 | 过户操作中途 Mock 抛出异常 | 全部回滚：旧绑定未置0，无新绑定，无过户记录 | P0 |
| TC-ARCH-002-08 | 房屋过户-正常 | 新业主已存在，执行过户 | 旧绑定 is_current=0+unbind_date=today，新绑定 is_current=1，resi_room_transfer 有记录 | P0 |

**TC-ARCH-003 客户管理**

| 用例ID | 用例标题 | 期望结果关键点 | 优先级 |
|---|---|---|---|
| TC-ARCH-003-01 | 创建客户-身份证加密 | DB 中 id_card 为密文（非原文），列表返回脱敏格式 110***1234 | P0 |
| TC-ARCH-003-02 | 手机号脱敏展示 | 列表/详情接口返回 138****8888，非明文 | P0 |
| TC-ARCH-003-03 | 手机号格式校验 | 传入非11位手机号，返回 400 + 格式错误提示 | P1 |
| TC-ARCH-003-04 | 绑定资产-同资产同期重复绑定 | 同房间已有当前业主，再次绑定应提示"该资产当前已有业主" | P0 |

**TC-ARCH-004 仪表档案**

| 用例ID | 用例标题 | 期望结果关键点 | 优先级 |
|---|---|---|---|
| TC-ARCH-004-01 | 创建仪表-编号项目内唯一 | 同项目相同 meter_code 重复创建，返回唯一键错误 | P0 |
| TC-ARCH-004-02 | 公摊总表-room_id 可为空 | isPublic=1 时 roomId 传 null，正常创建成功 | P1 |
| TC-ARCH-004-03 | 分户表-room_id 必填 | isPublic=0 时 roomId 传 null，返回校验错误 | P1 |

---

#### 模块2：费用配置（S2）

**TC-FEE-001 费用定义**

| 用例ID | 用例标题 | 步骤摘要 | 期望结果 | 优先级 |
|---|---|---|---|---|
| TC-FEE-001-01 | 创建固定费用 | calc_type=FIXED，unit_price=200 | 生成应收时金额=200.00（不依赖数量） | P0 |
| TC-FEE-001-02 | 创建按面积费用 | calc_type=AREA，unit_price=2.80 | 生成128.5㎡房间应收=2.80×128.5=359.80（ROUND） | P0 |
| TC-FEE-001-03 | 创建梯度公式费用-三段电价 | calc_type=FORMULA，formula 为三段梯度 | 公式预览接口：230度→计算结果=121.51（0.5283×230） | P0 |
| TC-FEE-001-04 | 梯度公式-第二段验证 | 用量300度 | 结果=0.5783×300=173.49 | P0 |
| TC-FEE-001-05 | 梯度公式-第三段验证 | 用量500度 | 结果=0.8783×500=439.15 | P0 |
| TC-FEE-001-06 | 取整方式-CEIL | round_type=CEIL，计算结果=359.804 | 最终金额=359.81（向上取整） | P1 |
| TC-FEE-001-07 | 取整方式-FLOOR | round_type=FLOOR，计算结果=359.806 | 最终金额=359.80（截尾） | P1 |
| TC-FEE-001-08 | 滞纳金配置-日万分之五 | overdue_rate=0.000500，逾期10天，本金1000元 | overdue_fee=1000×0.0005×10=5.00 | P0 |
| TC-FEE-001-09 | 滞纳金上限 | overdue_max=10，逾期计算结果=15 | 实际滞纳金=10.00（上限截断） | P1 |
| TC-FEE-001-10 | 专款专冲标记 | earmark_enable=1 | 该费用的预收款不可冲抵其他费用（收银台测试） | P0 |

**TC-FEE-002 费用分配**

| 用例ID | 用例标题 | 期望结果关键点 | 优先级 |
|---|---|---|---|
| TC-FEE-002-01 | 批量分配-按楼栋 | 选楼栋1（50个房间），成功数=50，返回统计 | P0 |
| TC-FEE-002-02 | 批量分配-含已存在分配 | 30个已分配+20个未分配，成功=20，跳过=30，无报错 | P0 |
| TC-FEE-002-03 | 个性化单价覆盖 | 房间101设 custom_price=3.00，费用定义 unit_price=2.80 | 生成应收时101室用3.00，其他房间用2.80 | P0 |
| TC-FEE-002-04 | 分配有效期-截止日期过期 | end_date=昨天 | 生成应收时该分配被跳过（end_date<today） | P0 |

---

#### 模块3：抄表管理（S3）

**TC-METER-001 抄表核心逻辑**

| 用例ID | 用例标题 | 验证数据 | 期望结果 | 优先级 |
|---|---|---|---|---|
| TC-METER-001-01 | 单户录入-用量计算 | last=100，curr=200，multiplier=1 | raw_usage=100，billed_usage=100（无损耗无公摊） | P0 |
| TC-METER-001-02 | 倍率计算 | last=100，curr=200，multiplier=10 | raw_usage=1000 | P0 |
| TC-METER-001-03 | 损耗计算 | raw_usage=100，loss_rate=0.03 | loss_amount=3，billed_usage=97 | P1 |
| TC-METER-001-04 | 同仪表同期间重复录入 | meter_id=1，period=2026-05 第二次录入 | 500，唯一键约束错误提示 | P0 |
| TC-METER-001-05 | 读数回退预警 | curr_reading=90 < last_reading=100 | 导入预览中该行标记 WARNING，提示"读数小于上期读数" | P0 |
| TC-METER-001-06 | 用量超阈值预警 | 月用量超过2倍历史均值 | 导入预览中标记 WARNING（可继续导入） | P2 |

**TC-METER-002 公摊计算验证**

> 测试数据设计：公摊组 G01，总表1台 + 分户表3台

| 仪表 | 类型 | 楼层/房间 | 面积 | 本期用量 |
|---|---|---|---|---|
| 总表 E00 | 公摊总表 | — | — | 100度 |
| 分户 E01 | 分户表 | 101室 | 60㎡ | 30度 |
| 分户 E02 | 分户表 | 102室 | 40㎡ | 25度 |
| 分户 E03 | 分户表 | 103室 | 50㎡ | 20度 |

公摊量 = 100 - (30+25+20) = 25度  
总面积 = 60+40+50 = 150㎡

| 用例ID | 用例标题 | 期望结果 | 优先级 |
|---|---|---|---|
| TC-METER-002-01 | 公摊总量计算 | 公摊量=25度 | P0 |
| TC-METER-002-02 | 101室分摊量 | share_amount=25×(60/150)=10.00度，billed_usage=30+10=40.00 | P0 |
| TC-METER-002-03 | 102室分摊量 | share_amount=25×(40/150)=6.67度（ROUND），billed_usage=31.67 | P0 |
| TC-METER-002-04 | 103室分摊量 | share_amount=25×(50/150)=8.33度，billed_usage=28.33 | P0 |
| TC-METER-002-05 | 公摊量守恒 | 三户分摊量之和≈25（浮点误差≤0.01度） | P0 |
| TC-METER-002-06 | 总表缺失时触发公摊计算 | 公摊组内无总表，触发计算 | 500，提示"公摊组G01缺少总表" | P1 |

**TC-METER-003 批量导入**

| 用例ID | 用例标题 | 期望结果 | 优先级 |
|---|---|---|---|
| TC-METER-003-01 | 下载导入模板 | Excel含表头：仪表编号/期间/本次读数/抄表日期 | P0 |
| TC-METER-003-02 | 上传200行正常数据 | 预览：正常200行，错误0行，耗时<5秒 | P0 |
| TC-METER-003-03 | 上传含10行错误数据 | 预览：正常190行，错误10行（红色高亮+原因），可继续导入190行 | P0 |
| TC-METER-003-04 | 确认导入后 Redis 暂存清除 | import_batch 对应的 Redis key 在 confirm 后被删除 | P1 |
| TC-METER-003-05 | 超时重传（Redis 30分钟 TTL） | upload 后超30分钟再 confirm | 500，提示"导入会话已超时，请重新上传" | P1 |

---

#### 模块4：应收生成（S4）

**TC-RECV-001 批量生成**

| 用例ID | 用例标题 | 验证要点 | 优先级 |
|---|---|---|---|
| TC-RECV-001-01 | 批量生成-500个房间 | 生成500条，gen_batch 一致，耗时<20秒 | P0 |
| TC-RECV-001-02 | 防重复-同月二次生成 | 第二次返回：成功0，跳过500，不产生重复记录 | P0 |
| TC-RECV-001-03 | 金额精度-面积费 | 2.80×128.5=359.8000，ROUND后=359.80 | P0 |
| TC-RECV-001-04 | 金额精度-公式梯度费 | 300度，两段计费，结果精确到分 | P0 |
| TC-RECV-001-05 | 按批次删除重生成 | DELETE gen_batch，验证所有 pay_state='0' 的删除，pay_state='2' 的保留 | P0 |
| TC-RECV-001-06 | 已收费用不可批量删除 | DELETE gen_batch，包含已收记录 | 已收记录保留，仅删除未收记录，返回删除数+保留数 | P0 |

---

#### 模块5：收银台（S4/S5）★ 最高风险模块

**TC-CASH-001 收款核心**

| 用例ID | 用例标题 | 验证要点 | 优先级 |
|---|---|---|---|
| TC-CASH-001-01 | 正常收款-现金 | 收1笔物业费359.80，pay_state='2'，pay_log 有记录，收据号格式正确 | P0 |
| TC-CASH-001-02 | 合并收款-多笔费用 | 同时收物业费359.80+停车费200，总计559.80，一条 pay_log，receivable_ids=2个 | P0 |
| TC-CASH-001-03 | 收款-带折扣 | 9.5折，359.80×0.95=341.81，discount_amount=17.99 | P0 |
| TC-CASH-001-04 | 收款-带滞纳金 | overdue_fee=5.00，total=359.80，receivable=364.80，实收364.80 | P0 |
| TC-CASH-001-05 | 收款-预收款冲抵 | 预收余额100元，应收300元，实收=200元，pre_pay_amount=100 | P0 |
| TC-CASH-001-06 | 预收款余额不足 | 预收余额80元，应收300元，实收=220元（余额全部冲抵） | P0 |
| TC-CASH-001-07 | 专款专冲-同费用 | 物业费专款预收100元，收物业费300元，可冲抵100元 | P0 |
| TC-CASH-001-08 | 专款专冲-跨费用拒绝 | 物业费专款预收100元，尝试冲抵水费 | 500，提示"该预收款为专款，仅可冲抵物业费" | P0 |
| TC-CASH-001-09 | 并发收款-幂等性 ⚡ | 10个并发请求对同一应收收款 | 只有1次成功（pay_state='2'），其余返回"该费用已收取或被其他操作锁定" | P0 |
| TC-CASH-001-10 | 收款金额与应收不符 | 传入 payAmount=100，实际应收359.80 | 500，提示"实收金额不足，至少应收359.80元" | P0 |
| TC-CASH-001-11 | 收据号唯一性-并发 ⚡ | 100个并发请求触发收款，各自生成收据号 | 100个收据号全部唯一（无重复） | P0 |
| TC-CASH-001-12 | 收款后看板缓存失效 | 收款后立即访问看板 overview 接口 | 数据已更新（缓存被清除，重新计算） | P1 |

**TC-CASH-002 退款与冲红**

| 用例ID | 用例标题 | 验证要点 | 优先级 |
|---|---|---|---|
| TC-CASH-002-01 | 正常退款 | 已收359.80，退款359.80，pay_state='0'，新 pay_log（REFUND） | P0 |
| TC-CASH-002-02 | 超额退款拒绝 | paid_amount=359.80，退款500 | 500，提示"退款金额超出已收金额" | P0 |
| TC-CASH-002-03 | 冲红操作 | 对已收流水执行冲红，原流水 pay_type 不变，新建对冲流水 parent_log_id=原ID | P0 |
| TC-CASH-002-04 | 已复核流水禁止冲红 | is_verified=1 的流水执行冲红 | 500，提示"已复核收款单不可撤销" | P0 |

**TC-CASH-003 押金操作**

| 用例ID | 用例标题 | 验证要点 | 优先级 |
|---|---|---|---|
| TC-CASH-003-01 | 收取装修押金 | 收1000元押金，resi_deposit 有记录（state=COLLECTED） | P0 |
| TC-CASH-003-02 | 退还押金-全额 | 退1000元，state=REFUNDED，resi_pay_log 有退款记录 | P0 |
| TC-CASH-003-03 | 退还押金-超额拒绝 | 押金1000，退款1500 | 500，提示"退款金额超出押金" | P0 |

---

#### 模块6：报表（S5/S7）

**TC-RPT-001 报表数据准确性（核心方法：与 SQL 直查结果交叉验证）**

| 用例ID | 报表名称 | 验证方法 | 优先级 |
|---|---|---|---|
| TC-RPT-001-01 | 交易汇总 | 接口返回值 vs `SELECT pay_method, SUM(pay_amount) FROM resi_pay_log GROUP BY pay_method` | P0 |
| TC-RPT-001-02 | 收费率报表 | 接口收费率 vs `SUM(paid_amount)/SUM(receivable)` 手算 | P0 |
| TC-RPT-001-03 | 欠费明细 | 接口条数 vs `SELECT COUNT(*) FROM resi_receivable WHERE pay_state='0' AND delete_time IS NULL` | P0 |
| TC-RPT-001-04 | 日结明细 | 今日实收 vs `SELECT SUM(pay_amount) FROM resi_pay_log WHERE DATE(creator_time)=today AND pay_type='COLLECT'` | P0 |
| TC-RPT-001-05 | 预收款报表 | 各账户余额 vs resi_pre_account.balance | P0 |
| TC-RPT-001-06 | 费用情况矩阵 | 颜色状态 vs resi_receivable.pay_state（PAID/UNPAID/NOT_GEN/PART_PAID） | P0 |

**TC-RPT-002 报表查询性能与导出**

| 用例ID | 用例标题 | 验证要点 | 优先级 |
|---|---|---|---|
| TC-RPT-002-01 | 欠费明细-5000行分页查询 | 第1页响应<3秒，total 正确 | P1 |
| TC-RPT-002-02 | Excel 导出-1000行 | 文件生成<30秒，数据与页面查询一致 | P1 |
| TC-RPT-002-03 | 无筛选条件拦截 | 欠费明细不传时间范围，接口返回 400，提示"请选择查询时间范围" | P1 |

---

#### 模块7：C端微信（S8/S9）

**TC-WX-001 登录绑定**

| 用例ID | 用例标题 | 验证要点 | 优先级 |
|---|---|---|---|
| TC-WX-001-01 | 首次登录-未绑定 | 返回 `{needBind: true, openid}`，不生成 JWT | P0 |
| TC-WX-001-02 | 首次登录-已绑定 | 返回 JWT，payload 含 customerId/projectId/roomIds | P0 |
| TC-WX-001-03 | 绑定-短信验证码正确 | 绑定成功，resi_customer.openid 有值，resi_wx_user 有记录 | P0 |
| TC-WX-001-04 | 绑定-验证码过期（5分钟） | 500，提示"验证码已过期，请重新获取" | P0 |
| TC-WX-001-05 | 绑定-验证码错误 | 500，提示"验证码不正确" | P0 |
| TC-WX-001-06 | 短信限频-1分钟内重发 | 1分钟内第二次发送 | 500，提示"发送过于频繁，请1分钟后重试" | P1 |
| TC-WX-001-07 | JWT 过期-7天后 | 携带过期 Token 请求 | 401，前端跳转登录页 | P0 |
| TC-WX-001-08 | 数据隔离-业主只看自己房间 | 业主A（房间101）的账单接口 | 返回101室的账单，不含102室数据 | P0 |

**TC-WX-002 微信缴费**

| 用例ID | 用例标题 | 验证要点 | 优先级 |
|---|---|---|---|
| TC-WX-002-01 | 正常缴费流程（沙箱） | pre-create→支付→notify→查账单（状态=已缴）→电子收据 | P0 |
| TC-WX-002-02 | 支付回调幂等 | 同一订单微信重复回调2次 | 只处理1次（Redis 锁），第2次幂等返回成功 | P0 |
| TC-WX-002-03 | 支付回调验签 | 伪造回调请求（无效签名） | 拒绝处理，返回 FAIL | P0 |
| TC-WX-002-04 | 回调金额篡改 | 回调金额≠订单金额 | 拒绝，记录告警日志，不更新账单状态 | P0 |
| TC-WX-002-05 | 订单超时关闭 | 创建订单后30分钟未支付 | 订单 status=CLOSED，再次缴费提示"订单已过期，请重新发起" | P0 |
| TC-WX-002-06 | 重复下单拦截 | 同一业主同一批账单30秒内重复发起支付 | 返回已存在的订单，不新建 | P1 |
| TC-WX-002-07 | 电子收据展示 | 支付成功后获取电子收据 | 含：项目名/房间号/费用明细/金额/时间/流水号/公章图片 | P1 |

---

### 2.3 边界与异常场景矩阵

#### 金额计算边界矩阵

| 场景 | 输入 | 期望结果 | 异常处理 |
|---|---|---|---|
| 零金额费用 | unit_price=0 | 生成0.00元应收，正常记录 | 允许，用于免收场景 |
| 极大金额 | unit_price=99999999，面积1000㎡ | 生成99999999000.00，DECIMAL(12,2) 溢出 | 应在费用定义时校验上限 |
| 精度丢失风险 | 2.80×128.5=359.800 | 359.80（ROUND 正确） | 必须全程 BigDecimal，禁止 double |
| 滞纳金精度 | 1000.00×0.000500×15=7.500 | 7.50（ROUND） | 精度 DECIMAL(8,6) 不丢失 |
| 折扣率边界 | discount_rate=0（免费） | discount_amount=total | 允许 |
| 折扣率边界 | discount_rate=1（不打折） | discount_amount=0 | 允许 |
| 折扣率非法 | discount_rate=1.5（折后贵于原价） | 400，提示"折扣比例需在0到1之间" | 强制校验 |

#### 并发场景矩阵

| 场景 | 并发数 | 期望结果 | 测试工具 |
|---|---|---|---|
| 同一应收收款 | 10 | 只有1次成功（SELECT FOR UPDATE 保护） | JMeter |
| 收据号生成 | 100 | 100个号唯一（BillRuleService 原子性） | JMeter |
| 批量生成应收 | 1 | 单次串行执行，防重复由 gen_batch 控制 | 手工 |
| 预收款扣减 | 5 | 余额不会变为负数（FOR UPDATE 锁账户） | JMeter |
| 微信支付回调 | 3（同订单） | 只处理1次（Redis setIfAbsent 幂等） | JMeter |

#### 数据边界矩阵

| 字段 | 正常值 | 边界值测试 |
|---|---|---|
| room_no（VARCHAR 50） | "101" | 50个字符（通过），51个字符（拒绝） |
| formula（TEXT） | 普通公式 | 64KB超长公式（测试是否截断） |
| bill_period（VARCHAR 7） | "2026-05" | "2026-13"（非法月份应拒绝） |
| pay_amount（DECIMAL 12,2） | 359.80 | 0.00（拒绝），0.01（通过），10000000000.00（通过边界） |
| receivable_ids（JSON 数组） | [id1,id2] | 空数组[]（拒绝），500个ID（测试性能） |

---

### 2.4 安全测试用例

**TC-SEC-001 权限控制**

| 用例ID | 用例标题 | 验证要点 | 优先级 |
|---|---|---|---|
| TC-SEC-001-01 | 未登录访问 B端接口 | GET /resi/archive/room（无 Token） | 401 Unauthorized | P0 |
| TC-SEC-001-02 | 跨项目数据访问 | 用户A（项目1）访问项目2的房间列表 | 返回空，不报错，不穿透 | P0 |
| TC-SEC-001-03 | 跨业主账单访问（C端） | 业主A的 JWT 请求业主B的账单 | 返回空，不泄露业主B数据 | P0 |
| TC-SEC-001-04 | C端 Token 访问 B端接口 | 携带 wxmp JWT 请求 /resi/archive/room | 401 或 403，不允许混用 | P0 |
| TC-SEC-001-05 | 菜单权限-收银员无法访问费用配置 | 收银员角色账号访问 /resi/feeconfig/definition | 403 Forbidden | P0 |

**TC-SEC-002 输入安全**

| 用例ID | 用例标题 | 验证要点 | 优先级 |
|---|---|---|---|
| TC-SEC-002-01 | SQL 注入-查询参数 | GET ?keyword='; DROP TABLE resi_room;-- | 返回空结果，不执行注入，无500错误 | P0 |
| TC-SEC-002-02 | XSS-输入含脚本 | 房间备注传入 `<script>alert(1)</script>` | 存储转义，展示时不执行脚本 | P1 |
| TC-SEC-002-03 | 身份证明文泄露 | GET /resi/archive/customer 接口响应 | 响应中 idCard 字段为脱敏值，不含密文，不含明文 | P0 |
| TC-SEC-002-04 | 支付回调伪造 | 不携带微信签名直接 POST /wxmp/payment/notify | 拒绝处理，返回 FAIL，记录安全日志 | P0 |

---

## 3. 测试环境规划

### 3.1 环境架构

```
┌─────────────────────────────────────────────────────────────┐
│                     环境层次划分                             │
├──────────────┬──────────────┬──────────────┬────────────────┤
│   开发环境   │   测试环境   │  预发布环境  │   生产环境     │
│   (DEV)      │   (TEST)     │   (STAGING)  │   (PROD)       │
├──────────────┼──────────────┼──────────────┼────────────────┤
│ 开发自测用   │ QA 测试用    │ UAT 验收用   │ 正式运营       │
│ 本机或共享   │ 独立服务器   │ 与生产配置   │                │
│              │              │ 相同         │                │
├──────────────┼──────────────┼──────────────┼────────────────┤
│ 数据库随意   │ 测试数据集   │ 脱敏生产数据 │ 真实数据       │
│ 可随时重置   │ 固定+可重置  │ 只读导入     │ 严格备份       │
└──────────────┴──────────────┴──────────────┴────────────────┘
```

### 3.2 环境配置清单

#### 测试环境（TEST）— QA 主战场

| 组件 | 配置要求 | 说明 |
|---|---|---|
| **服务器** | 4C8G，SSD 100GB | Docker 宿主机 |
| **MySQL 8.0** | 独立实例，端口 3307，数据库名 pms_test | 与生产同版本，每 Sprint 可重置 |
| **Redis 6** | 独立实例，端口 6379，无密码 | 测试环境不设密码，方便 QA 查看缓存 |
| **应用（pms）** | 端口 8080，`SPRING_PROFILES_ACTIVE=test` | CI/CD 自动部署 |
| **应用（wxmp）** | 端口 8081，`SPRING_PROFILES_ACTIVE=test` | CI/CD 自动部署 |
| **Nginx** | 端口 80，转发 /resi/ → 8080，/wxmp/ → 8081 | 反向代理 |
| **微信沙箱** | 配置微信支付沙箱环境密钥 | C端支付测试专用 |

**测试环境专用配置（application-test.yml 关键项）**：
```yaml
# 测试环境特殊配置
resi:
  security:
    id-card-encrypt: true       # 保持加密，验证脱敏
  meter:
    import-threshold: 200       # 抄表异常阈值，测试用放宽
  dashboard:
    cache-ttl: 30               # 缩短到30秒，便于测试缓存失效
  wx:
    pay:
      sandbox: true             # 使用微信沙箱

# 测试专用：关闭邮件通知（避免骚扰）
spring:
  mail:
    enabled: false
```

#### 预发布环境（STAGING）— UAT 专用

| 组件 | 配置要求 | 说明 |
|---|---|---|
| **服务器** | 8C16G，与生产规格相同 | UAT 期间独占 |
| **MySQL** | 使用脱敏的生产数据快照 | Sprint 9 开始导入 |
| **配置** | 与生产完全相同，除域名和密钥外 | 验证生产配置正确性 |
| **微信** | 正式公众号/小程序的测试号 | 真实用户流程验证 |

#### 测试账号矩阵

| 账号 | 角色 | 权限 | 可访问项目 | 用途 |
|---|---|---|---|---|
| test_admin | 超级管理员 | 全部菜单 | 全部项目 | 基础数据配置 |
| test_cashier_1 | 收银员 | 收银台+档案（只读） | 阳光花园（项目1） | 收款场景测试 |
| test_cashier_2 | 收银员 | 收银台+档案（只读） | 碧水湾（项目2） | 权限隔离测试 |
| test_finance | 财务 | 报表+发票+收款记录 | 项目1+项目2 | 报表准确性测试 |
| test_readonly | 只读查询 | 仅报表查询 | 项目1 | 权限边界测试 |
| wx_owner_1 | C端业主 | — | 项目1·101室 | C端功能测试 |
| wx_owner_2 | C端业主 | — | 项目1·102室 | 数据隔离测试 |

### 3.3 测试数据规划

#### 基础测试数据集（S0 阶段脚本准备，随 Sprint 累积）

```sql
-- 数据规模目标（测试环境）
-- 项目：3个（阳光花园/碧水湾/翠湖名苑）
-- 楼栋：每项目5栋，共15栋
-- 房间：每栋20层×4户=80室，共1200个房间
-- 客户：1000位（覆盖大部分房间）
-- 仪表：每房间1个电表+1个水表，共2400个
-- 费用定义：每项目5种（物业费/水费/电费/停车费/装修押金）
-- 费用分配：每项目×每费用×每房间=覆盖全量
-- 历史应收：过去12个月，每月生成一次，约14400条
-- 历史流水：其中80%已收，约11520条

-- 关键测试场景数据
-- 欠费房间：固定50个房间设置为欠费2-3个月
-- 预收款账户：20个房间有余额（100-500元不等）
-- 押金：30个房间有未退押金
-- 特殊费用：10个房间有临时费
```

#### 数据重置策略

```
每 Sprint 开始前：
  1. 保留档案数据（project/building/room/customer/meter_device）
  2. 清空流水和应收数据（resi_receivable/resi_pay_log/resi_pre_pay 等）
  3. 重新执行应收生成脚本（生成最近3个月数据）
  4. 执行收款脚本（随机70%已收，30%未收）

目的：每个 Sprint 测试开始时有干净且具代表性的数据环境
工具：提供 reset_test_data.sql 脚本（QA 维护）
```

#### 微信测试账号

```
测试用微信账号：准备 3 个真实微信账号（非企业账号）
  - wx_test_01：已绑定业主（阳光花园101室）
  - wx_test_02：已绑定业主（阳光花园102室，用于数据隔离测试）
  - wx_test_03：未绑定（用于绑定流程测试）

微信支付沙箱：
  - 使用微信支付沙箱环境（mchid+sandbox key）
  - 固定测试金额（沙箱金额规则：总分=1，退款等于原支付金额）
```

---

## 4. 自动化测试范围

### 4.1 分层自动化策略

```
测试金字塔
                    ▲
                   / \
                  /UAT\         手工（S9 阶段，业务人员执行）
                 /─────\
                /性能测试\       JMeter（S10 阶段，QA+BE 执行）
               /─────────\
              / E2E/集成测试\    Postman Collections（S5后滚动执行）
             /─────────────\
            /  接口自动化测试 \  Postman + Newman（每 Sprint 末执行）
           /─────────────────\
          /    单元测试（Unit）  \ JUnit 5 + Mockito（开发同步编写）
         /─────────────────────\

自动化比例目标：
  单元测试：核心业务逻辑覆盖率 ≥ 70%
  接口测试：P0/P1 用例 100% 自动化
  E2E 测试：核心链路 5 条场景自动化
```

### 4.2 单元测试规范

**必须编写单元测试的类（CLAUDE.md 第10章规定）**：

| 类 | 测试重点 | 测试框架 |
|---|---|---|
| `ResiReceivableGenService` | 批量生成防重复、金额计算、异常回滚 | JUnit 5 + Mockito |
| `ResiCashierService.collect()` | 金额核算、事务、并发幂等 | JUnit 5 + Mockito + @Transactional |
| `ResiMeterShareCalculator` | 公摊计算精度（多个数据集验证） | JUnit 5（参数化测试） |
| `ResiOverdueCalcJob` | 滞纳金计算公式、上限截断 | JUnit 5 + Mockito |
| `WxPayNotifyService` | 验签、幂等、金额校验 | JUnit 5 + Mockito |
| `ResiMaskUtils` | 脱敏方法（手机/身份证各多个样本） | JUnit 5 |

**单元测试代码规范**：

```java
// 示例：收银台收款金额计算测试
// 文件：src/test/java/com/zhaoxinms/resi/cashier/ResiCashierServiceTest.java

@SpringBootTest
@Transactional  // 每个测试方法后自动回滚，不污染测试数据库
class ResiCashierServiceTest {

    @Autowired
    private ResiCashierService cashierService;

    // --- 正向测试 ---

    @Test
    @DisplayName("正常收款：应收359.80，实收359.80，无折扣无预收")
    void collect_normal_success() {
        // Given
        ResiCollectReq req = buildNormalCollectReq("359.80", "CASH");
        // When
        CollectResult result = cashierService.collect(req);
        // Then
        assertNotNull(result.getPayNo());
        assertEquals(new BigDecimal("359.80"), result.getPaidAmount());
        assertEquals(new BigDecimal("0.00"), result.getDiscountAmount());
        // 验证 DB 状态
        ResiReceivable recv = receivableMapper.selectById(req.getReceivableIds().get(0));
        assertEquals("2", recv.getPayState());
        assertEquals(new BigDecimal("359.80"), recv.getPaidAmount());
    }

    @Test
    @DisplayName("收款带折扣：9.5折，359.80→341.81，折扣17.99")
    void collect_with_discount_95() {
        ResiCollectReq req = buildCollectReqWithDiscount("359.80", "0.9500");
        CollectResult result = cashierService.collect(req);
        assertEquals(new BigDecimal("341.81"), result.getPaidAmount());
        assertEquals(new BigDecimal("17.99"), result.getDiscountAmount());
    }

    @Test
    @DisplayName("预收款冲抵：余额100，应收300，实收=200")
    void collect_with_prepay_offset() {
        // 先充值预收款100元
        setupPreAccount("100.00", null);
        ResiCollectReq req = buildCollectReqWithPrePay("300.00", "100.00");
        CollectResult result = cashierService.collect(req);
        assertEquals(new BigDecimal("200.00"), result.getPayAmount());
        assertEquals(new BigDecimal("100.00"), result.getPrePayAmount());
        // 验证余额
        ResiPreAccount account = preAccountMapper.selectByResourceId(roomId, null);
        assertEquals(new BigDecimal("0.00"), account.getBalance());
    }

    // --- 异常测试 ---

    @Test
    @DisplayName("并发收款幂等：同一应收不得重复收款")
    void collect_concurrent_idempotent() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    cashierService.collect(buildSameCollectReq());
                    successCount.incrementAndGet();
                } catch (ServiceException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await(10, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "只有1次收款应成功");
        assertEquals(threadCount - 1, failCount.get(), "其余应失败");
    }

    @Test
    @DisplayName("专款专冲：物业费专款不可冲抵水费")
    void collect_earmark_crossFee_rejected() {
        setupEarmarkPreAccount(feeIdWater, "100.00"); // 水费专款
        ResiCollectReq req = buildCollectReqWithPrePay(propertyFeeReceivableId, "100.00");
        ServiceException ex = assertThrows(ServiceException.class,
            () -> cashierService.collect(req));
        assertTrue(ex.getMessage().contains("专款"));
    }
}
```

**参数化测试（公摊计算）**：

```java
@ParameterizedTest(name = "公摊场景{index}：总表={0}，分户合计={1}，期望公摊={2}")
@CsvSource({
    "100, 75, 25",    // 标准场景
    "100, 100, 0",    // 无公摊（分户总和等于总表）
    "100, 90, 10",    // 小公摊
    "100, 0, 100",    // 极端：所有用量都是公摊（分户均为0）
})
void meterShare_calculation(String total, String subTotal, String expectedShare) {
    BigDecimal shareAmount = calculator.calcPublicShare(
        new BigDecimal(total), new BigDecimal(subTotal));
    assertEquals(new BigDecimal(expectedShare), shareAmount);
}
```

### 4.3 接口自动化测试

#### 工具与框架

| 工具 | 用途 | 执行时机 |
|---|---|---|
| **Postman Collections** | 接口用例编写、手工执行 | Sprint 内 QA 测试 |
| **Newman** | Postman 集合命令行执行 | CI/CD 集成，每次 merge 到 develop 触发 |
| **Postman Environment** | 多环境配置（test/staging 切换） | 每套环境一个 env 文件 |

#### 自动化用例集组织

```
resi-api-tests/
├── collections/
│   ├── 00_auth.postman_collection.json          # 登录获取Token（前置）
│   ├── 01_archive.postman_collection.json        # S1 档案层（40个用例）
│   ├── 02_feeconfig.postman_collection.json      # S2 费用配置（30个用例）
│   ├── 03_meter.postman_collection.json          # S3 抄表（25个用例）
│   ├── 04_receivable.postman_collection.json     # S4 应收（20个用例）
│   ├── 05_cashier.postman_collection.json        # S4/S5 收银台（35个用例）
│   ├── 06_report.postman_collection.json         # S5/S7 报表（40个用例）
│   ├── 07_wxmp.postman_collection.json           # S8/S9 C端（30个用例）
│   └── 99_regression.postman_collection.json     # 回归套件（核心链路10条）
├── environments/
│   ├── test.postman_environment.json
│   └── staging.postman_environment.json
└── newman_run.sh                                  # 执行脚本
```

#### Newman CI 集成脚本

```bash
#!/bin/bash
# newman_run.sh — 在 CI/CD 中执行接口自动化测试

COLLECTION_DIR="./collections"
ENV_FILE="./environments/${ENV:-test}.postman_environment.json"
REPORT_DIR="./reports/$(date +%Y%m%d_%H%M%S)"

mkdir -p $REPORT_DIR

# 执行所有 Collection
for collection in $COLLECTION_DIR/*.postman_collection.json; do
  name=$(basename $collection .postman_collection.json)
  echo "执行: $name"
  newman run $collection \
    --environment $ENV_FILE \
    --reporters cli,htmlextra \
    --reporter-htmlextra-export "$REPORT_DIR/${name}.html" \
    --bail                    # 遇到失败立即停止（CI 中快速反馈）
  if [ $? -ne 0 ]; then
    echo "❌ $name 测试失败，停止执行"
    exit 1
  fi
done

echo "✅ 所有接口测试通过，报告保存至 $REPORT_DIR"
```

#### 关键接口 Postman 用例示例

```
用例：TC-CASH-001-01 正常收款

Pre-request Script：
  // 创建测试应收（如不存在）
  pm.sendRequest({url: pm.environment.get('baseUrl') + '/resi/receivable/test-data'}, ...);

Request：
  POST {{baseUrl}}/resi/cashier/collect
  Authorization: Bearer {{token_cashier}}
  Body:
  {
    "projectId": {{projectId}},
    "receivableIds": ["{{receivableId_unpaid}}"],
    "payMethod": "CASH",
    "payAmount": 359.80,
    "note": "自动化测试"
  }

Tests：
  pm.test("HTTP 200", () => pm.response.to.have.status(200));
  pm.test("code=200", () => {
    const json = pm.response.json();
    pm.expect(json.code).to.eql(200);
    pm.expect(json.data.payNo).to.match(/^ZS\d{14}$/);
    pm.expect(json.data.paidAmount).to.eql(359.80);
  });
  pm.test("收据号存入环境变量", () => {
    pm.environment.set("lastPayNo", pm.response.json().data.payNo);
  });
```

### 4.4 回归测试套件

#### 核心回归链路（每 Sprint 末全量执行）

| 链路ID | 链路名称 | 涉及接口数 | 估计执行时间 |
|---|---|---|---|
| REG-01 | 档案录入链路：项目→楼栋→房间→客户→绑定 | 8 | 3分钟 |
| REG-02 | 费用配置链路：定义→分配→票据配置 | 6 | 2分钟 |
| REG-03 | 收费主链路：生成应收→收银台收款→打印收据 | 7 | 3分钟 |
| REG-04 | 抄表链路：录入→公摊→入账→生成应收 | 6 | 3分钟 |
| REG-05 | 财务链路：退款→冲红→预收款→押金 | 8 | 4分钟 |
| REG-06 | 报表准确性链路：收款→核验报表数据一致 | 5 | 2分钟 |
| REG-07 | 权限隔离链路：跨项目/跨账号访问 | 6 | 2分钟 |
| REG-08 | C端链路：登录→查账单→缴费→收据 | 8 | 5分钟 |
| REG-09 | 看板链路：收款→缓存失效→数据更新 | 4 | 2分钟 |
| REG-10 | 定时任务链路：滞纳金计算→欠费通知 | 3 | 2分钟 |

**总计**：61个接口，约28分钟（Newman 自动执行）

#### 回归触发机制

```
触发时机                  执行范围              通过标准
─────────────────────────────────────────────────────
每次 develop 分支 merge   REG-01～07           100% Pass
每 Sprint 末（手动触发）   全部 REG-01～10      100% Pass
生产部署前（冒烟）         REG-01+03+08         100% Pass
```

---

## 5. 性能测试基准指标

### 5.1 性能目标定义

#### 系统规模假设（基准场景）

```
小区规模：1个项目，5栋楼，500个房间
用户规模：同时在线 B端用户 20人（收银员 5人 + 管理层 10人 + 财务 5人）
           C端用户高峰：200人（月初收费高峰期）
数据规模：应收账单 6000条/月（500房间×12费用），历史数据 24个月
```

#### 性能指标目标

| 级别 | 说明 | 指标 |
|---|---|---|
| **响应时间（RT）** | P50（中位数） | 核心接口 < 200ms |
| | P95 | 核心接口 < 500ms |
| | P99 | 核心接口 < 1000ms |
| | 最大 | 所有接口 < 3000ms |
| **吞吐量（TPS）** | 收款接口 50并发 | TPS ≥ 20 |
| | 查询接口 50并发 | TPS ≥ 100 |
| **错误率** | 正常压力下 | < 0.1% |
| | 极限压力下 | < 1% |
| **资源使用** | CPU（压测中） | < 80% |
| | 内存（压测中） | < 85% |
| | DB 连接池 | < 80% 使用率 |

### 5.2 各接口性能基准

#### B端接口基准

| 接口 | 方法 | 并发用户 | P50 目标 | P99 目标 | TPS 目标 | 备注 |
|---|---|---|---|---|---|---|
| `收银台-房间搜索` | GET | 20 | 100ms | 500ms | 50 | idx_room_alias 索引支持 |
| `收银台-查询待缴费用` | GET | 20 | 200ms | 800ms | 30 | 含状态过滤 |
| `收银台-收款` | POST | 50 | 300ms | 1000ms | 20 | **核心接口，SELECT FOR UPDATE** |
| `收银台-退款` | POST | 10 | 300ms | 1000ms | 10 | 低频操作 |
| `应收-批量生成（500房间）` | POST | 1 | — | 20000ms | — | 单次串行，总耗时<20s |
| `应收-批量生成（1000房间）` | POST | 1 | — | 60000ms | — | 单次串行，总耗时<60s |
| `报表-收费率（分页10条）` | GET | 20 | 500ms | 2000ms | 20 | 聚合查询，idx_project_period |
| `报表-欠费明细（5000行）` | GET | 5 | 1000ms | 3000ms | 5 | 强制要求传时间范围参数 |
| `报表-Excel导出（1000行）` | GET | 3 | — | 30000ms | — | 低并发，文件流响应 |
| `看板-总览（缓存命中）` | GET | 20 | 50ms | 200ms | 100 | Redis 缓存 5分钟 |
| `看板-总览（缓存未命中）` | GET | 5 | 500ms | 2000ms | 10 | DB 聚合查询 |
| `档案-房间列表` | GET | 20 | 100ms | 400ms | 80 | 简单分页查询 |
| `费用-批量分配（500房间）` | POST | 1 | — | 5000ms | — | saveBatch 500条 |
| `抄表-批量导入（200行）` | POST | 1 | — | 10000ms | — | Excel解析+写入 |

#### C端接口基准

| 接口 | 方法 | 并发用户 | P50 目标 | P99 目标 | 备注 |
|---|---|---|---|---|---|
| `C端-账单查询` | GET | 100 | 200ms | 800ms | 高峰期月初200并发 |
| `C端-微信缴费-预创建` | POST | 50 | 300ms | 1000ms | 含微信 API 调用 |
| `C端-支付回调` | POST | 20 | 200ms | 500ms | 幂等，Redis 锁 |
| `C端-电子收据` | GET | 50 | 200ms | 500ms | 数据组装 |
| `C端-公告列表` | GET | 100 | 100ms | 400ms | 简单分页 |

#### 定时任务性能基准

| 任务 | 数据规模 | 最大允许耗时 | 执行窗口 |
|---|---|---|---|
| 滞纳金计算（每日01:00） | 500个欠费房间 | 5分钟 | 01:00-01:05 |
| 批量生成应收（月初02:00） | 500房间×5费用=2500条 | 10分钟 | 02:00-02:10 |
| 欠费通知推送（月初10:00） | 100个欠费业主 | 5分钟 | 10:00-10:05 |
| 日报推送（每日18:00） | 10位管理员 | 1分钟 | 18:00-18:01 |

### 5.3 压测场景设计

#### 场景1：收款接口高并发（最高优先级）

```
工具：JMeter 5.x
线程组：50个线程，Ramp-up=10秒，循环=60次
前置：数据库中准备500条未收应收记录（每线程独占1条，避免竞争）
测试时长：约70秒（含 Ramp-up）

JMeter 配置：
  HTTP Request: POST /resi/cashier/collect
  Header: Authorization: Bearer ${token}
  Body: {"receivableIds":["${receivableId}"],"payMethod":"CASH","payAmount":${amount}}

断言：
  Response Code = 200
  JSON Path: $.code = 200

监听器：
  Aggregate Report（TPS/RT/Error Rate）
  Response Time Graph（RT 趋势图）

期望结果：
  TPS ≥ 20
  P99 < 1000ms
  Error Rate < 0.1%
  无数据重复（执行后验证 SELECT COUNT(*) FROM resi_pay_log = 50×60）
```

#### 场景2：月初高峰模拟（混合场景）

```
模拟月初第一天，收银员集中收费的场景

线程组1：10个收银员同时搜索房间
  GET /resi/cashier/room/search（持续5分钟）

线程组2：5个收银员同时查询待缴费用
  GET /resi/cashier/room/{roomId}/receivables（持续5分钟）

线程组3：5个收银员同时收款
  POST /resi/cashier/collect（持续5分钟）

线程组4：2个财务查询报表
  GET /resi/report/transaction-summary（持续5分钟）

线程组5：100个C端业主查询账单（模拟高峰推送后的访问）
  GET /wxmp/payment/bills（持续5分钟）

期望结果：
  各接口 P99 均在目标范围内
  DB CPU < 70%，连接池使用 < 70%
  无 OOM，无超时 500 错误
```

#### 场景3：批量生成应收（关键单接口性能）

```
测试数据准备：1000个房间，每房间分配5种费用
单线程执行：POST /resi/receivable/generate {projectId:1, billPeriod:"2026-05"}

监控指标：
  接口总耗时
  数据库 INSERT 速率（条/秒）
  内存使用（是否有 OOM 风险）

期望：
  500房间：< 20秒
  1000房间：< 60秒
  若超时：改为异步 Job（后台执行+进度轮询），接口立即返回 taskId
```

#### 场景4：看板缓存压测

```
目的：验证 Redis 缓存有效性和收款后的缓存失效

阶段1（缓存预热）：
  1次请求 GET /resi/dashboard/overview，触发 DB 计算并写入 Redis

阶段2（缓存命中压测）：
  20线程×100次 GET /resi/dashboard/overview
  期望：P50 < 50ms，P99 < 200ms，不触发 DB 查询

阶段3（收款触发缓存失效）：
  POST /resi/cashier/collect 1次
  再次 GET /resi/dashboard/overview
  期望：数据已更新（非缓存值），响应时间回到未命中水平

验证方法：比对收款前后看板 "今日实收" 数值是否正确变化
```

#### 场景5：报表大数据量查询

```
数据准备：24个月历史数据，约144000条应收，115000条流水
测试接口：GET /resi/report/collection-rate?projectId=1&startPeriod=2024-06&endPeriod=2026-05

5线程同时请求（模拟多个财务同时查报表）

期望：
  单次响应 < 3秒
  5并发 P99 < 5秒
  返回数据正确（行数与 SQL 直查一致）

若不达标：
  强制要求传时间范围（最大查询跨度12个月）
  超过1万行数据禁止分页展示，强制要求导出
```

### 5.4 性能测试执行计划

#### 阶段一：单接口基准测试（Sprint 5 末，M3 前）

```
目标：验证 P0 核心接口达标
范围：收款/退款/应收生成/收银台查询
工具：JMeter（BE-A/BE-B 配合）
时间：M3 前1天（2026-08-06）
输出：单接口性能基准报告
```

#### 阶段二：集成性能测试（Sprint 7 末，M4 前）

```
目标：验证混合场景下系统整体性能
范围：月初高峰混合场景（场景2）
工具：JMeter 分布式（若需要）
时间：2026-09-02
输出：混合场景性能报告 + 调优建议
```

#### 阶段三：全量压测（Sprint 10，上线前）

```
目标：上线前最终性能验证 + 回归
范围：全部5个场景
环境：Staging 环境（与生产规格相同）
时间：2026-10-06（上线前3天）
输出：完整性能测试报告（含调优前后对比）
通过标准：所有指标达标，无 P0 性能缺陷
```

#### 性能问题处理预案

| 问题 | 处理方案 | 决策时限 |
|---|---|---|
| 收款接口 P99 > 1s | 分析慢查询日志；检查索引；考虑连接池调优 | 发现后24小时内 |
| 批量生成超时 | 改为异步任务（进度轮询），接口立即返回 | Sprint 4 若发现 |
| 报表查询超时 | 强制时间范围参数；增加索引；考虑预聚合 | 发现后48小时内 |
| 内存溢出（OOM） | 检查大对象；分批处理；调整 JVM 堆参数 | 发现即阻塞上线 |
| 连接池耗尽 | 调整 Druid 最大连接数；检查连接泄漏 | 发现即处理 |

---

## 6. 缺陷管理规范

### 6.1 缺陷严重级别定义

| 级别 | 定义 | 示例 | SLA |
|---|---|---|---|
| **P0（阻塞）** | 系统无法使用 / 数据错误 / 安全漏洞 | 收款后账目不平；权限穿透；系统500崩溃 | 当日修复并验证 |
| **P1（严重）** | 核心功能不可用 / 业务流程中断 | 批量生成失败；收银台无法打开；报表数据错误 | 当 Sprint 内修复 |
| **P2（一般）** | 功能异常但有绕过方案 / 非核心功能 | 某筛选条件无效；导出格式问题；非关键字段错误 | 下 Sprint 修复 |
| **P3（轻微）** | UI/UX 问题 / 文案错误 | 按钮位置不对；文字描述有误；样式偏差 | 统一收集后处理 |

### 6.2 缺陷提交规范

```markdown
# 缺陷报告模板

**缺陷编号**：BUG-2026-XXXX
**发现人**：QA
**发现日期**：2026-XX-XX
**严重级别**：P0/P1/P2/P3
**所属模块**：收银台 / 应收管理 / 报表 / ...
**Sprint**：S4

**环境**：测试环境（http://test-server:8080）
**测试账号**：test_cashier_1

**标题**：[收银台] 预收款冲抵后余额未更新，存在账目不平风险

**复现步骤**：
  1. 前置：101室预收款余额=100元
  2. 收银台选择101室物业费300元
  3. 勾选"冲抵预收款"100元
  4. 点击收款，实收200元，支付方式：现金
  5. 查看101室预收款余额

**期望结果**：预收款余额=0元（100元被冲抵）
**实际结果**：预收款余额仍显示100元（未扣减）

**附件**：接口请求截图、DB 数据截图

**影响范围**：所有使用预收款冲抵的收款场景均受影响
**关联用例**：TC-CASH-001-05
```

### 6.3 缺陷跟踪流程

```
发现 → 提交（QA）→ 确认（BE/FE）→ 修复（BE/FE）→ 验证（QA）→ 关闭（QA）

P0：发现→立即通知开发（微信）→2小时内修复→QA 验证→同日关闭
P1：发现→提交缺陷单→当天开始修复→Sprint 内关闭
P2/P3：发现→提交缺陷单→排期处理
```

---

## 7. 各 Sprint 测试计划

| Sprint | 测试时间 | QA 工作重点 | 用例数（估计） | 输出物 |
|---|---|---|---|---|
| **S0** | W01-W02 | 环境搭建、用例模板、测试数据脚本 | — | 测试环境就绪报告、用例模板 |
| **S1** | W03-W04 | 档案层功能测试（含权限隔离、批量导入） | 60 | S1 测试报告 |
| **S2** | W05-W06 | 费用配置测试（含公式验证、批量分配） | 40 | S2 测试报告 |
| **S3** | W07-W08 | 抄表测试（含公摊精度验证、导入流程） | 45 | S3 测试报告 |
| **S4** | W09-W10 | 应收生成、收银台核心（并发测试重点） | 55 | S4 测试报告 + 并发测试报告 |
| **S5** | W11-W12 | 收银台完整、5张核心报表（数据准确性） | 60 | S5 测试报告、P0 里程碑验收报告 |
| **S6** | W13-W14 | 管理增强（过户事务、调账、发票） | 40 | S6 测试报告 |
| **S7** | W15-W16 | 完整报表（20张）、看板、定时任务 | 80 | S7 测试报告、P1 里程碑验收报告 |
| **S8** | W17-W18 | C端登录绑定、账单查询、安全测试 | 35 | S8 测试报告 |
| **S9** | W19-W20 | 微信缴费（沙箱）、全功能、UAT 协助 | 50 | S9 测试报告、UAT 报告、P2 验收报告 |
| **S10** | W21 | 性能全量压测、冒烟、生产验证 | — | 性能测试报告、上线冒烟报告 |

**总计用例数**：约 465 个功能用例 + 性能场景 5 套 + 自动化用例 61 条

---

## 8. 测试交付物清单

| 交付物 | 格式 | 交付时机 | 负责人 |
|---|---|---|---|
| 测试计划文档（本文件） | Markdown | S0 结束前 | QA |
| 测试用例集 | Excel / Postman Collection | 各 Sprint 开始前 | QA |
| Sprint 测试报告 | Markdown | 各 Sprint 结束 | QA |
| 缺陷清单 | 缺陷管理工具导出 | 各 Sprint 结束 | QA |
| 接口自动化测试集 | Postman Collection + Newman 脚本 | S5 末完成主体 | QA |
| 单元测试覆盖率报告 | JaCoCo HTML 报告 | 各 Sprint 结束 | BE-A/BE-B |
| 并发安全测试报告 | JMeter + 分析文档 | S4/S10 | QA + BE |
| P0 里程碑验收报告 | Markdown | M3（2026-08-07） | QA + PM |
| P1 里程碑验收报告 | Markdown | M4（2026-08-28） | QA + PM |
| UAT 报告 | 模板填写 | M5（2026-09-25） | PM + QA |
| 性能测试报告 | JMeter HTML + 分析文档 | S10（2026-10-06） | QA + BE |
| 上线冒烟报告 | Checklist | M6（2026-10-09） | QA |
| 操作手册（测试辅助） | PDF | S10 | PM + QA |

---

*本测试计划随 Sprint 推进动态更新。测试用例在各 Sprint 开始前补充完善，测试结果在 Sprint 结束后归档。*  
*如需求变更导致测试范围变化，须由 PM 审批后更新本文档并通知 QA。*  
*最后更新：2026-05-10*
