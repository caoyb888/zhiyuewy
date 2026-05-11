# SPRINT PLAN
## 肇新智慧物业 · 住宅物业收费模块

**文档版本**：V1.0  
**编制日期**：2026-05-10  
**项目经理**：待定  
**计划周期**：2026-05-18 ～ 2026-10-09（共 21 周）  
**Sprint 节奏**：2 周 / Sprint，共 10 个 Sprint + 1 个上线冲刺  
**团队规模**：后端 × 2、前端 × 2、测试 × 1、产品/PM × 1

---

## 目录

1. [团队角色与职责](#1-团队角色与职责)
2. [整体甘特图](#2-整体甘特图)
3. [Sprint 总览](#3-sprint-总览)
4. [Sprint 详细计划](#4-sprint-详细计划)
   - [Sprint 0 — 基础建设（第 1-2 周）](#sprint-0--基础建设第-1-2-周)
   - [Sprint 1 — 基础档案（第 3-4 周）](#sprint-1--基础档案第-3-4-周)
   - [Sprint 2 — 费用配置（第 5-6 周）](#sprint-2--费用配置第-5-6-周)
   - [Sprint 3 — 抄表管理（第 7-8 周）](#sprint-3--抄表管理第-7-8-周)
   - [Sprint 4 — 应收生成与收银台核心（第 9-10 周）](#sprint-4--应收生成与收银台核心第-9-10-周)
   - [Sprint 5 — 收银台完整功能与核心报表（第 11-12 周）](#sprint-5--收银台完整功能与核心报表第-11-12-周)
   - [Sprint 6 — 管理增强（第 13-14 周）](#sprint-6--管理增强第-13-14-周)
   - [Sprint 7 — 完整报表与看板（第 15-16 周）](#sprint-7--完整报表与看板第-15-16-周)
   - [Sprint 8 — C端基础（第 17-18 周）](#sprint-8--c端基础第-17-18-周)
   - [Sprint 9 — C端完整功能（第 19-20 周）](#sprint-9--c端完整功能第-19-20-周)
   - [Sprint 10 — 上线冲刺（第 21 周）](#sprint-10--上线冲刺第-21-周)
5. [风险登记册](#5-风险登记册)
6. [定义完成（DoD）](#6-定义完成dod)
7. [里程碑节点](#7-里程碑节点)

---

## 1. 团队角色与职责

| 角色 | 标识 | 主要职责 | 参与阶段 |
|---|---|---|---|
| 后端开发 A | **BE-A** | 档案、费用配置、应收生成、定时任务 | S0 ～ S10 |
| 后端开发 B | **BE-B** | 收银台、财务台账、报表、C端后端 | S0 ～ S10 |
| 前端开发 A | **FE-A** | 档案、费用配置、抄表、应收、收银台页面 | S1 ～ S10 |
| 前端开发 B | **FE-B** | 报表、看板、C端小程序、打印 | S3 ～ S10 |
| 测试工程师 | **QA** | 用例编写、功能测试、回归、UAT 协助 | S1 ～ S10 |
| 产品/项目经理 | **PM** | 需求确认、验收、风险跟踪、上线协调 | 全程 |

> **说明**：Sprint 0 为纯技术准备，FE-A/FE-B 可同步进行技术选型与组件调研，QA 完成测试环境搭建和用例模板准备。

---

## 2. 整体甘特图

```
周次  │ 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21
日期  │ 5/18          6/15          7/13          8/10          9/7   10/9
──────┼──────────────────────────────────────────────────────────────────
      │[──S0──][─────────S1─────────][─────S2──────][─────S3──────]
      │        档案                  费用配置        抄表管理
      │
P0    │                              [────────────S4────────────]
阶段  │                               应收生成 + 收银台核心
      │
      │                                            [────────────S5────]
      │                                             收银台完整+核心报表
──────┼──────────────────────────────────────────────────────────────────
P1    │                                                    [───S6────]
阶段  │                                                     管理增强
      │
      │                                                          [──S7──]
      │                                                           报表+看板
──────┼──────────────────────────────────────────────────────────────────
P2    │                                                               [──S8──]
阶段  │                                                                C端基础
      │
      │                                                                    [──S9──]
      │                                                                     C端完整
──────┼──────────────────────────────────────────────────────────────────
上线  │                                                                         [S10]
      │                                                                          上线冲刺
──────┼──────────────────────────────────────────────────────────────────
里程  │  ▲        ▲              ▲              ▲         ▲        ▲    ▲
碑    │ M0       M1             M2             M3        M4       M5   M6
```

### 关键里程碑

| 编号 | 日期 | 里程碑内容 |
|---|---|---|
| **M0** | 2026-05-22 | 开发环境就绪，DB 初始化完成，CI/CD 搭建完毕 |
| **M1** | 2026-06-12 | P0 档案层完成，可录入项目/楼栋/房间/客户数据 |
| **M2** | 2026-07-10 | P0 核心链路完成，可完成一次完整收款流程（内部演示） |
| **M3** | 2026-08-07 | P0 全部完成，核心报表可用，提交内部 UAT |
| **M4** | 2026-08-28 | P1 全部完成，看板+完整报表+管理增强功能就绪 |
| **M5** | 2026-09-25 | P2 全部完成，C端微信缴费可用，提交用户 UAT |
| **M6** | 2026-10-09 | 正式上线，生产环境部署完成 |

---

## 3. Sprint 总览

| Sprint | 周次 | 日期区间 | 主题 | 阶段 | 交付目标 |
|---|---|---|---|---|---|
| **S0** | W01-W02 | 05/18 ～ 05/29 | 基础建设 | 准备 | 环境、DB、CI/CD、公共组件 |
| **S1** | W03-W04 | 06/01 ～ 06/12 | 基础档案 | P0 | 项目/楼栋/房间/客户/仪表 CRUD |
| **S2** | W05-W06 | 06/15 ～ 06/26 | 费用配置 | P0 | 费用定义/分配/票据配置/折扣 |
| **S3** | W07-W08 | 06/29 ～ 07/10 | 抄表管理 | P0 | 抄表录入/导入/公摊/入账 |
| **S4** | W09-W10 | 07/13 ～ 07/24 | 应收+收银台核心 | P0 | 批量生成应收、收款核心流程 |
| **S5** | W11-W12 | 07/27 ～ 08/07 | 收银台完整+核心报表 | P0 | 退款/冲红/预收/押金/5张核心报表 |
| **S6** | W13-W14 | 08/10 ～ 08/21 | 管理增强 | P1 | 过户/调账/折扣/发票/车位 |
| **S7** | W15-W16 | 08/24 ～ 09/04 | 报表+看板 | P1 | 20+报表全量、看板+推送 |
| **S8** | W17-W18 | 09/07 ～ 09/18 | C端基础 | P2 | wxmp 模块、登录绑定、账单查询 |
| **S9** | W19-W20 | 09/21 ～ 10/02 | C端完整 | P2 | 微信缴费、通知公告、管家便民 |
| **S10** | W21 | 10/05 ～ 10/09 | 上线冲刺 | 上线 | 性能测试、生产部署、培训 |

---

## 4. Sprint 详细计划

---

### Sprint 0 — 基础建设（第 1-2 周）

**周期**：2026-05-18 ～ 2026-05-29  
**目标**：搭建开发基础设施，完成 DB 初始化，确保所有成员可开始编码  
**Sprint Goal**：开发环境 Ready，第一行业务代码可以运行

#### Story 列表

| Story ID | Story 标题 | 负责人 | 故事点 | 优先级 |
|---|---|---|---|---|
| S0-01 | 开发/测试环境搭建 | BE-A | 3 | 🔴 P0 |
| S0-02 | 数据库初始化（28张resi_表DDL执行） | BE-A | 2 | 🔴 P0 |
| S0-03 | 初始化数据（字典/菜单/流水规则/定时任务） | BE-A | 2 | 🔴 P0 |
| S0-04 | CI/CD 流水线配置 | BE-B | 3 | 🔴 P0 |
| S0-05 | 后端公共基础类搭建（包结构/常量/异常） | BE-B | 3 | 🔴 P0 |
| S0-06 | 前端工程初始化（路由/菜单/api目录结构） | FE-A | 2 | 🔴 P0 |
| S0-07 | 测试环境准备（用例模板/测试账号/测试数据脚本） | QA | 2 | 🟡 P1 |

**总故事点**：17 点

#### 任务分解

**S0-01 开发/测试环境搭建**（BE-A 主导）
- [ ] Docker Compose 配置新增 wxmp 服务（端口 8081）
- [ ] Nginx 配置新增 `/resi/` 和 `/wxmp/` 路由转发
- [ ] Redis 命名空间配置（`resi:` 前缀隔离）
- [ ] 开发人员本地环境文档更新

**S0-02 数据库初始化**（BE-A）
- [ ] 执行 `DATABASE_DESIGN.md` 中全部 28 张 `resi_` 表 DDL
- [ ] 验证所有表字段、索引、唯一键创建正确
- [ ] 测试环境与开发环境分别执行并确认

**S0-03 初始化数据**（BE-A）
- [ ] `base_billrule` 插入住宅收据流水规则（`RESI_RECEIPT`）
- [ ] `sys_dict_type/data` 插入 6 组数据字典
- [ ] `sys_job` 插入 6 条定时任务记录
- [ ] `sys_menu` 新增"住宅收费"菜单树（含权限标识）

**S0-04 CI/CD 流水线配置**（BE-B）
- [ ] 配置 `zhaoxinwy-pms` 模块增量编译触发条件
- [ ] 配置 `zhaoxinwy-wxmp` 新模块构建流程
- [ ] 配置自动部署到测试环境
- [ ] 配置代码检查（CLAUDE.md 规范检查项）

**S0-05 后端公共基础类**（BE-B）
- [ ] 创建包结构 `com.zhaoxinms.resi.*`（所有子包目录）
- [ ] `ResiConstants.java`（枚举值常量，对应 DATABASE_DESIGN 第5章）
- [ ] `ResiProjectScopeAspect.java`（项目数据隔离 AOP 骨架）
- [ ] `ResiBaseEntity.java`（档案类公共字段）、`ResiFlowBaseEntity.java`（流水类公共字段）
- [ ] `zhaoxinwy-wxmp` 模块创建（Maven pom.xml、主启动类、配置文件骨架）

**S0-06 前端工程初始化**（FE-A）
- [ ] `src/views/resi/` 目录结构创建
- [ ] `src/api/resi/` 目录及空白 API 文件创建（archive.js / feeconfig.js / cashier.js 等）
- [ ] 路由配置新增住宅收费顶级路由
- [ ] 公共工具函数 `src/utils/resi.js`（金额格式化 formatMoney、枚举转文本工具）

**S0-07 测试准备**（QA）
- [ ] 测试账号创建（管理员/收银员/财务角色各一个）
- [ ] 测试用例模板制定（含验收标准检查项）
- [ ] 测试数据导入脚本（10个项目/50栋楼/500个房间的基础数据）

#### 验收标准

- [ ] 访问 `http://test-server:8080`，能看到"住宅收费"菜单（权限已分配）
- [ ] 数据库中 28 张 `resi_` 表全部存在，`SHOW CREATE TABLE resi_room` 字段与 DDL 一致
- [ ] `base_billrule` 中存在 `en_code='RESI_RECEIPT'` 的记录
- [ ] CI/CD：提交代码到 develop 分支，5 分钟内自动部署到测试环境
- [ ] 访问 `http://test-server:8081/actuator/health` 返回 `{"status":"UP"}`（wxmp 服务启动）
- [ ] `src/views/resi/` 目录结构与 CLAUDE.md 第3.4节一致

---

### Sprint 1 — 基础档案（第 3-4 周）

**周期**：2026-06-01 ～ 2026-06-12  
**目标**：完成住宅物业的完整档案体系，支持录入项目、楼栋、房间、客户、仪表  
**Sprint Goal**：可以通过 B 端录入一个完整的小区档案，并将业主绑定到房间

#### Story 列表

| Story ID | Story 标题 | 负责人 | 故事点 | 优先级 |
|---|---|---|---|---|
| S1-01 | 项目（小区）管理 CRUD | BE-A + FE-A | 3 | 🔴 P0 |
| S1-02 | 楼栋管理 CRUD | BE-A + FE-A | 2 | 🔴 P0 |
| S1-03 | 房间管理 CRUD + Excel 批量导入 | BE-A + FE-A | 5 | 🔴 P0 |
| S1-04 | 客户（业主）管理 CRUD | BE-A + FE-A | 3 | 🔴 P0 |
| S1-05 | 客户资产绑定（客户绑定房间/车位） | BE-A + FE-A | 3 | 🔴 P0 |
| S1-06 | 仪表档案管理 CRUD | BE-A + FE-A | 3 | 🟡 P1 |
| S1-07 | 项目权限隔离 AOP 实现 | BE-A | 3 | 🔴 P0 |
| S1-08 | 档案层测试用例 + 执行 | QA | 3 | 🔴 P0 |

**总故事点**：25 点

#### 任务分解

**S1-01 项目管理**
- [ ] BE-A：`ResiProjectController` 实现（list/get/add/edit/remove）
- [ ] BE-A：`ResiProjectService` + `ResiProjectMapper` + `resi_project` XML
- [ ] BE-A：接口路径 `/resi/archive/project`，继承 `BaseController`，加 `@Log` 注解
- [ ] FE-A：`views/resi/archive/project/index.vue`（列表 + 弹窗表单）
- [ ] FE-A：`api/resi/archive.js` 中封装项目相关接口
- [ ] 联调验证：增删改查正常，`sys_oper_log` 有操作记录

**S1-02 楼栋管理**
- [ ] BE-A：楼栋 CRUD，接口 `/resi/archive/building`，含 treeselect（供房间表单选择）
- [ ] FE-A：楼栋管理页，支持按项目过滤展示

**S1-03 房间管理（含批量导入）**
- [ ] BE-A：房间 CRUD 接口 `/resi/archive/room`
- [ ] BE-A：房间批量导入（参考 `PaymentMeterImportController` 三步模式）
  - `GET /resi/archive/room/import/template`（下载模板）
  - `POST /resi/archive/room/import/upload`（上传 Excel，POI 解析，返回预览+错误行）
  - `POST /resi/archive/room/import/confirm`（确认导入）
- [ ] BE-A：`room_alias` 自动生成规则（`{楼栋名}{单元号}{房号}`，可手动覆盖）
- [ ] BE-A：全文检索接口 `GET /resi/archive/room/search?keyword=`（供收银台使用）
- [ ] FE-A：房间列表页（含楼栋单元筛选、状态标签）
- [ ] FE-A：导入向导组件（上传→预览错误行→确认）

**S1-04 客户管理**
- [ ] BE-A：客户 CRUD，接口 `/resi/archive/customer`
- [ ] BE-A：`id_card` 字段 AES-256 加密存储，返回时脱敏（`110***1234`）
- [ ] BE-A：`phone` 展示时脱敏（`138****8888`），接口返回明文但前端展示脱敏
- [ ] FE-A：客户管理页，展示脱敏手机号和身份证

**S1-05 客户资产绑定**
- [ ] BE-A：`POST /resi/archive/customer/bind-asset`（绑定）、`DELETE` 解绑
- [ ] BE-A：`GET /resi/archive/customer/{id}/assets`（查客户所有资产）
- [ ] FE-A：客户详情页内嵌资产绑定列表，支持新增绑定/解绑

**S1-06 仪表档案**
- [ ] BE-A：仪表 CRUD，接口 `/resi/archive/meter-device`
- [ ] BE-A：仪表与房间关联，`is_public=1` 时 `room_id` 可为空
- [ ] FE-A：仪表管理页，按房间或公摊组筛选

**S1-07 项目权限隔离**
- [ ] BE-A：`@ResiProjectScope` 注解定义
- [ ] BE-A：`ResiProjectScopeAspect`：拦截带注解方法，自动追加 `project_id IN (?)` 到 QueryWrapper
- [ ] BE-A：`ResiProjectPermissionService`：查询当前用户可访问项目列表（支持超管=全部）
- [ ] BE-A：所有档案类 list 接口加 `@ResiProjectScope`
- [ ] 测试：账号 A 只有项目1权限，不能看到项目2的数据

**S1-08 测试**（QA）
- [ ] 编写档案层测试用例（参数边界/必填校验/唯一约束/权限隔离）
- [ ] 执行功能测试，记录缺陷
- [ ] 验证 Excel 导入：正常数据/含错误行/空文件/超大文件（>1000行）

#### 验收标准

- [ ] 可以创建项目→楼栋→房间（手工+批量导入），三者层级关系正确
- [ ] Excel 导入 500 行房间数据，耗时 < 10 秒，错误行有明确提示（行号+原因）
- [ ] 客户身份证在数据库中为密文，前端展示为脱敏格式
- [ ] 账号 B 无权访问账号 A 创建的项目数据（权限隔离验证）
- [ ] 所有 CRUD 操作在 `sys_oper_log` 有完整记录
- [ ] Swagger 文档可访问，接口路径符合 `/resi/archive/` 规范
- [ ] QA 提交缺陷 < 5 个 P1 以上问题

---

### Sprint 2 — 费用配置（第 5-6 周）

**周期**：2026-06-15 ～ 2026-06-26  
**目标**：完成费用定义、分配、票据配置；业务人员可以配置物业费、水电费等收费规则  
**Sprint Goal**：物业费和水费的收费规则配置完成，可为500个房间完成费用分配

#### Story 列表

| Story ID | Story 标题 | 负责人 | 故事点 | 优先级 |
|---|---|---|---|---|
| S2-01 | 费用定义 CRUD（含公式引擎对接） | BE-A + FE-A | 5 | 🔴 P0 |
| S2-02 | 费用分配 CRUD + 批量分配 | BE-A + FE-A | 5 | 🔴 P0 |
| S2-03 | 票据模板配置 | BE-B + FE-A | 3 | 🔴 P0 |
| S2-04 | 折扣配置管理 | BE-B + FE-A | 2 | 🟡 P1 |
| S2-05 | 费用配置层测试 | QA | 3 | 🔴 P0 |

**总故事点**：18 点

#### 任务分解

**S2-01 费用定义**
- [ ] BE-A：`ResiFeeDefinitionController` CRUD，路径 `/resi/feeconfig/definition`
- [ ] BE-A：表单校验（`fee_type=PERIOD` 时 `cycle_unit` 必填；`calc_type=FORMULA` 时 `formula` 必填）
- [ ] BE-A：公式引擎对接——注入现有 `FormulaCalculator` Bean，提供公式预览接口
  - `POST /resi/feeconfig/definition/preview-formula`（传入公式文本+变量值，返回计算结果）
- [ ] BE-A：`GET /resi/feeconfig/definition/select`（下拉选项接口，供分配/应收页使用）
- [ ] FE-A：费用定义列表页（含费用类型/计费方式 Tag 展示）
- [ ] FE-A：费用定义表单（联动显示：选 FORMULA 时展示公式编辑区，选 PERIOD 时显示周期配置）
- [ ] FE-A：公式预览组件（输入变量值，实时展示计算结果）

**S2-02 费用分配**
- [ ] BE-A：单条分配 CRUD，路径 `/resi/feeconfig/allocation`
- [ ] BE-A：批量分配接口 `POST /resi/feeconfig/allocation/batch`
  - 支持三种批量方式：按楼栋、按单元、全项目
  - MyBatis-Plus `saveBatch` 500条/批，返回{总数/成功/跳过（已存在）}
  - 唯一键冲突（`uk_alloc`）时跳过，不报错
- [ ] FE-A：费用分配列表页（支持按费用/楼栋/房间筛选）
- [ ] FE-A：批量分配向导（选费用→选范围→预览待分配数量→确认执行→结果统计）

**S2-03 票据配置**
- [ ] BE-B：票据配置 CRUD，路径 `/resi/feeconfig/ticket`
- [ ] BE-B：`field_config` JSON 字段的读写（字段列表+排序，支持拖拽排序后保存）
- [ ] FE-A：票据配置页（Logo/公章图片上传，字段列表拖拽排序，纸张规格选择）
- [ ] FE-A：收据预览组件（按 field_config 渲染票据预览效果）

**S2-04 折扣配置**
- [ ] BE-B：折扣 CRUD，路径 `/resi/feeconfig/discount`
- [ ] BE-B：有效期验证（`valid_end >= valid_start`）
- [ ] FE-A：折扣管理页（含有效期状态展示：未开始/有效/已过期）

**S2-05 测试**（QA）
- [ ] 费用定义：各计费方式（FIXED/AREA/USAGE/FORMULA）创建并验证公式
- [ ] 费用分配：批量分配500房间，验证唯一键保护（重复分配不报错）
- [ ] 边界测试：`overdue_rate` 最大精度（6位小数）、`formula` 梯度公式验证

#### 验收标准

- [ ] 可创建5种类型的费用定义（物业费固定/水费用量/电费梯度公式/停车费月收/装修押金）
- [ ] 梯度电价公式（三段阶梯）预览计算结果正确（230度以内/231-400度/400度以上）
- [ ] 批量分配 500 个房间，耗时 < 5 秒，跳过已存在分配时不报错
- [ ] 票据预览与打印字段顺序和 `field_config` 配置一致
- [ ] 折扣配置有效期范围检验（结束日期不能早于开始日期）

---

### Sprint 3 — 抄表管理（第 7-8 周）

**周期**：2026-06-29 ～ 2026-07-10  
**目标**：完成抄表的录入、Excel 导入、公摊计算、异常预警和入账流程  
**Sprint Goal**：完成一次完整的月度抄表流程（录入→预警→公摊计算→批量入账→生成应收）

#### Story 列表

| Story ID | Story 标题 | 负责人 | 故事点 | 优先级 |
|---|---|---|---|---|
| S3-01 | 抄表记录 CRUD + 单户手动录入 | BE-A + FE-A | 3 | 🔴 P0 |
| S3-02 | 抄表 Excel 批量导入（含预警检测） | BE-A + FE-B | 5 | 🔴 P0 |
| S3-03 | 公摊量计算逻辑 | BE-A | 5 | 🔴 P0 |
| S3-04 | 抄表单户/批量入账 | BE-A + FE-A | 3 | 🔴 P0 |
| S3-05 | 抄表页面完整交互（列表/状态流转/预警高亮） | FE-B | 3 | 🟡 P1 |
| S3-06 | 抄表模块测试 | QA | 3 | 🔴 P0 |

**总故事点**：22 点

#### 任务分解

**S3-01 抄表 CRUD**
- [ ] BE-A：`ResiMeterReadingController`，路径 `/resi/meter/reading`
- [ ] BE-A：手动新增时，自动从 `payment_meter_index` 或上期记录带入 `last_reading`
- [ ] BE-A：用量自动计算：`raw_usage = (curr_reading - last_reading) × multiplier`
- [ ] BE-A：唯一键保护（同仪表同期间不可重复）
- [ ] FE-A：抄表列表页（按项目/期间/状态筛选）

**S3-02 Excel 批量导入（三步模式）**
- [ ] BE-A：`GET /resi/meter/reading/import/template`（生成含仪表编号的模板文件）
- [ ] BE-A：`POST /resi/meter/reading/import/upload`
  - POI 解析 Excel（参考现有 `PaymentMeterImportController`）
  - 按 `meter_code` 匹配仪表，查询 `last_reading`
  - 预警检测：`curr_reading < last_reading`（读数回退）或用量超阈值
  - 返回：正常行数/警告行数/错误行数，错误行含行号和原因
  - 上传数据暂存 Redis（key: `resi:meter:import:{batchId}`，TTL 30分钟）
- [ ] BE-A：`POST /resi/meter/reading/import/confirm`（从 Redis 取出写入DB）
- [ ] FE-B：导入向导页（拖拽上传→预览表格含警告/错误行高亮→确认或取消）

**S3-03 公摊计算**
- [ ] BE-A：公摊计算 Service `ResiMeterShareCalculator`
  - 按 `public_group` 分组
  - 计算：公摊量 = 总表用量 - ∑分户表用量
  - 按面积比例分摊到各分户：`share_amount = 公摊量 × (本户面积 / 组内总面积)`
  - 写入各分户 `resi_meter_reading.share_amount`
  - `billed_usage = raw_usage - loss_amount + share_amount`
- [ ] BE-A：`POST /resi/meter/reading/calc-share`（触发指定期间/分组的公摊计算）
- [ ] BE-A：公摊计算结果审核接口（展示公摊前后用量对比）
- [ ] FE-B（本Sprint）/ FE-A：公摊计算触发按钮 + 结果展示（下一 Sprint 完善UI）

**S3-04 单户/批量入账**
- [ ] BE-A：`POST /resi/meter/reading/bill/{id}`（单户入账）
  - 调用 `ResiReceivableService.createFromMeterReading()`
  - 更新 `resi_meter_reading.status=BILLED`，写入 `receivable_id`
- [ ] BE-A：`POST /resi/meter/reading/bill/batch`（按期间批量入账）
  - 仅处理 status=INPUT 的记录
  - saveBatch 500条，返回入账统计
- [ ] FE-A：列表中"入账"操作按钮（单条/全选批量）

**S3-05 前端完整页面**（FE-B）
- [ ] 抄表列表：状态 Tag 颜色（INPUT 灰/BILLED 绿/VERIFIED 蓝）
- [ ] 预警行用黄色背景或 ⚠️ 图标标注
- [ ] 公摊详情抽屉（展示该表所在公摊组、各分户分摊量）

**S3-06 测试**（QA）
- [ ] 读数回退场景：预警正确触发，可选择继续导入（警告）
- [ ] 公摊计算验证：总表100度，分户A用40度（60㎡）、B用30度（40㎡），公摊量30度，A分摊18度，B分摊12度
- [ ] 批量入账幂等：同期间二次触发批量入账，不产生重复应收记录

#### 验收标准

- [ ] Excel 导入 200 行抄表数据，包含 10 行异常（回退/超阈值），异常行以红色高亮展示，正常行可正常导入
- [ ] 公摊计算结果：100度总表 - 70度分户 = 30度公摊，按面积比 6:4 分摊，数值误差 < 0.01度
- [ ] 单户入账后：`resi_meter_reading.status=BILLED`，`resi_receivable` 中有对应记录
- [ ] 批量入账 200 条，耗时 < 15 秒
- [ ] 二次批量入账无重复应收产生（唯一键保护）

---

### Sprint 4 — 应收生成与收银台核心（第 9-10 周）

**周期**：2026-07-13 ～ 2026-07-24  
**目标**：实现周期费批量生成和收银台核心收款流程（最高优先级功能）  
**Sprint Goal**：完成一次从"批量生成物业费"到"收款打印收据"的完整业务流程

#### Story 列表

| Story ID | Story 标题 | 负责人 | 故事点 | 优先级 |
|---|---|---|---|---|
| S4-01 | 周期费批量生成应收 | BE-A + FE-A | 5 | 🔴 P0 |
| S4-02 | 临时费手动录入 | BE-A + FE-A | 2 | 🔴 P0 |
| S4-03 | 收银台：房间搜索与费用展示 | BE-B + FE-A | 5 | 🔴 P0 |
| S4-04 | 收银台：核心收款（事务/并发/票据号） | BE-B | 8 | 🔴 P0 |
| S4-05 | 收款流水打印（收款单数据接口） | BE-B + FE-B | 3 | 🔴 P0 |
| S4-06 | 应收生成与收银台测试 | QA | 5 | 🔴 P0 |

**总故事点**：28 点

#### 任务分解

**S4-01 批量生成应收**
- [ ] BE-A：`ResiReceivableGenService.batchGenerate(projectId, billPeriod)`
  - 查询有效费用分配（`start_date <= today AND (end_date IS NULL OR end_date >= today)`）
  - 按 `calc_type` 调用计算器（FIXED/AREA/USAGE/FORMULA）
  - 计算滞纳金（查询上期欠费，按 `overdue_rate` 和逾期天数计算）
  - `saveBatch` 500条，`gen_batch = "GEN-{projectId}-{period}"`
  - 防重复：同 `gen_batch` 已存在则跳过（不报错），返回跳过数
- [ ] BE-A：`POST /resi/receivable/generate`、`DELETE /resi/receivable/generate/{genBatch}`（按批次删除重生成）
- [ ] BE-A：`POST /resi/receivable/create-temp`（临时费单条录入）
- [ ] FE-A：应收管理页（按项目/账单月/费用/状态筛选，支持导出）
- [ ] FE-A：批量生成：选月份→预览待生成数量→执行→展示结果统计

**S4-02 临时费**
- [ ] BE-A：手动录入单条临时费（`fee_type=TEMP`，`bill_period=NULL`）
- [ ] FE-A：临时费录入弹窗（选房间/费用/数量/单价/备注）

**S4-03 收银台查询**
- [ ] BE-B：`GET /resi/cashier/room/search?keyword=&projectId=`（模糊匹配 `room_alias/customer_name/room_no`）
- [ ] BE-B：`GET /resi/cashier/room/{roomId}/receivables`（待缴费用，支持费用/年份/账单月/类型筛选）
- [ ] BE-B：`GET /resi/cashier/room/{roomId}/summary`（应收/已缴/欠费汇总卡片）
- [ ] FE-A：收银台三区布局（左侧搜索树/中间费用列表/右侧收款操作区）
  - 左侧：搜索框 + el-tree 懒加载（项目→楼栋→房间）
  - 中间：费用列表（勾选式 el-table，含筛选条件）
  - 右侧：已选费用清单 + 金额汇总（本Sprint右侧暂为静态）

**S4-04 收银台核心收款**（BE-B，最复杂任务）
- [ ] `POST /resi/cashier/calc`（收款预览，仅计算不写库）：输入receivableIds+discountId，返回各金额汇总
- [ ] `POST /resi/cashier/collect`（收款核心，需事务+并发保护）
  - `SELECT FOR UPDATE` 锁定应收记录
  - 调用 `BillRuleService.useBillNumber("RESI_RECEIPT")` 生成收据号
  - 批量更新 `resi_receivable.pay_state/paid_amount/pay_log_id`
  - INSERT `resi_pay_log`
  - 清除 Redis 看板缓存 `resi:dashboard:{projectId}:*`
  - 返回收据数据
- [ ] 并发测试：同一笔应收被两个并发请求收款，只有一个成功

**S4-05 打印接口**
- [ ] BE-B：`GET /resi/print/receipt/{payLogId}`（返回结构化收据 JSON，字段列表按 `field_config` 排序）
- [ ] FE-B：收款成功弹窗（展示收据号、金额、打印按钮），调用现有打印工具渲染

**S4-06 测试**（QA）
- [ ] 批量生成：选月份后执行，500个房间生成应收，耗时和数量正确
- [ ] 重复生成：二次执行同月份生成，返回"跳过N条（已存在）"，不产生重复数据
- [ ] 收款并发测试：Jmeter 模拟 10 个并发请求对同一应收收款，仅 1 个成功，其余返回"该费用已被收取"
- [ ] 收款金额验证：应收100元，折扣9折=90元，收90元，`resi_receivable.paid_amount=90`，`pay_state='2'`

#### 验收标准

- [ ] 批量生成 500 个房间物业费，耗时 < 20 秒，`gen_batch` 字段正确填充
- [ ] 重复生成同月不产生重复数据（`gen_batch` 已存在的记录被跳过）
- [ ] 收款核心接口并发安全：10 并发同一应收，只有 1 次成功
- [ ] 收款成功后：`resi_receivable.pay_state='2'`，`resi_pay_log` 有记录，收据号符合 `ZS202607XXXXXX` 格式
- [ ] Redis 缓存在收款后被清除（下次看板请求重新计算）
- [ ] `BillRuleController` 流水号无重复（并发下生成 100 个收据号，全部唯一）

---

### Sprint 5 — 收银台完整功能与核心报表（第 11-12 周）

**周期**：2026-07-27 ～ 2026-08-07  
**目标**：完成收银台全部辅助操作、打印完整流程、5张核心报表  
**Sprint Goal**：P0 阶段全部完成，可演示完整的物业收费业务流程

#### Story 列表

| Story ID | Story 标题 | 负责人 | 故事点 | 优先级 |
|---|---|---|---|---|
| S5-01 | 收银台：退款 + 冲红 | BE-B + FE-A | 5 | 🔴 P0 |
| S5-02 | 收银台：预收款收取与冲抵 | BE-B + FE-A | 5 | 🔴 P0 |
| S5-03 | 收银台：押金收取与退还 | BE-B + FE-A | 3 | 🔴 P0 |
| S5-04 | 缴费通知单打印（批量） | BE-B + FE-B | 3 | 🔴 P0 |
| S5-05 | 核心报表（5张）：交易汇总/明细/收费率/欠费明细/应收管理 | BE-B + FE-B | 8 | 🔴 P0 |
| S5-06 | P0 阶段综合测试 + 内部演示准备 | QA + PM | 5 | 🔴 P0 |

**总故事点**：29 点

#### 任务分解

**S5-01 退款 + 冲红**
- [ ] BE-B：`POST /resi/cashier/refund`（退款，更新 `resi_receivable.pay_state`，写退款流水）
- [ ] BE-B：`POST /resi/cashier/write-off`（冲红：原流水标记冲红，新建对冲流水 `parent_log_id` 关联）
- [ ] BE-B：退款校验：已收金额不得退超，押金单独走押金退款
- [ ] FE-A：收款记录列表（含退款/冲红按钮，已复核记录禁止操作）
- [ ] FE-A：复核操作（财务角色可复核，复核后不可撤销）

**S5-02 预收款**
- [ ] BE-B：`POST /resi/cashier/pre-pay/add`（收取预收款：写 `resi_pre_account` 余额、`resi_pre_pay` 流水）
- [ ] BE-B：收款接口增加预收款冲抵逻辑（`SELECT FOR UPDATE` 锁余额账户，专款专冲校验）
- [ ] BE-B：`POST /resi/finance/pre-pay/batch-offset`（批量冲预收款）
- [ ] FE-A：收银台右侧区域：预收款余额展示 + 勾选冲抵

**S5-03 押金**
- [ ] BE-B：押金在收款接口中自动识别（`fee_type=DEPOSIT` 时写 `resi_deposit`）
- [ ] BE-B：`PUT /resi/finance/deposit/{id}/refund`（押金退还）
- [ ] FE-A：收银台下方押金台账入口，押金退还操作

**S5-04 缴费通知单**
- [ ] BE-B：`GET /resi/print/notice/{receivableId}`（单张通知单 JSON）
- [ ] BE-B：`POST /resi/print/notice/batch`（批量：传 receivableIds，返回多张通知单数据）
- [ ] FE-B：应收管理页"打印通知单"功能（勾选→批量打印，复用现有打印工具）

**S5-05 核心报表（5张）**
- [ ] BE-B：交易汇总 `GET /resi/report/transaction-summary`（按费用名/收款方式汇总，GROUP BY）
- [ ] BE-B：交易明细 `GET /resi/report/transaction-detail`（JOIN resi_receivable，支持分页+导出）
- [ ] BE-B：收费率报表 `GET /resi/report/collection-rate`（应收/实收/收费率，按账单月分组）
- [ ] BE-B：欠费明细 `GET /resi/report/arrears-detail`（pay_state='0'，支持楼栋/房间/费用筛选）
- [ ] BE-B：应收管理 `GET /resi/report/receivable-mgmt`（支持单条删除未收费用/补录）
- [ ] 所有报表支持 `export=true` 参数导出 Excel
- [ ] FE-B：5张报表页面（Element UI Table + 分页 + 搜索条件 + 导出按钮）

**S5-06 内部演示**（PM 主导）
- [ ] QA：P0 全量回归测试
- [ ] PM：准备演示数据（1个项目/3栋楼/100个房间/500条应收）
- [ ] PM：内部演示脚本（项目创建→费用配置→分配→生成应收→抄表→收款→报表）

#### 验收标准

- [ ] 退款：退款后 `paid_amount` 减少，`pay_state` 回退到 `'1'`（部分退款）或 `'0'`（全额退款）
- [ ] 冲红：原流水状态标记为 `WRITEOFF`，不可再次操作；新建对冲流水金额为负数
- [ ] 预收款：收取100元预收，冲抵物业费80元，余额剩余20元，3张记录（收入/冲出/日志）均正确
- [ ] 专款专冲：指定费用的预收款不能冲抵其他费用应收
- [ ] 5张报表数据与数据库直接 SQL 查询结果一致（QA 交叉验证）
- [ ] 批量导出 1000 行数据，Excel 文件生成 < 30 秒
- [ ] 内部演示通过 PM 验收，无 P0/P1 级别未解决 Bug

**P0 阶段完成标准（M3 里程碑）**：本 Sprint 结束即 M3，需满足：

- [ ] 完整业务流程可端到端运行（从录入档案到收款打印）
- [ ] 5张核心报表数据准确
- [ ] 所有 P0 接口响应时间 < 2 秒（单用户）
- [ ] QA 测试报告：P0 级 Bug 全部关闭，P1 级 Bug ≤ 5 个

---

### Sprint 6 — 管理增强（第 13-14 周）

**周期**：2026-08-10 ～ 2026-08-21  
**目标**：完成 P1 管理增强功能：过户、调账、发票、车位、折扣深化  
**Sprint Goal**：财务管理员可以完整处理换发票、减免违约金、房屋过户业务

#### Story 列表

| Story ID | Story 标题 | 负责人 | 故事点 | 优先级 |
|---|---|---|---|---|
| S6-01 | 房屋过户（档案+历史记录） | BE-A + FE-A | 5 | 🟡 P1 |
| S6-02 | 调账记录（减免滞纳金/金额调整/状态调整） | BE-B + FE-A | 4 | 🟡 P1 |
| S6-03 | 发票换开功能 | BE-B + FE-B | 3 | 🟡 P1 |
| S6-04 | 车场/车位管理 | BE-A + FE-A | 3 | 🟡 P1 |
| S6-05 | 收银台：减免违约金 + 折扣应用完善 | BE-B + FE-A | 3 | 🟡 P1 |
| S6-06 | 管理增强测试 | QA | 3 | 🟡 P1 |

**总故事点**：21 点

#### 任务分解

**S6-01 房屋过户**
- [ ] BE-A：`POST /resi/archive/room/transfer`（事务）
  - 旧业主绑定 `is_current=0`，`unbind_date=today`
  - 新建新业主绑定
  - INSERT `resi_room_transfer`
  - 更新 `resi_room.state='TRANSFERRED'`（临时状态）
- [ ] BE-A：`GET /resi/report/transfer-query`（过户查询报表，按时间段）
- [ ] FE-A：房间详情页"过户"按钮 → 选择新业主弹窗 → 确认 → 记录展示

**S6-02 调账**
- [ ] BE-B：`PUT /resi/receivable/{id}/adjust`（调账）
  - 支持类型：AMOUNT/PERIOD/STATUS/OVERDUE_WAIVE
  - 每次调整写 `resi_adjust_log`，保留 `before_value/after_value`
  - OVERDUE_WAIVE：更新 `resi_receivable.overdue_fee=0`，重算 `receivable`
- [ ] BE-B：减免违约金接口 `POST /resi/cashier/waive-overdue`
- [ ] FE-A：应收列表"调账"按钮，弹窗选择调账类型和原因

**S6-03 发票换开**
- [ ] BE-B：`POST /resi/finance/invoice/exchange`（关联 pay_log_id，INSERT resi_invoice_record）
- [ ] BE-B：`GET /resi/finance/invoice`（发票查询，支持时间/收据号筛选）
- [ ] FE-B：收款流水列表"换开发票"按钮，弹窗录入发票信息

**S6-04 车场车位**
- [ ] BE-A：车场区域 CRUD，路径 `/resi/archive/parking-area`
- [ ] BE-A：车位 CRUD，路径 `/resi/archive/parking-space`（支持批量导入）
- [ ] FE-A：车场/车位管理页（车位状态：空闲/占用/已售 Tag 展示）

**S6-05 收银台增强**
- [ ] BE-B：减免违约金接口（收银台触发，调 adjust 逻辑）
- [ ] BE-B：折扣自动推荐（查询当前有效折扣，返回给收银台）
- [ ] FE-A：收银台右侧区域：折扣下拉选择，实时更新应收金额

**S6-06 测试**（QA）
- [ ] 过户后原业主资产绑定历史保留，新业主可正常在收银台看到待缴费用
- [ ] 调账记录不可删除，每次调账有完整 before/after 快照
- [ ] 发票换开：同一收款单只能换开一张发票（唯一索引保护）

#### 验收标准

- [ ] 过户事务原子性：若中间步骤失败，全部回滚（无半过户状态）
- [ ] 过户后旧业主的历史收款记录仍可查询
- [ ] 减免违约金后：`overdue_fee=0`，`receivable` 重算正确，调账记录存在
- [ ] 换发票：通过收据号查询到对应收款流水，录入发票号后可在发票统计表查询

---

### Sprint 7 — 完整报表与看板（第 15-16 周）

**周期**：2026-08-24 ～ 2026-09-04  
**目标**：完成全部 20+ 报表、数据看板、定时推送  
**Sprint Goal**：领导层可以通过看板掌握实时收费情况，所有报表可按需查询导出

#### Story 列表

| Story ID | Story 标题 | 负责人 | 故事点 | 优先级 |
|---|---|---|---|---|
| S7-01 | 数据看板 5 个接口（含 Redis 缓存） | BE-B + FE-B | 5 | 🟡 P1 |
| S7-02 | 管理层日报定时推送（Quartz + 微信消息） | BE-A | 5 | 🟡 P1 |
| S7-03 | 剩余报表（15张）批量实现 | BE-B + FE-B | 10 | 🟡 P1 |
| S7-04 | 费用情况矩阵表（颜色矩阵，特殊实现） | BE-B + FE-B | 5 | 🟡 P1 |
| S7-05 | 滞纳金自动计算 Quartz 任务 | BE-A | 3 | 🟡 P1 |
| S7-06 | P1 阶段综合测试 | QA | 4 | 🟡 P1 |

**总故事点**：32 点

#### 任务分解

**S7-01 看板**
- [ ] BE-B：5个看板接口（`/resi/dashboard/*`），Redis 缓存 5 分钟
  - `overview`：应收/实收/收费率/欠费（当月聚合）
  - `collection-trend`：近12月收缴率折线图数据
  - `payment-method-dist`：收款方式饼图
  - `arrears-top10`：欠费 Top10 房间
  - `fee-type-dist`：各费用类型收费占比
- [ ] FE-B：`views/resi/dashboard/index.vue`（ECharts 4.9.0）
  - 4张卡片 + 折线图 + 饼图 + 柱状图，懒加载各自接口
  - 数据刷新按钮（主动清缓存重新加载）

**S7-02 日报推送**
- [ ] BE-A：`ResiDailyReportJob`（Cron: `0 0 18 * * ?`）
  - 查询 `resi_wx_user` 中 `project_id` 对应的管理层 openid
  - 聚合当日数据（今日实收、本月收费率、欠费总额）
  - 写入 `sys_sms` 队列（`es_type='3'` 微信），由现有 SmsTask 消费发送
- [ ] BE-A：在 sys_job 表激活日报推送任务（`status='0'`）

**S7-03 剩余 15 张报表**（BE-B + FE-B，流水线作业）
- 综合查询表、交易记录、收费明细、折扣明细、预收款报表
- 收清欠汇总、清欠率、过户查询、提醒明细、发票统计
- 押金记录、日结明细、调账记录、银行代收（展示层）、账单通知
- 每张报表：后端接口 + 分页 + 导出 + 前端页面

**S7-04 费用情况矩阵表**
- [ ] BE-B：透视查询，返回 `{months[], rows[{roomId, roomName, cells[{period, status, amount}]}]}`
- [ ] FE-B：动态列 `el-table`，`cell-class-name` 回调按 status 返回颜色 class
  - PAID→绿色背景，UNPAID→黄色，PART_PAID→红色，NOT_GEN→灰色

**S7-05 滞纳金计算任务**
- [ ] BE-A：`ResiOverdueCalcJob`（Cron: `0 0 1 * * ?`）
  - 查询所有 `pay_state IN ('0','1')` 且超过 `overdue_days` 的应收
  - 重新计算 `overdue_fee = (逾期天数 - overdue_days) × price × overdue_rate`
  - 不超过 `overdue_max`（若配置）
  - 批量更新 `receivable = total + overdue_fee - discount_amount`

**S7-06 测试**（QA）
- [ ] 看板数据与报表数据一致性交叉验证
- [ ] 日报推送：Mock 微信 API，验证消息内容格式正确
- [ ] 滞纳金计算：逾期10天，日利率0.05%，1000元本金 → 滞纳金=5元

#### 验收标准

- [ ] 看板5个接口首次加载 < 3 秒，命中 Redis 缓存时 < 200ms
- [ ] 收款操作后，看板数据在下次请求时已更新（缓存失效）
- [ ] 费用情况矩阵表：1000个房间×12个月，渲染 < 5 秒
- [ ] 全部 20 张报表可正常分页查询，支持 Excel 导出
- [ ] 滞纳金每日计算误差 < 0.01元

**P1 阶段完成标准（M4 里程碑）**：
- [ ] 所有 P1 功能通过 QA 测试
- [ ] 看板数据准确，领导层日报推送成功发送到测试微信账号
- [ ] 20张报表数据经过 QA 交叉验证（与数据库 SQL 结果一致）

---

### Sprint 8 — C端基础（第 17-18 周）

**周期**：2026-09-07 ～ 2026-09-18  
**目标**：搭建 `zhaoxinwy-wxmp` 微信端，完成登录绑定、账单查询  
**Sprint Goal**：业主可以用微信小程序登录并查看自己的欠费账单

#### Story 列表

| Story ID | Story 标题 | 负责人 | 故事点 | 优先级 |
|---|---|---|---|---|
| S8-01 | wxmp 模块安全配置（独立 JWT + 过滤器） | BE-B | 3 | 🔴 P0 |
| S8-02 | 微信授权登录（code → openid → JWT） | BE-B + FE-B | 5 | 🔴 P0 |
| S8-03 | 业主手机号绑定（短信验证码） | BE-B + FE-B | 5 | 🔴 P0 |
| S8-04 | C端账单查询与历史缴费 | BE-B + FE-B | 5 | 🟡 P1 |
| S8-05 | C端功能测试 + 微信开发者工具联调 | QA + FE-B | 3 | 🔴 P0 |

**总故事点**：21 点

#### 任务分解

**S8-01 wxmp 安全配置**
- [ ] BE-B：`zhaoxinwy-wxmp` 独立 Spring Security 配置（`/wxmp/**` 路径使用 C端 JWT 过滤器）
- [ ] BE-B：C端 JWT 工具类（Payload：customerId/openid/projectId/roomIds/exp）
- [ ] BE-B：Token 有效期7天，refreshToken 存 Redis 30天
- [ ] 配置文件：wxmp 相关配置项（`resi.wx.*`，从环境变量读取）

**S8-02 微信授权登录**
- [ ] BE-B：`POST /wxmp/auth/login`
  - 接收 code，调用微信 `code2session` API 获取 openid
  - 查询 `resi_wx_user WHERE openid=?`
  - 已绑定：生成 JWT 返回；未绑定：返回 `{needBind: true, openid}`
  - 更新 `last_login_time`
- [ ] FE-B（小程序端）：`wx.login()` → 调接口 → 存 storage → 进入主页或绑定页

**S8-03 业主绑定**
- [ ] BE-B：`POST /wxmp/auth/send-sms`（发送短信验证码，写 Redis，TTL 5分钟，限频1次/分钟）
- [ ] BE-B：`POST /wxmp/auth/bind`
  - 校验短信验证码（Redis 取值比对）
  - 查询 `resi_customer WHERE project_id=? AND phone=?`
  - 更新 `resi_customer.openid`，INSERT `resi_wx_user`
  - 返回 JWT
- [ ] FE-B：绑定页面（输入手机号→发送验证码→输入验证码→选择项目（若多个）→绑定成功）

**S8-04 C端账单查询**
- [ ] BE-B：`GET /wxmp/payment/bills`（返回当前业主所有未缴账单，按账单月倒序）
- [ ] BE-B：`GET /wxmp/payment/history`（历史缴费记录，分页）
- [ ] FE-B：账单列表页（分欠费/已缴Tab，金额红色突出显示）
- [ ] FE-B：历史缴费详情（展示费用明细、收款方式、收据号）

**S8-05 测试**（QA）
- [ ] 微信开发者工具真机调试：登录→绑定完整流程
- [ ] 短信验证码：超时（5分钟）后失效，同手机号1分钟内限1次
- [ ] Token 过期：7天后请求返回 401，前端跳转重新登录

#### 验收标准

- [ ] 微信小程序登录流程完整（手机真机测试通过）
- [ ] 绑定后，`resi_customer.openid` 字段有值，`resi_wx_user` 有记录
- [ ] 短信验证码在5分钟内有效，超时提示"验证码已过期"
- [ ] 账单列表按业主房间过滤，无法看到其他业主数据（安全隔离）

---

### Sprint 9 — C端完整功能（第 19-20 周）

**周期**：2026-09-21 ～ 2026-10-02  
**目标**：完成微信在线缴费、通知公告、管家展示、便民信息全部 C 端功能  
**Sprint Goal**：业主可以在微信小程序完成全流程缴费，并收到物业推送通知

#### Story 列表

| Story ID | Story 标题 | 负责人 | 故事点 | 优先级 |
|---|---|---|---|---|
| S9-01 | 微信支付集成（统一下单+支付回调） | BE-B | 8 | 🔴 P0 |
| S9-02 | 电子收据展示 | BE-B + FE-B | 3 | 🔴 P0 |
| S9-03 | 欠费通知推送（B端一键发送） | BE-A + FE-B | 5 | 🟡 P1 |
| S9-04 | 通知公告（B端发布 + C端展示） | BE-A + FE-B | 4 | 🟡 P1 |
| S9-05 | 管家展示与评价 | BE-A + FE-B | 3 | 🟡 P1 |
| S9-06 | 便民信息 | BE-A + FE-B | 2 | 🟡 P1 |
| S9-07 | 消息推送重试任务 | BE-A | 2 | 🟡 P1 |
| S9-08 | C端完整测试 + 用户 UAT | QA + PM | 5 | 🔴 P0 |

**总故事点**：32 点

#### 任务分解

**S9-01 微信支付**
- [ ] BE-B：`POST /wxmp/payment/pre-create`（创建支付订单）
  - 汇总金额，防重复下单（Redis 锁，key=`resi:pay:creating:{customerId}`）
  - 调用微信统一下单 API，返回 `prepayId`
- [ ] BE-B：`POST /wxmp/payment/notify`（微信支付回调，需挂公网域名）
  - 验签（WxJava SDK）
  - 幂等保护（Redis `setIfAbsent`，TTL 60秒）
  - 调用 `ResiCashierService.collect()`（`client=2`）
  - 写 `resi_push_record`（PAY_SUCCESS 场景，待发送）
- [ ] BE-B：订单超时关闭 Job（`ResiOrderExpireJob`，`0 */5 * * * ?`）
- [ ] FE-B：支付确认页 → 调起微信支付 → 支付结果轮询（3秒/次，最多10次）

**S9-02 电子收据**
- [ ] BE-B：`GET /wxmp/payment/receipt/{payLogId}`（返回收据 HTML 或结构化JSON）
- [ ] FE-B：收据展示页（项目名/房间/费用明细/金额/时间/流水号，底部公章图）
- [ ] FE-B：长按保存图片（wx.canvasToTempFilePath 或 html2canvas）

**S9-03 欠费通知推送**
- [ ] BE-A：`POST /resi/notice/send-arrears`（B端一键发送）
  - 查询欠费业主的 openid
  - 批量写入 `resi_push_record`（status=0）
  - 接口立即返回，异步发送（由推送任务消费）
- [ ] BE-A：欠费通知 Quartz Job（月度自动，`0 0 9 10 * ?`）
- [ ] FE-B：B端"发送欠费通知"按钮（选月份/范围→预览发送数量→确认→异步执行提示）

**S9-04 通知公告**
- [ ] BE-A：公告 CRUD（B端），路径 `/resi/notice`
- [ ] BE-A：公告发布时按 `target_type` 查目标业主 openid，写 `resi_push_record`
- [ ] BE-A：`GET /wxmp/notice/list`（C端，按项目/状态过滤，分页）
- [ ] BE-A：`GET /wxmp/notice/{id}`（详情，`view_count+1`）
- [ ] FE-B：B端公告管理页（富文本编辑器 `<editor>` 组件，定向推送配置）
- [ ] FE-B：C端公告列表页 + 详情页

**S9-05 管家展示与评价**
- [ ] BE-A：管家 CRUD（B端），路径 `/resi/butler`；管家负责房间配置
- [ ] BE-A：`GET /wxmp/butler/my-butler`（根据 JWT 中 roomIds 查找专属管家）
- [ ] BE-A：`POST /wxmp/butler/review`（评价，唯一键 `uk_review` 保证每月限评一次）
- [ ] FE-B：C端管家展示页（头像/姓名/网格/评分），一键拨打 `wx.makePhoneCall`

**S9-06 便民信息**
- [ ] BE-A：便民信息 CRUD（B端），`GET /wxmp/convenience/list`（C端按分类展示）
- [ ] FE-B：C端便民信息页（按分类分组展示，一键拨打）

**S9-07 推送重试**
- [ ] BE-A：`ResiPushRetryJob`（`0 0 */2 * * ?`）
  - 查询 `status=2 AND retry_count < 3` 的记录
  - 重新调用微信模板消息 API
  - 失败次数更新，超过3次标记 `-1` 不再重试
- [ ] BE-A：`ResiPushCleanJob`（每月1日清理3个月前成功记录）

**S9-08 用户 UAT**（PM + QA）
- [ ] 准备 UAT 环境和真实测试账号
- [ ] 邀请 3～5 名真实物业工作人员进行用户验收测试
- [ ] 收集 UAT 反馈，分类为 Bug/优化/需求变更

#### 验收标准

- [ ] 微信支付端到端测试（使用微信沙箱环境）：从账单→支付→到账→收据 全流程通过
- [ ] 支付回调幂等：模拟微信重复回调同一订单，只处理一次
- [ ] 订单超时：30分钟未支付，订单状态变为 CLOSED，业主再次打开提示"订单已过期"
- [ ] 公告定向推送：向指定楼栋推送，该楼栋业主5分钟内收到微信消息
- [ ] 管家评价：同月同管家同业主二次评价，返回"本月已评价"（唯一键保护）
- [ ] UAT 通过率 ≥ 90%，P0/P1 级反馈全部解决

**P2 阶段完成标准（M5 里程碑）**：
- [ ] C端全部功能通过真机测试
- [ ] 微信支付沙箱测试通过
- [ ] UAT 结束，剩余问题在上线冲刺内解决

---

### Sprint 10 — 上线冲刺（第 21 周）

**周期**：2026-10-05 ～ 2026-10-09（5个工作日）  
**目标**：性能压测、UAT 缺陷修复、生产部署、人员培训、上线  
**Sprint Goal**：系统上线，物业工作人员可以正式使用

#### 任务列表（非 Story 形式，直接列操作项）

**性能测试**（BE-A + BE-B，周一）
- [ ] Jmeter 压测：收款接口 50 并发，TPS ≥ 20，平均响应时间 < 500ms
- [ ] 批量生成应收：1000个房间同时生成，< 60 秒完成
- [ ] 报表接口：欠费明细5000行，响应 < 3 秒
- [ ] Redis 缓存命中率验证（看板接口）
- [ ] 性能测试报告输出

**UAT 缺陷修复**（全员，周一至周二）
- [ ] 分类处理 UAT 反馈
- [ ] P0/P1 Bug 必须修复上线
- [ ] P2/P3 Bug 记录至下个迭代

**生产环境准备**（BE-A，周二）
- [ ] 生产环境 Docker Compose 配置确认
- [ ] 环境变量配置（DB密码/Redis密码/微信密钥等，不进代码仓库）
- [ ] 数据库备份策略配置（每日全量备份）
- [ ] Nginx 生产配置（HTTPS/限流/日志）

**生产部署**（BE-A + BE-B，周三）
- [ ] 执行生产 DB 初始化（28张表DDL + 初始化数据）
- [ ] Docker 镜像构建并推送到镜像仓库
- [ ] 灰度部署（先10%流量，观察30分钟，无异常全量）
- [ ] 生产环境冒烟测试（核心链路人工验证）

**用户培训**（PM + QA，周三至周四）
- [ ] 操作手册编写（B端：档案录入/收银台/报表）
- [ ] 收银员岗位培训（1小时，重点：收银台操作和打印）
- [ ] 财务岗位培训（1小时，重点：报表查询和发票换开）
- [ ] 管理员培训（1小时，重点：费用配置和权限管理）

**上线与观察**（全员，周五）
- [ ] 正式切流，监控告警无异常
- [ ] 上线后2小时内，每10分钟检查一次日志
- [ ] 准备回滚方案（老系统数据不删除，新功能可随时下线菜单）
- [ ] 发送上线通知给物业公司管理层

#### 验收标准（M6 里程碑）

- [ ] 压测报告：收款接口 50 并发，P99 < 1 秒，无报错
- [ ] 生产数据库：28张 `resi_` 表全部存在，初始化数据正确
- [ ] 生产冒烟：可完整执行 创建项目→配置费用→收款→打印收据 流程
- [ ] 培训完成：收银员、财务、管理员均完成培训并签署确认
- [ ] 系统运行4小时无 P0 错误日志

---

## 5. 风险登记册

| 编号 | 风险描述 | 概率 | 影响 | 应对措施 | 负责人 |
|---|---|---|---|---|---|
| R01 | 微信支付资质审核延迟，导致 C 端支付无法上线 | 中 | 高 | 提前30天提交申请；C端展示功能可先上线，支付功能延后 | PM |
| R02 | 现有公式引擎接口不稳定或无法注入 | 低 | 高 | S2 前确认 `FormulaCalculator` Bean 可用；备选：临时重实现简单版 | BE-A |
| R03 | `BillRuleService` 并发下流水号重复 | 低 | 高 | S4 前并发测试验证；如不满足，改 Redis INCR 方案，需 PM 评审 | BE-B |
| R04 | 大规模批量生成（1000房间）超时 | 中 | 中 | S4 前压测；超时则改为异步 Job + 进度轮询方案 | BE-A |
| R05 | FE-B 小程序开发经验不足，C 端进度滞后 | 中 | 中 | S7 结束后评估；必要时引入外包支援或延迟 P2 部分功能 | PM |
| R06 | 微信模板消息申请被拒（内容不符合规范） | 中 | 中 | 提前申请3个模板（日报/欠费/缴费成功）；审核期间用短信替代 | BE-A |
| R07 | UAT 中发现重大业务逻辑错误，影响上线时间 | 低 | 高 | UAT 提前到 S9 中期进行；核心流程在 M3 时就邀请业务人员验证 | PM + QA |
| R08 | 需求变更（业主提出新的收费规则） | 中 | 中 | 变更控制：S5 后需求冻结；新需求进入下一迭代 | PM |
| R09 | 测试数据不足，报表无法验证准确性 | 中 | 低 | S0 准备真实脱敏数据或标准测试数据集（500房间×12月） | QA |

---

## 6. 定义完成（DoD）

### Story 级别完成定义

每个 Story 满足以下所有条件才算 Done：

- [ ] **代码完成**：功能代码提交至 develop 分支，代码符合 CLAUDE.md 所有规范
- [ ] **测试通过**：QA 执行验收测试用例，100% 通过率（允许 P3 遗留，需记录）
- [ ] **接口文档**：Swagger 文档更新，新增接口均有完整描述和示例
- [ ] **日志完整**：收款/退款/调账等核心操作，`sys_oper_log` 有对应记录
- [ ] **代码审查**：PR 经过另一位开发 Review，CLAUDE.md 第11.3节检查项全部通过
- [ ] **无 P0 Bug**：当前 Story 范围内无未修复的 P0 级 Bug
- [ ] **验收标准**：Story 卡片中的验收标准逐条确认

### Sprint 级别完成定义

- [ ] 当前 Sprint 所有 Story 达到 Story DoD
- [ ] 测试报告输出（用例执行结果/缺陷统计/未修复原因）
- [ ] Sprint 回顾会议完成，改进项记录至下个 Sprint
- [ ] 演示视频或演示会议完成（内部）

### 禁止带入下个 Sprint 的情况

- P0 级 Bug（系统崩溃、数据错误、权限穿透）
- 违反 CLAUDE.md 禁止行为清单的代码（修改现有表/接口/硬编码密钥等）
- 未经测试的数据库 Schema 变更

---

## 7. 里程碑节点

| 里程碑 | 日期 | 完成标准 | 验收人 |
|---|---|---|---|
| **M0** 开发就绪 | 2026-05-22 | 环境搭建完成，28张表DDL执行无误，CI/CD 可运行 | BE-A |
| **M1** 档案层完成 | 2026-06-12 | 项目/楼栋/房间/客户/仪表全部 CRUD 可用，权限隔离验证通过 | PM + QA |
| **M2** 核心链路可演示 | 2026-07-24 | 可完整演示：配置费用→生成应收→收款→打印收据 | PM + 业务代表 |
| **M3** P0 完成 | 2026-08-07 | 5张核心报表准确，无未修复 P0/P1 Bug，内部演示通过 | PM + QA |
| **M4** P1 完成 | 2026-08-28 | 20张报表可用，看板数据准确，日报推送成功 | PM + 业务负责人 |
| **M5** P2 完成/UAT | 2026-09-25 | C端全功能通过，UAT通过率≥90%，P0/P1问题全部修复 | PM + 甲方代表 |
| **M6** 正式上线 | 2026-10-09 | 生产部署完成，冒烟测试通过，培训完成，运行4小时无P0错误 | PM + 全员 |

---

## 附录：Story 点数估算说明

| 故事点 | 参考工作量（后端 + 前端合计） |
|---|---|
| 1 | 半天以内，简单配置或参数修改 |
| 2 | 1天，标准 CRUD（单表，无复杂业务逻辑） |
| 3 | 1.5～2天，含联表查询或中等复杂业务规则 |
| 5 | 2.5～3天，含事务/并发/复杂联动逻辑 |
| 8 | 4天，高复杂度（收银台核心/微信支付/公摊计算） |
| 13 | 超过5天，需拆分（不允许出现在计划中） |

**团队速度参考**：每个 Sprint（2周）目标 Velocity 为 20～30 故事点（含测试和联调时间）

---

*本文档随项目推进持续更新，每个 Sprint 结束后更新实际进度和下个 Sprint 计划。*  
*如有需求变更，须经 PM 评估影响范围后更新本文档，并同步更新技术方案文档。*  
*最后更新：2026-05-10*
