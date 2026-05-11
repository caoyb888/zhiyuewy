# 智悦物业系统系统 - 模块与 API 文档

> 本文档自动生成于系统源码分析，涵盖现有模块、API 接口概览及数据库表结构。

---

## 一、系统架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                      前端 (Vue 2.6)                          │
│                  pms-web / Nginx                             │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                   后端 (Spring Boot 2.5.6)                  │
│  zhaoxinwy-admin (入口)                                      │
│    ├── zhaoxinwy-system   系统管理                           │
│    ├── zhaoxinwy-pms      物业管理核心                       │
│    ├── zhaoxinwy-workflow 工作流引擎                         │
│    ├── zhaoxinwy-quartz   定时任务                           │
│    ├── zhaoxinwy-generator 代码生成器                        │
│    ├── zhaoxinwy-common   通用工具                           │
│    ├── zhaoxinwy-framework 框架核心                          │
│    └── zhaoxinwy-tenant   多租户支持                         │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│              MySQL 8.0  +  Redis 6                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 二、现有模块列表

### 后端 Java 模块

| 模块名 | 说明 | 状态 |
|--------|------|------|
| `zhaoxinwy-admin` | Web 服务入口，Spring Boot 主启动类 | ✅ 可用 |
| `zhaoxinwy-system` | 系统管理（用户、角色、菜单、部门、岗位、字典、参数、通知公告） | ✅ 可用 |
| `zhaoxinwy-pms` | 物业管理核心（资产、收费、抄表、统计报表） | ✅ 可用 |
| `zhaoxinwy-workflow` | 工作流引擎（基于 Activiti，含流程设计器、报修/投诉工单） | ✅ 可用 |
| `zhaoxinwy-quartz` | 定时任务管理 | ✅ 可用 |
| `zhaoxinwy-generator` | 代码生成器 | ✅ 可用 |
| `zhaoxinwy-framework` | 框架核心（安全、拦截器、AOP、Web 配置） | ✅ 可用 |
| `zhaoxinwy-common` | 通用工具类、常量、异常定义 | ✅ 可用 |
| `zhaoxinwy-tenant` | 多租户支持 | ✅ 可用 |

### 未开源/已移除模块

| 模块名 | 说明 | 状态 |
|--------|------|------|
| `zhaoxinwy-owner` | 业主端功能 | ❌ 源码缺失 |
| `zhaoxinwy-pay` | 支付模块 | ❌ 源码缺失 |
| `zhaoxinwy-park` | 停车管理模块 | ❌ 源码缺失 |
| `zhaoxinwy-jmreport` | 报表服务（积木报表） | ❌ 源码缺失 |

### 前端模块

| 模块名 | 技术栈 | 说明 |
|--------|--------|------|
| `pms-web` | Vue 2.6 + Element UI 2.15.6 | 管理后台前端 |

---

## 三、API 接口概览

> 所有接口前缀：`/prod-api`（生产环境）或 `/dev-api`（开发环境）
> 实际后端映射：`/prod-api/*` → `/*`


### zhaoxinwy-admin

**CaptchaController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/common/CaptchaController.java`
- 接口：
  - `48:    @GetMapping("/captchaImage")`

**CommonController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/common/CommonController.java`
- 接口：
  - `41:    @GetMapping("common/download")`
  - `70:    @PostMapping("/common/upload")`
  - `94:    @GetMapping("/common/download/resource")`

**CacheController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/monitor/CacheController.java`
- 基础路径：`/monitor/cache`
- 接口：
  - `32:    @GetMapping()`

**ServerController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/monitor/ServerController.java`
- 基础路径：`/monitor/server`
- 接口：
  - `21:    @GetMapping()`

**SysLogininforController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/monitor/SysLogininforController.java`
- 基础路径：`/monitor/logininfor`
- 接口：
  - `34:    @GetMapping("/list")`
  - `44:    @GetMapping("/export")`
  - `54:    @DeleteMapping("/{infoIds}")`
  - `62:    @DeleteMapping("/clean")`

**SysOperlogController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/monitor/SysOperlogController.java`
- 基础路径：`/monitor/operlog`
- 接口：
  - `34:    @GetMapping("/list")`
  - `44:    @GetMapping("/export")`
  - `54:    @DeleteMapping("/{operIds}")`
  - `62:    @DeleteMapping("/clean")`

**SysUserOnlineController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/monitor/SysUserOnlineController.java`
- 基础路径：`/monitor/online`
- 接口：
  - `43:    @GetMapping("/list")`
  - `87:    @DeleteMapping("/{tokenId}")`

**SysConfigController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysConfigController.java`
- 基础路径：`/system/config`
- 接口：
  - `43:    @GetMapping("/list")`
  - `53:    @GetMapping("/export")`
  - `65:    @GetMapping(value = "/{configId}")`
  - `74:    @GetMapping(value = "/configKey/{configKey}")`
  - `85:    @PostMapping`
  - `102:    @PutMapping`
  - `118:    @DeleteMapping("/{configIds}")`
  - `130:    @DeleteMapping("/refreshCache")`

**SysDeptController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysDeptController.java`
- 基础路径：`/system/dept`
- 接口：
  - `43:    @GetMapping("/list")`
  - `54:    @GetMapping("/list/exclude/{deptId}")`
  - `75:    @GetMapping(value = "/{deptId}")`
  - `85:    @GetMapping("/treeselect")`
  - `95:    @GetMapping(value = "/roleDeptTreeselect/{roleId}")`
  - `110:    @PostMapping`
  - `126:    @PutMapping`
  - `151:    @DeleteMapping("/{deptId}")`

**SysDictDataController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysDictDataController.java`
- 基础路径：`/system/dict/data`
- 接口：
  - `44:    @GetMapping("/list")`
  - `54:    @GetMapping("/export")`
  - `66:    @GetMapping(value = "/{dictCode}")`
  - `75:    @GetMapping(value = "/type/{dictType}")`
  - `91:    @PostMapping`
  - `103:    @PutMapping`
  - `115:    @DeleteMapping("/{dictCodes}")`

**SysDictTypeController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysDictTypeController.java`
- 基础路径：`/system/dict/type`
- 接口：
  - `39:    @GetMapping("/list")`
  - `49:    @GetMapping("/export")`
  - `61:    @GetMapping(value = "/{dictId}")`
  - `72:    @PostMapping`
  - `88:    @PutMapping`
  - `104:    @DeleteMapping("/{dictIds}")`
  - `116:    @DeleteMapping("/refreshCache")`
  - `126:    @GetMapping("/optionselect")`

**SysIndexController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysIndexController.java`
- 基础路径：`/`

**SysLoginController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysLoginController.java`
- 接口：
  - `44:    @PostMapping("/login")`
  - `61:    @GetMapping("getInfo")`
  - `81:    @GetMapping("getRouters")`

**SysMenuController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysMenuController.java`
- 基础路径：`/system/menu`
- 接口：
  - `41:    @GetMapping("/list")`
  - `52:    @GetMapping(value = "/{menuId}")`
  - `61:    @GetMapping("/treeselect")`
  - `71:    @GetMapping(value = "/roleMenuTreeselect/{roleId}")`
  - `86:    @PostMapping`
  - `106:    @PutMapping`
  - `130:    @DeleteMapping("/{menuId}")`

**SysNoticeController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysNoticeController.java`
- 基础路径：`/system/notice`
- 接口：
  - `40:    @GetMapping("/list")`
  - `52:    @GetMapping(value = "/{noticeId}")`
  - `63:    @PostMapping`
  - `75:    @PutMapping`
  - `87:    @DeleteMapping("/{noticeIds}")`

**SysPostController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysPostController.java`
- 基础路径：`/system/post`
- 接口：
  - `42:    @GetMapping("/list")`
  - `52:    @GetMapping("/export")`
  - `64:    @GetMapping(value = "/{postId}")`
  - `75:    @PostMapping`
  - `95:    @PutMapping`
  - `115:    @DeleteMapping("/{postIds}")`
  - `124:    @GetMapping("/optionselect")`

**SysProfileController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysProfileController.java`
- 基础路径：`/system/user/profile`
- 接口：
  - `46:    @GetMapping`
  - `61:    @PutMapping`
  - `95:    @PutMapping("/updatePwd")`
  - `123:    @PostMapping("/avatar")`

**SysRegisterController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysRegisterController.java`
- 接口：
  - `29:    @PostMapping("/register")`

**SysRoleController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysRoleController.java`
- 基础路径：`/system/role`
- 接口：
  - `55:    @GetMapping("/list")`
  - `65:    @GetMapping("/export")`
  - `77:    @GetMapping(value = "/{roleId}")`
  - `89:    @PostMapping`
  - `110:    @PutMapping`
  - `144:    @PutMapping("/dataScope")`
  - `156:    @PutMapping("/changeStatus")`
  - `169:    @DeleteMapping("/{roleIds}")`
  - `179:    @GetMapping("/optionselect")`
  - `189:    @GetMapping("/authUser/allocatedList")`

**SysUserController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/system/SysUserController.java`
- 基础路径：`/system/user`
- 接口：
  - `56:    @GetMapping("/list")`
  - `66:    @GetMapping("/export")`
  - `76:    @PostMapping("/importData")`
  - `86:    @GetMapping("/importTemplate")`
  - `97:    @GetMapping(value = { "/", "/{userId}" })`
  - `119:    @PostMapping`
  - `146:    @PutMapping`
  - `169:    @DeleteMapping("/{userIds}")`
  - `184:    @PutMapping("/resetPwd")`
  - `198:    @PutMapping("/changeStatus")`

**SwaggerController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/tool/SwaggerController.java`
- 基础路径：`/tool/swagger`
- 接口：
  - `20:    @GetMapping()`

**TestController**

- 文件：`./zhaoxinwy-admin/src/main/java/com/zhaoxinms/web/controller/tool/TestController.java`
- 基础路径：`/test/user`
- 接口：
  - `44:    @GetMapping("/list")`
  - `53:    @GetMapping("/{userId}")`
  - `73:    @PostMapping("/save")`
  - `84:    @PutMapping("/update")`
  - `101:    @DeleteMapping("/{userId}")`


### zhaoxinwy-common

**BaseController**

- 文件：`./zhaoxinwy-common/src/main/java/com/zhaoxinms/common/core/controller/BaseController.java`


### zhaoxinwy-generator

**GenController**

- 文件：`./zhaoxinwy-generator/src/main/java/com/zhaoxinms/generator/controller/GenController.java`
- 基础路径：`/tool/gen`
- 接口：
  - `51:    @GetMapping("/list")`
  - `63:    @GetMapping(value = "/{talbleId}")`
  - `80:    @GetMapping("/db/list")`
  - `92:    @GetMapping(value = "/column/{talbleId}")`
  - `107:    @PostMapping("/importTable")`
  - `122:    @PutMapping`
  - `135:    @DeleteMapping("/{tableIds}")`
  - `146:    @GetMapping("/preview/{tableId}")`
  - `158:    @GetMapping("/download/{tableName}")`
  - `170:    @GetMapping("/genCode/{tableName}")`


### zhaoxinwy-pms

**ConfigBuildingController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/baseconfig/controller/ConfigBuildingController.java`
- 基础路径：`/pms/building`
- 接口：
  - `65:    @GetMapping("/list")`
  - `89:    @GetMapping(value = "/{id}")`
  - `101:    @PostMapping`
  - `115:    @PutMapping("/{id}")`
  - `129:	@DeleteMapping("/{id}")`
  - `143:    @GetMapping("/select")`

**ConfigFeeAlertController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/baseconfig/controller/ConfigFeeAlertController.java`
- 基础路径：`/baseconfig/ConfigFeeAlert`
- 接口：
  - `55:    @GetMapping`
  - `73:    @PostMapping`
  - `88:    @GetMapping("/{id}")`
  - `101:    @PutMapping("/{id}")`
  - `123:    @DeleteMapping("/{id}")`

**ConfigFeeItemController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/baseconfig/controller/ConfigFeeItemController.java`
- 基础路径：`/baseconfig/ConfigFeeItem`
- 接口：
  - `59:    @GetMapping`
  - `79:    @PostMapping`
  - `94:    @GetMapping("/{id}")`
  - `109:    @PutMapping("/{id}")`
  - `132:    @DeleteMapping("/{id}")`
  - `148:    @GetMapping("/select")`

**ConfigFeeSettingController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/baseconfig/controller/ConfigFeeSettingController.java`
- 基础路径：`/baseconfig/ConfigFeeSetting`
- 接口：
  - `50:    @GetMapping`
  - `77:    @PostMapping`

**ConfigHouseBlockController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/baseconfig/controller/ConfigHouseBlockController.java`
- 基础路径：`/baseconfig/ConfigHouseBlock`
- 接口：
  - `64:    @GetMapping`
  - `83:    @PostMapping`
  - `98:    @GetMapping("/{id}")`
  - `113:    @PutMapping("/{id}")`
  - `136:    @DeleteMapping("/{id}")`
  - `152:    @GetMapping("selectList")`

**ConfigHouseContractController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/baseconfig/controller/ConfigHouseContractController.java`
- 基础路径：`/baseconfig`
- 接口：
  - `79:    @GetMapping("/House/houseContract")`
  - `91:    @GetMapping("/HouseContractImport/Template")`
  - `120:    @PostMapping("/HouseContractImport/Uploader")`
  - `141:    @GetMapping("/HouseContractImport/Import")`

**ConfigHouseController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/baseconfig/controller/ConfigHouseController.java`
- 基础路径：`/baseconfig/House`
- 接口：
  - `95:    @GetMapping`
  - `124:    @GetMapping("/tips/{resourceName}")`
  - `137:    @GetMapping("stateOptions")`
  - `165:    @PostMapping`
  - `181:    @GetMapping("/{id}")`
  - `194:    @GetMapping("/name/{name}")`
  - `209:    @PutMapping("/{id}")`
  - `233:    @DeleteMapping("/{id}")`
  - `254:    @GetMapping("/rentControl/{buildingId}")`

**ConfigHouseImportController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/baseconfig/controller/ConfigHouseImportController.java`
- 基础路径：`/baseconfig/HouseImport`
- 接口：
  - `54:     @GetMapping("/Template")`
  - `86:     @PostMapping("/Uploader")`
  - `112:     @GetMapping("/Import")`

**BillRuleController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/base/controller/BillRuleController.java`
- 基础路径：`/Base/BillRule`
- 接口：
  - `50:    @GetMapping`
  - `65:    @GetMapping("/Selector")`
  - `82:    @PutMapping("/{id}/Actions/State")`
  - `104:    @GetMapping("/{id}")`
  - `118:    @GetMapping("/BillNumber/{enCode}")`
  - `131:    @PostMapping`
  - `152:    @PutMapping("/{id}")`
  - `175:    @DeleteMapping("/{id}")`
  - `190:    @GetMapping("/useBillNumber/{enCode}")`
  - `197:    @GetMapping("/getBillNumber/{enCode}")`

**DownloadController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/base/controller/DownloadController.java`
- 接口：
  - `58:    @PostMapping("/Uploader/{type}")`
  - `80:    @GetMapping("/Download/{type}/{fileName}")`
  - `97:    @GetMapping("/Download")`
  - `124:    @GetMapping("/DownloadModel")`
  - `151:    @GetMapping("/Image/{type}/{fileName}")`
  - `166:    @GetMapping("/getPath/{type}")`

**OwnerUserController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/owner/controller/OwnerUserController.java`
- 基础路径：`/owner/ownerUser`
- 接口：
  - `72:    @GetMapping("/list")`
  - `87:    @GetMapping(value = "{id}")`
  - `99:    @GetMapping(value = "detail/{id}")`
  - `133:    @PostMapping`
  - `147:    @PutMapping("/{id}")`
  - `161:	@DeleteMapping("/{id}")`

**PayLogPrintController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PayLogPrintController.java`
- 基础路径：`/print/printData`
- 接口：
  - `58:    @GetMapping("tempPrint")`
  - `75:    @GetMapping("billPrint")`
  - `93:    @GetMapping("depositPrint")`
  - `110:    @GetMapping("depositRefundPrint")`
  - `127:    @GetMapping("prePayPrint")`

**PaymentBillController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentBillController.java`
- 基础路径：`/payment/PaymentBill`
- 接口：
  - `63:    @GetMapping`
  - `82:    @GetMapping("/unpaied/{resourceName}")`
  - `98:    @GetMapping("/needPay/{resourceName}")`
  - `120:    @GetMapping("/paied/{resourceName}")`
  - `140:    @GetMapping("/{id}")`
  - `155:    @PutMapping("/{id}")`
  - `171:    @DeleteMapping("/{id}")`

**PaymentBillCreateController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentBillCreateController.java`
- 基础路径：`/payment/PaymentBillCreate`
- 接口：
  - `62:    @PostMapping`
  - `78:    @PostMapping("/getBatchCreateData")`
  - `106:    @PostMapping("/batchCreate")`
  - `122:    @PostMapping("/getGenerateData")`
  - `151:    @PostMapping("/generate")`
  - `167:    @PostMapping("/getMeterData")`
  - `194:    @PostMapping("/createMeterData")`

**PaymentBillNotifyController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentBillNotifyController.java`
- 基础路径：`/payment/PaymentBillNotify`
- 接口：
  - `78:    @GetMapping("unpaiedAndPayingList")`
  - `92:    @GetMapping("print")`
  - `110:    @GetMapping("download")`

**PaymentBillPayController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentBillPayController.java`
- 基础路径：`/payment/PaymentBillPay`
- 接口：
  - `49:    @PostMapping("/payCalc")`
  - `56:    @PostMapping("/payChceck")`
  - `65:    @PostMapping("/payBill")`
  - `76:    @PostMapping("/refundBill")`

**PaymentContractController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentContractController.java`
- 基础路径：`/payment/PaymentContract`
- 接口：
  - `86:    @GetMapping`
  - `106:    @PostMapping`
  - `122:    @PutMapping("/{id}")`
  - `137:    @GetMapping("/{id}")`
  - `165:    @GetMapping("/resourceName/{resourceName}")`
  - `189:    @GetMapping("/resourceNameTips/{resourceName}")`
  - `205:    @DeleteMapping("/{houseid}")`

**PaymentDepositController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentDepositController.java`
- 基础路径：`/payment/PaymentDeposit`
- 接口：
  - `73:    @GetMapping`
  - `101:    @PostMapping`
  - `117:    @GetMapping("/{id}")`
  - `132:    @PutMapping("/{id}")`

**PaymentMeterController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentMeterController.java`
- 基础路径：`/payment/PaymentMeter`
- 接口：
  - `67:    @GetMapping`
  - `86:    @PostMapping`
  - `102:    @GetMapping("/{id}")`
  - `117:    @PutMapping("/{id}")`
  - `133:    @DeleteMapping("/{id}")`

**PaymentMeterImportController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentMeterImportController.java`
- 基础路径：`/payment/PaymentMeterImport`
- 接口：
  - `82:    @GetMapping("/Template")`
  - `138:    @GetMapping("/Download")`
  - `170:    @PostMapping("/Uploader")`
  - `196:    @GetMapping("/Import")`

**PaymentMeterIndexController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentMeterIndexController.java`
- 基础路径：`/payment/PaymentMeterIndex`
- 接口：
  - `63:    @GetMapping`
  - `80:    @PostMapping`
  - `97:    @GetMapping("/{id}")`
  - `110:    @PutMapping("/{id}")`
  - `133:    @DeleteMapping("/{id}")`

**PaymentMethodController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentMethodController.java`
- 基础路径：`/payment/paymentMethod`
- 接口：
  - `67:    @GetMapping("/list")`
  - `82:    @GetMapping(value = "/{id}")`
  - `94:    @PostMapping`
  - `108:    @PutMapping("/{id}")`
  - `122:	@DeleteMapping("/{id}")`

**PaymentOrderController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentOrderController.java`
- 基础路径：`/payment/paymentOrder`
- 接口：
  - `75:    @GetMapping("/list")`
  - `122:    @GetMapping(value = "/{id}")`

**PaymentPreAccountController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentPreAccountController.java`
- 基础路径：`/payment/PaymentPreAccount`
- 接口：
  - `69:    @GetMapping`
  - `86:    @GetMapping("/accounts/{resourceId}")`
  - `113:    @GetMapping("/accounts/{resourceId}/canUse")`

**PaymentPreController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentPreController.java`
- 基础路径：`/payment/PaymentPre`
- 接口：
  - `74:    @GetMapping`
  - `102:    @PostMapping("/add")`
  - `119:    @PostMapping("/refund")`
  - `134:    @GetMapping("/{id}")`

**PaymentTempController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/PaymentTempController.java`
- 基础路径：`/payment/PaymentTemp`
- 接口：
  - `72:    @GetMapping`
  - `99:    @PostMapping`
  - `115:    @GetMapping("/{id}")`
  - `130:    @PutMapping("/{id}")`

**SysSmsController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/SysSmsController.java`
- 基础路径：`/payment/sms`
- 接口：
  - `59:    @GetMapping("/list")`
  - `74:    @GetMapping(value = "/{id}")`
  - `86:    @PostMapping`
  - `100:    @PutMapping("/{id}")`
  - `114:	@DeleteMapping("/{id}")`

**SysSmsTemplateController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/payment/controller/SysSmsTemplateController.java`
- 基础路径：`/payment/smsTemplate`
- 接口：
  - `59:    @GetMapping("/list")`
  - `74:    @GetMapping(value = "/{id}")`
  - `86:    @PostMapping`
  - `100:    @PutMapping("/{id}")`
  - `114:	@DeleteMapping("/{id}")`

**DailyFeeReportController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/statistics/controller/DailyFeeReportController.java`
- 基础路径：`/statistics/DailyFeeReport`
- 接口：
  - `55:    @GetMapping`
  - `254:    @GetMapping("getTitleHead")`

**DailyReportController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/statistics/controller/DailyReportController.java`
- 基础路径：`/statistics/DailyReport`
- 接口：
  - `50:    @GetMapping`

**DashboardController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/statistics/controller/DashboardController.java`
- 基础路径：`/statistics/dashboard`
- 接口：
  - `34:    @GetMapping`

**NextFeeStatisticsController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/statistics/controller/NextFeeStatisticsController.java`
- 基础路径：`/statistics`
- 接口：
  - `47:    @GetMapping("/nextFee")`

**PaymentBillStatisticsController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/statistics/controller/PaymentBillStatisticsController.java`
- 基础路径：`/statistics/paymentBill`
- 接口：
  - `56:    @GetMapping("/overdue")`

**PaymentPayLogController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/statistics/controller/PaymentPayLogController.java`
- 基础路径：`/statistics/PaymentPayLog`
- 接口：
  - `46:    @GetMapping`

**PaymentStatisticsController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/statistics/controller/PaymentStatisticsController.java`
- 基础路径：`/statistics/PaymentStatistics`
- 接口：
  - `52:    @GetMapping`
  - `105:    @GetMapping("/groupByHouse")`

**PreAccountController**

- 文件：`./zhaoxinwy-pms/src/main/java/com/zhaoxinms/statistics/controller/PreAccountController.java`
- 基础路径：`/statistics/preAccount`
- 接口：
  - `64:    @GetMapping`


### zhaoxinwy-quartz

**SysJobController**

- 文件：`./zhaoxinwy-quartz/src/main/java/com/zhaoxinms/quartz/controller/SysJobController.java`
- 基础路径：`/monitor/job`
- 接口：
  - `45:    @GetMapping("/list")`
  - `58:    @GetMapping("/export")`
  - `70:    @GetMapping(value = "/{jobId}")`
  - `81:    @PostMapping`
  - `113:    @PutMapping`
  - `145:    @PutMapping("/changeStatus")`
  - `158:    @PutMapping("/run")`
  - `170:    @DeleteMapping("/{jobIds}")`

**SysJobLogController**

- 文件：`./zhaoxinwy-quartz/src/main/java/com/zhaoxinms/quartz/controller/SysJobLogController.java`
- 基础路径：`/monitor/jobLog`
- 接口：
  - `37:    @GetMapping("/list")`
  - `50:    @GetMapping("/export")`
  - `62:    @GetMapping(value = "/{configId}")`
  - `74:    @DeleteMapping("/{jobLogIds}")`
  - `85:    @DeleteMapping("/clean")`


### zhaoxinwy-workflow

**FlowComplaintsController**

- 文件：`./zhaoxinwy-workflow/src/main/java/com/zhaoxinms/workflow/business/controller/FlowComplaintsController.java`
- 基础路径：`/business/complaints`
- 接口：
  - `58:    @GetMapping("/list")`
  - `84:    @GetMapping(value = "/{id}")`
  - `95:    @GetMapping(value = "/instanceId/{instanceId}")`
  - `108:    @PostMapping`
  - `121:    @PutMapping("/{id}")`
  - `134:	@DeleteMapping("/{id}")`

**FlowRepairController**

- 文件：`./zhaoxinwy-workflow/src/main/java/com/zhaoxinms/workflow/business/controller/FlowRepairController.java`
- 基础路径：`/business/repair`
- 接口：
  - `64:    @GetMapping("/list")`
  - `91:    @GetMapping(value = "/{id}")`
  - `103:    @GetMapping(value = "/instanceId/{instanceId}")`
  - `116:    @PostMapping`
  - `129:    @PutMapping("/{id}")`
  - `142:	@DeleteMapping("/{id}")`

**ActivitiProcessController**

- 文件：`./zhaoxinwy-workflow/src/main/java/com/zhaoxinms/workflow/engine/controller/ActivitiProcessController.java`
- 基础路径：`/activiti/process`
- 接口：
  - `91:    @PostMapping("/listHistory")`
  - `101:    @GetMapping("/deisnger/startFormOperates/{processDefKey}")`
  - `115:    @GetMapping("/designer/formOperates/{processInstanceId}/{taskId}")`
  - `153:    @GetMapping("/designer/{instanceId}")`
  - `187:    @PostMapping("/jumpTo")`
  - `232:    @PostMapping("/delegate")`
  - `247:    @PostMapping("/cancelApply")`
  - `263:    @PostMapping("/suspendOrActiveApply")`
  - `273:    @GetMapping("/taskList")`
  - `282:    @PostMapping("/complete")`

**ActivitiProcessDefinitionController**

- 文件：`./zhaoxinwy-workflow/src/main/java/com/zhaoxinms/workflow/engine/controller/ActivitiProcessDefinitionController.java`
- 基础路径：`/activiti/definition`
- 接口：
  - `64:    @GetMapping("/list")`
  - `75:    @PostMapping("/upload")`
  - `102:    @DeleteMapping("/remove/{deploymentId}")`
  - `114:    @PostMapping("/export")`
  - `122:    @PostMapping( "/suspendOrActiveDefinition")`
  - `152:    @PostMapping(value = "/convert2Model")`

**WorkflowDesignerController**

- 文件：`./zhaoxinwy-workflow/src/main/java/com/zhaoxinms/workflow/engine/controller/WorkflowDesignerController.java`
- 基础路径：`/workflow/designer`
- 接口：
  - `56:    @GetMapping("/deploy/{id}")`
  - `71:    @GetMapping`
  - `87:    @GetMapping("/{id}")`
  - `102:    @PostMapping`
  - `131:    @PutMapping("/{id}")`

**WorkflowSelectorController**

- 文件：`./zhaoxinwy-workflow/src/main/java/com/zhaoxinms/workflow/engine/controller/WorkflowSelectorController.java`
- 基础路径：`/roleSelect`

**ModelerController**

- 文件：`./zhaoxinwy-workflow/src/main/java/com/zhaoxinms/workflow/engine/modeler/ModelerController.java`
- 基础路径：`/activiti/modeler`
- 接口：
  - `57:    @GetMapping("/list")`
  - `92:    @PostMapping(value = "/create")`
  - `129:    @GetMapping(value = "/deploy/{modelId}")`
  - `153:    @GetMapping(value = "/export/{modelId}")`
  - `179:    @DeleteMapping("/remove/{ids}")`

---

## 四、数据库表结构概览

> 数据库名：`pms`  
> 字符集：`utf8mb4_unicode_ci`  
> 总表数：96 张  
> 详细建表语句见：`pms_schema.sql`


### Activiti 工作流（29 张）

| 表名 | 说明 |
|------|------|
| `ACT_EVT_LOG` | - |
| `ACT_GE_BYTEARRAY` | - |
| `ACT_GE_PROPERTY` | - |
| `ACT_HI_ACTINST` | - |
| `ACT_HI_ATTACHMENT` | - |
| `ACT_HI_COMMENT` | - |
| `ACT_HI_DETAIL` | - |
| `ACT_HI_IDENTITYLINK` | - |
| `ACT_HI_PROCINST` | - |
| `ACT_HI_TASKINST` | - |
| `ACT_HI_VARINST` | - |
| `ACT_PROCDEF_INFO` | - |
| `ACT_RE_DEPLOYMENT` | - |
| `ACT_RE_MODEL` | - |
| `ACT_RE_PROCDEF` | - |
| `ACT_RU_DEADLETTER_JOB` | - |
| `ACT_RU_EVENT_SUBSCR` | - |
| `ACT_RU_EXECUTION` | - |
| `ACT_RU_IDENTITYLINK` | - |
| `ACT_RU_JOB` | - |
| `ACT_RU_SUSPENDED_JOB` | - |
| `ACT_RU_TASK` | - |
| `ACT_RU_TIMER_JOB` | - |
| `ACT_RU_VARIABLE` | - |
| `act_designer` | - |
| `act_evt_log` | - |
| `act_id_group` | - |
| `act_id_membership` | - |
| `act_id_user` | - |

### 系统管理（20 张）

| 表名 | 说明 |
|------|------|
| `sys_config` | - |
| `sys_dept` | - |
| `sys_dict_data` | - |
| `sys_dict_type` | - |
| `sys_job` | - |
| `sys_job_log` | - |
| `sys_logininfor` | - |
| `sys_menu` | - |
| `sys_notice` | - |
| `sys_oper_log` | - |
| `sys_post` | - |
| `sys_role` | - |
| `sys_role_dept` | - |
| `sys_role_menu` | - |
| `sys_sms` | - |
| `sys_sms_template` | - |
| `sys_tenant` | - |
| `sys_user` | - |
| `sys_user_post` | - |
| `sys_user_role` | - |

### 定时任务（11 张）

| 表名 | 说明 |
|------|------|
| `qrtz_blob_triggers` | - |
| `qrtz_calendars` | - |
| `qrtz_cron_triggers` | - |
| `qrtz_fired_triggers` | - |
| `qrtz_job_details` | - |
| `qrtz_locks` | - |
| `qrtz_paused_trigger_grps` | - |
| `qrtz_scheduler_state` | - |
| `qrtz_simple_triggers` | - |
| `qrtz_simprop_triggers` | - |
| `qrtz_triggers` | - |

### 基础配置（6 张）

| 表名 | 说明 |
|------|------|
| `config_building` | - |
| `config_fee_alert` | - |
| `config_fee_item` | - |
| `config_fee_setting` | - |
| `config_house` | - |
| `config_house_block` | - |

### 收费管理（12 张）

| 表名 | 说明 |
|------|------|
| `payment_bill` | - |
| `payment_contract` | - |
| `payment_contract_fee` | - |
| `payment_deposit` | - |
| `payment_meter` | - |
| `payment_meter_index` | - |
| `payment_method` | - |
| `payment_order` | - |
| `payment_pay_log` | - |
| `payment_pre` | - |
| `payment_pre_account` | - |
| `payment_temp` | - |

### 业主管理（4 张）

| 表名 | 说明 |
|------|------|
| `owner_logininfor` | - |
| `owner_notice` | - |
| `owner_user` | - |
| `owner_wx_user` | - |

### 其他/通用（14 张）

| 表名 | 说明 |
|------|------|
| `base_billrule` | - |
| `flow_complaints` | - |
| `flow_repair` | - |
| `gen_table` | - |
| `gen_table_column` | - |
| `park` | - |
| `park_account` | - |
| `park_account_log` | - |
| `park_car` | - |
| `park_charging_rules` | - |
| `park_checkpoint` | - |
| `park_device` | - |
| `park_device_brand` | - |
| `park_use_log` | - |

---

## 五、关键配置说明

### 数据库连接配置
```yaml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://mysql:3306/pms?useUnicode=true&characterEncoding=utf8&...
        username: root
        password: root
```

### Redis 配置
```yaml
spring:
  redis:
    host: redis
    port: 6379
    password:
```

### 服务端口
| 服务 | 容器内端口 | 宿主机端口 |
|------|-----------|-----------|
| Frontend (Nginx) | 80 | 80 |
| Backend (Spring Boot) | 8080 | 8080 |
| MySQL | 3306 | 3308 |
| Redis | 6379 | 6381 |

---

## 六、Swagger 文档

启动服务后访问：
- http://localhost:8080/swagger-ui/index.html

---

*本文档由系统自动生成，最后更新于系统当前运行环境。*
